package com.sakethh.linkora.data

import com.sakethh.linkora.common.utils.catchAsExceptionAndEmitFailure
import com.sakethh.linkora.data.local.dao.LocalizationDao
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.localization.LocalizationInfoDTO
import com.sakethh.linkora.domain.model.localization.LocalizedLanguage
import com.sakethh.linkora.domain.model.localization.LocalizedString
import com.sakethh.linkora.domain.repository.LocalizationRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.statement.bodyAsText
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class LocalizationRepoImpl(
    private val httpClient: HttpClient,
    private val localizationServerURL: () -> String,
    private val localizationDao: LocalizationDao
) :
    LocalizationRepo.Remote, LocalizationRepo.Local {
    override fun getLanguagesFromServer(): Flow<Result<LocalizationInfoDTO>> {
        return flow {
            emit(Result.Loading())
            httpClient.get(localizationServerURL() + "info").body<LocalizationInfoDTO>().let {
                emit(Result.Success(it))
            }
        }.catchAsExceptionAndEmitFailure()
    }

    override suspend fun getLanguagePackFromServer(languageCode: String): Flow<Result<List<LocalizedString>>> {
        return flow {
            emit(Result.Loading())
            val localizedStrings = mutableListOf<LocalizedString>()
            httpClient.get(localizationServerURL() + languageCode)
                .bodyAsText()
                .substringAfter("<resources>")
                .substringBefore("</resources>")
                .split("<string").forEach {
                    if (it.substringAfter("name=\"").substringBefore("\">").trim().isNotBlank()) {
                        localizedStrings.add(
                            LocalizedString(
                                languageCode = languageCode,
                                stringName = it.substringAfter("name=\"").substringBefore("\">")
                                    .substringBefore("\"/>").trim(),
                                stringValue = it.substringAfter("\">").substringAfter("\"/>")
                                    .substringBefore("</string>")
                                    .trim()
                            )
                        )
                    }
                }
            emit(Result.Success(localizedStrings.toList()))
        }.catchAsExceptionAndEmitFailure()
    }

    private fun unitFlowResult(init: suspend () -> Unit): Flow<Result<Unit>> {
        return flow {
            emit(Result.Loading())
            init()
            emit(Result.Success(Unit))
        }.catchAsExceptionAndEmitFailure()
    }

    override suspend fun addLocalizedStrings(localizedStrings: List<LocalizedString>): Flow<Result<Unit>> {
        return unitFlowResult {
            localizationDao.addLocalizedStrings(localizedStrings)
        }
    }

    override suspend fun doesStringsPackForThisLanguageExists(languageCode: String): Boolean {
        return localizationDao.doesStringsPackForThisLanguageExists(languageCode)
    }

    override suspend fun deleteAllLocalizedStringsForThisLanguage(languageCode: String): Flow<Result<Unit>> {
        return unitFlowResult {
            localizationDao.deleteAllLocalizedStringsForThisLanguage(languageCode)
        }
    }

    override suspend fun getLocalizedStringValueFor(
        stringName: String,
        languageCode: String
    ): String? {
        return localizationDao.getLocalizedStringValueFor(stringName, languageCode)
    }

    override suspend fun addANewLanguage(localizedLanguage: LocalizedLanguage): Flow<Result<Unit>> {
        return unitFlowResult {
            localizationDao.addANewLanguage(localizedLanguage)
        }
    }

    override suspend fun addNewLanguages(languages: List<LocalizedLanguage>): Flow<Result<Unit>> {
        return unitFlowResult {
            localizationDao.addNewLanguages(languages)
        }
    }

    override suspend fun deleteALanguage(localizedLanguage: LocalizedLanguage): Flow<Result<Unit>> {
        return unitFlowResult {
            localizationDao.deleteALanguage(localizedLanguage)
        }
    }

    override suspend fun deleteALanguage(languageName: String): Flow<Result<Unit>> {
        return unitFlowResult {
            localizationDao.deleteALanguage(languageName)
        }
    }

    override suspend fun deleteALanguageBasedOnLanguageCode(languageCode: String): Flow<Result<Unit>> {
        return unitFlowResult {
            localizationDao.deleteALanguageBasedOnLanguageCode(languageCode)
        }
    }

    override suspend fun getLanguageNameForTheCode(languageCode: String): String {
        return localizationDao.getLanguageNameForTheCode(languageCode)
    }

    override suspend fun getLanguageCodeForTheLanguageNamed(languageName: String): String {
        return localizationDao.getLanguageCodeForTheLanguageNamed(languageName)
    }

    override fun getAllLanguages(): Flow<Result<List<LocalizedLanguage>>> {
        return localizationDao.getAllLanguages().map {
            Result.Success(it)
        }.onStart {
            Result.Loading<List<LocalizedLanguage>>()
        }.catchAsExceptionAndEmitFailure()
    }
}