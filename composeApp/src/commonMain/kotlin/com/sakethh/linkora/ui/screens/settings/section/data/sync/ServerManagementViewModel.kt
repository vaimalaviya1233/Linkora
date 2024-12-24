package com.sakethh.linkora.ui.screens.settings.section.data.sync

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.SyncType
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.NetworkRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.domain.model.ServerConnection
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServerManagementViewModel(
    private val networkRepo: NetworkRepo, private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    val serverSetupState = mutableStateOf(
        ServerSetupState(
            isConnecting = false,
            isConnectedSuccessfully = false,
            isError = false
        )
    )


    fun resetState() {
        serverSetupState.value = ServerSetupState(
            isConnecting = false,
            isConnectedSuccessfully = false,
            isError = false
        )
    }

    fun testServerConnection(serverUrl: String, token: String) {
        viewModelScope.launch {
            networkRepo.testServerConnection(serverUrl, token).collectLatest {
                it.onSuccess {
                    serverSetupState.value = ServerSetupState(
                        isConnecting = false,
                        isConnectedSuccessfully = true,
                        isError = false
                    )
                }.onFailure { failureMessage ->
                    serverSetupState.value = ServerSetupState(
                        isConnecting = false,
                        isConnectedSuccessfully = false,
                        isError = true
                    )
                    pushUIEvent(UIEvent.Type.ShowSnackbar(failureMessage))
                }.onLoading {
                    serverSetupState.value = ServerSetupState(
                        isConnecting = true,
                        isConnectedSuccessfully = false,
                        isError = false
                    )
                }
            }
        }
    }

    fun saveServerConnection(
        serverConnection: ServerConnection, onSaved: () -> Unit = {
            viewModelScope.launch {
                pushUIEvent(UIEvent.Type.ShowSnackbar("Successfully saved connection details."))
            }
        }
    ) {
        viewModelScope.launch {
            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_URL.name
                ), newValue = serverConnection.serverUrl
            )
            AppPreferences.serverUrl.value = serverConnection.serverUrl

            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(AppPreferenceType.SERVER_AUTH_TOKEN.name),
                newValue = serverConnection.authToken
            )
            AppPreferences.serverSecurityToken.value = serverConnection.authToken

            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_SYNC_TYPE.name
                ), newValue = serverConnection.syncType.name
            )
            AppPreferences.serverSyncType.value = serverConnection.syncType

        }.invokeOnCompletion {
            onSaved()
        }
    }

    fun deleteTheConnection(onDeleted: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_URL.name
                ), newValue = ""
            )
            AppPreferences.serverUrl.value = ""

            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(AppPreferenceType.SERVER_AUTH_TOKEN.name),
                newValue = ""
            )
            AppPreferences.serverSecurityToken.value = ""

            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_SYNC_TYPE.name
                ), newValue = ""
            )
            AppPreferences.serverSyncType.value = SyncType.TwoWay
            pushUIEvent(UIEvent.Type.ShowSnackbar("Deleted the server connection successfully."))
        }.invokeOnCompletion {
            onDeleted()
        }
    }
}