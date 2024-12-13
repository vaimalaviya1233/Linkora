package com.sakethh.linkora.domain.repository

import androidx.datastore.preferences.core.Preferences


interface PreferencesRepository {
    suspend fun <T> changeSettingPreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T,
    )

    suspend fun <T> readSettingPreferenceValue(
        preferenceKey: Preferences.Key<T>,
    ): T?
}