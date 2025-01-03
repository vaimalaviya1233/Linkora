package com.sakethh.linkora.ui.screens.settings.section

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.domain.model.localization.LocalizedLanguage
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.LocalizationRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class LanguageSettingsScreenVM(
    private val localizationRepoRemote: LocalizationRepo.Remote,
    val localizationRepoLocal: LocalizationRepo.Local
) : ViewModel() {
    private val _availableLanguages = MutableStateFlow(
        emptyList<LocalizedLanguage>()
    )
    val availableLanguages = _availableLanguages.asStateFlow()

    fun doesLanguagePackExists(exists: MutableState<Boolean>, languageCode: String) = runBlocking {
        exists.value = localizationRepoLocal.doesStringsPackForThisLanguageExists(languageCode)
    }

    fun fetchRemoteLanguages() {
        viewModelScope.launch {
            localizationRepoRemote.getLanguagesFromServer().collect {
                it.onSuccess {
                    localizationRepoLocal.addNewLanguages(it.data.availableLanguages.filter { it.languageCode != "en" }
                        .map {
                            LocalizedLanguage(
                                languageCode = it.languageCode,
                                languageName = it.languageName,
                                localizedStringsCount = it.localizedStringsCount,
                                contributionLink = it.contributionLink
                            )
                        }).collectLatest {
                        it.onSuccess {
                            pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.SavedAvailableLanguagesInfoLocally.getLocalizedString()))
                        }

                        it.onFailure {
                            pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                        }
                    }
                }

                it.onFailure {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            localizationRepoLocal.getAllLanguages().collectLatest {
                it.onSuccess {
                    _availableLanguages.emit(it.data)
                }
                it.onFailure {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                }
            }
        }
    }

    fun deleteALanguagePack(language: LocalizedLanguage) {
        viewModelScope.launch {
            localizationRepoLocal.deleteAllLocalizedStringsForThisLanguage(language.languageCode)
                .collectLatest {
                    it.onSuccess {
                        pushUIEvent(
                            UIEvent.Type.ShowSnackbar(
                                Localization.Key.DeletedTheStringsPack.getLocalizedString().replace(
                                    LinkoraPlaceHolder.First.value,
                                    "\"${language.languageName}\""
                                )
                            )
                        )
                    }
                    it.onFailure {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                    }
                }
        }
    }

    fun downloadALanguageStringsPack(language: LocalizedLanguage) {
        viewModelScope.launch {
            localizationRepoRemote.getLanguagePackFromServer(language.languageCode).collectLatest {
                it.onSuccess {
                    localizationRepoLocal.addLocalizedStrings(it.data).collectLatest {
                        it.onSuccess {
                            pushUIEvent(
                                UIEvent.Type.ShowSnackbar(
                                    message = Localization.Key.DownloadedLanguageStrings.getLocalizedString()
                                        .replace(
                                            LinkoraPlaceHolder.First.value,
                                            "\"${language.languageName}\""
                                        )
                                )
                            )
                        }

                        it.onFailure {
                            pushUIEvent(UIEvent.Type.ShowSnackbar(message = it))
                        }
                    }
                }

                it.onFailure {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = it))
                }
            }
        }
    }
}