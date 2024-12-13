package com.sakethh.linkora.ui.screens.settings

import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.domain.repository.PreferencesRepository
import kotlinx.coroutines.launch

class SettingsScreenViewModel(
    val preferencesRepository: PreferencesRepository
) : ViewModel() {

    fun <T> changeSettingPreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T,
    ) {
        viewModelScope.launch {
            preferencesRepository.changeSettingPreferenceValue(preferenceKey, newValue)
        }
    }
}