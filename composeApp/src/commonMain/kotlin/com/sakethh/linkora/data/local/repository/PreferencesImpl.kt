package com.sakethh.linkora.data.local.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import kotlinx.coroutines.flow.first

class PreferencesImpl(
    private val dataStore: DataStore<Preferences>
) : PreferencesRepository {

    override suspend fun <T> changePreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T
    ) {
        dataStore.edit {
            it[preferenceKey] = newValue
        }
    }

    override suspend fun <T> readPreferenceValue(
        preferenceKey: Preferences.Key<T>,
    ): T? {
        return dataStore.data.first()[preferenceKey]
    }

}