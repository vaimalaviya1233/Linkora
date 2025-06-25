package com.sakethh.linkora.ui.screens.settings.section.data.sync

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.SyncType
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.NetworkRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import com.sakethh.linkora.ui.AppVM
import com.sakethh.linkora.ui.domain.model.ServerConnection
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.permittedToShowNotification
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class ServerManagementViewModel(
    private val networkRepo: NetworkRepo,
    val preferencesRepository: PreferencesRepository,
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
                    permittedToShowNotification()
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

    val dataSyncLogs = mutableStateListOf<String>()
    private var saveServerConnectionAndSyncJob: Job? = null

    fun cancelServerConnectionAndSync(removeConnection: Boolean = true) {
        saveServerConnectionAndSyncJob?.cancel()
        if (removeConnection.not()) {
            return
        }
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

    fun saveServerConnectionAndSync(
        serverConnection: ServerConnection,
        timeStampAfter: suspend () -> Long = { 0 },
        onSyncStart: () -> Unit,
        onCompletion: () -> Unit
    ) {
        saveServerConnectionAndSyncJob?.cancel()
        dataSyncLogs.clear()
        saveServerConnectionAndSyncJob = viewModelScope.launch {
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
            onSyncStart()
            if (AppPreferences.canReadFromServer()) {
                remoteSyncRepo.applyUpdatesFromRemote(timeStampAfter()).collectLatest {
                    it.onLoading {
                        dataSyncLogs.add(it)
                    }.onSuccess {
                        AppVM.readSocketEvents(remoteSyncRepo)
                    }.onFailure {
                        cancel()
                    }.pushSnackbarOnFailure()
                }
            }
            if (AppPreferences.canPushToServer()) {
                with(remoteSyncRepo) {
                    channelFlow {
                        this.pushNonSyncedDataToServer<Unit>()
                    }.collectLatest {
                        it.onLoading {
                            dataSyncLogs.add(it)
                        }
                        it.onSuccess {
                            onCompletion()
                        }
                        it.onFailure {
                            cancel()
                        }.pushSnackbarOnFailure()
                    }
                }
            }
        }
        saveServerConnectionAndSyncJob?.invokeOnCompletion {
            if (it == null) {
                viewModelScope.launch {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.DataSynchronizationCompletedSuccessfully.getLocalizedString()))
                }
            }
            onCompletion()
            dataSyncLogs.clear()
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