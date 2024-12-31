package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.localization.LocalizationInfoDTO
import com.sakethh.linkora.domain.repository.remote.LocalizationRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class LocalizationRepoImpl(val httpClient: HttpClient, val localizationServerURL: String) :
    LocalizationRepo {
    override fun getRemoteLanguages(): Flow<Result<LocalizationInfoDTO>> {
        return flow {
            emit(Result.Loading())
            httpClient.get(localizationServerURL + "info").body<LocalizationInfoDTO>().let {
                emit(Result.Success(it))
            }
        }.catch {
            it.printStackTrace()
            it as Exception
            emit(Result.Failure(it.message.toString()))
        }
    }
}