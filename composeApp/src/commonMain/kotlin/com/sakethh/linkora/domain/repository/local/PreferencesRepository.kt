package com.sakethh.linkora.domain.repository.local

import androidx.datastore.preferences.core.Preferences


interface PreferencesRepository {
    suspend fun <T> changePreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T,
    )

    suspend fun <T> readPreferenceValue(
        preferenceKey: Preferences.Key<T>,
    ): T?
}