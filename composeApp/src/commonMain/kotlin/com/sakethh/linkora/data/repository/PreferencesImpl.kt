package com.sakethh.linkora.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sakethh.linkora.domain.repository.PreferencesRepository
import kotlinx.coroutines.flow.first

class PreferencesImpl(
    val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override suspend fun <T> changeSettingPreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T
    ) {
        dataStore.edit {
            it[preferenceKey] = newValue
        }
    }

    override suspend fun <T> readSettingPreferenceValue(
        preferenceKey: Preferences.Key<T>,
    ): T? {
        return dataStore.data.first()[preferenceKey]
    }

}