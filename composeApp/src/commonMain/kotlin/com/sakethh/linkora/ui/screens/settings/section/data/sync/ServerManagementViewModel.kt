package com.sakethh.linkora.ui.screens.settings.section.data.sync

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.AppVM
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.SyncType
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.NetworkRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import com.sakethh.linkora.ui.domain.model.ServerConnection
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServerManagementViewModel(
    private val networkRepo: NetworkRepo,
    private val preferencesRepository: PreferencesRepository,
    private val remoteSyncRepo: RemoteSyncRepo
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

    val dataImportLogs = mutableStateListOf<String>()
    private var saveServerConnectionAndImportDataJob: Job? = null

    fun cancelServerConnectionAndImporting() {
        saveServerConnectionAndImportDataJob?.cancel()
        viewModelScope.launch {
            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_URL.name
                ), newValue = ""
            )
            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_AUTH_TOKEN.name
                ), newValue = ""
            )
            AppPreferences.serverBaseUrl.value = ""
            AppPreferences.serverSecurityToken.value = ""
        }
    }

    fun saveServerConnectionAndImport(
        serverConnection: ServerConnection, onImportStart: () -> Unit, onCompletion: () -> Unit
    ) {
        saveServerConnectionAndImportDataJob?.cancel()
        saveServerConnectionAndImportDataJob = viewModelScope.launch {
            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_URL.name
                ), newValue = serverConnection.serverUrl
            )
            AppPreferences.serverBaseUrl.value = serverConnection.serverUrl

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
            if (AppPreferences.canReadFromServer()) {
                onImportStart()
                remoteSyncRepo.applyUpdatesFromRemote(0).collectLatest {
                    it.onLoading {
                        dataImportLogs.add(it)
                    }.onSuccess {
                        with(remoteSyncRepo) {
                            channelFlow {
                                this.pushNonSyncedDataToServer<Unit>()
                            }.collectLatest {
                                it.onLoading {
                                    dataImportLogs.add(it)
                                }
                                it.onSuccess {
                                    onCompletion()
                                }
                            }
                        }
                        pushUIEvent(
                            UIEvent.Type.ShowSnackbar(
                                Localization.getLocalizedString(
                                    Localization.Key.SuccessfullySavedConnectionDetails
                                )
                            )
                        )
                        AppVM.readSocketEvents(viewModelScope, remoteSyncRepo)
                    }.pushSnackbarOnFailure()
                }
            }
        }
        saveServerConnectionAndImportDataJob?.invokeOnCompletion {
            onCompletion()
            dataImportLogs.clear()
        }
    }

    fun deleteTheConnection(onDeleted: () -> Unit) {
        viewModelScope.launch {
            preferencesRepository.changePreferenceValue(
                preferenceKey = stringPreferencesKey(
                    AppPreferenceType.SERVER_URL.name
                ), newValue = ""
            )
            AppPreferences.serverBaseUrl.value = ""

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

            AppVM.shutdownSocketConnection()

            pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.getLocalizedString(Localization.Key.DeletedTheServerConnectionSuccessfully)))
        }.invokeOnCompletion {
            onDeleted()
        }
    }
}