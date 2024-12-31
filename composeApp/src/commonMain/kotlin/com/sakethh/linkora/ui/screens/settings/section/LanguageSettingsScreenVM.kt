package com.sakethh.linkora.ui.screens.settings.section

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.domain.dto.localization.LocalizationInfoDTO
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.remote.LocalizationRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class LanguageSettingsScreenVM(private val localizationRepo: LocalizationRepo) : ViewModel() {
    private val _availableLanguages = MutableStateFlow(
        LocalizationInfoDTO(
            availableLanguages = listOf(),
            totalAvailableLanguages = 0,
            totalStrings = 0,
            lastUpdatedOn = ""
        )
    )
    val availableLanguages = _availableLanguages.asStateFlow()

    fun fetchRemoteLanguages() {
        viewModelScope.launch {
            localizationRepo.getRemoteLanguages().collect {
                it.onSuccess {
                    _availableLanguages.emit(it.data)
                }
            }
        }
    }
}