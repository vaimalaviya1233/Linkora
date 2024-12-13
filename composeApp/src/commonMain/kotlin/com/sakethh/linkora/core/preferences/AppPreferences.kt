package com.sakethh.linkora.core.preferences

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import com.sakethh.linkora.domain.repository.PreferencesRepository
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope

object AppPreferences {
    val shouldUseForceDarkTheme = mutableStateOf(true)
    val shouldFollowSystemTheme = mutableStateOf(false)
    val shouldUseAmoledTheme = mutableStateOf(false)
    val shouldUseDynamicTheming = mutableStateOf(false)

    fun readAll(preferencesRepository: PreferencesRepository) = runBlocking {
        supervisorScope {
            listOf(
                launch {
                    shouldUseForceDarkTheme.value =
                        preferencesRepository.readSettingPreferenceValue(
                            booleanPreferencesKey(
                                AppPreferenceType.USE_DARK_THEME.name
                            )
                        )
                            ?: shouldUseForceDarkTheme.value
                },
                launch {
                    shouldFollowSystemTheme.value =
                        preferencesRepository.readSettingPreferenceValue(
                            booleanPreferencesKey(
                                AppPreferenceType.FOLLOW_SYSTEM_THEME.name
                            )
                        )
                            ?: shouldFollowSystemTheme.value
                },
                launch {
                    shouldUseAmoledTheme.value =
                        preferencesRepository.readSettingPreferenceValue(
                            booleanPreferencesKey(
                                AppPreferenceType.USE_AMOLED_THEME.name
                            )
                        )
                            ?: shouldUseAmoledTheme.value
                },
                launch {
                    shouldUseDynamicTheming.value =
                        preferencesRepository.readSettingPreferenceValue(
                            booleanPreferencesKey(
                                AppPreferenceType.USE_DYNAMIC_THEMING.name
                            )
                        )
                            ?: shouldUseDynamicTheming.value
                },
            ).joinAll()
        }
    }
}