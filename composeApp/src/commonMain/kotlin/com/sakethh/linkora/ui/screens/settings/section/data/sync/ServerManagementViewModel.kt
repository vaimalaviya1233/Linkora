package com.sakethh.linkora.ui.screens.settings.section.data.sync

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.platform.FileManager
import com.sakethh.linkora.platform.PermissionManager
import com.sakethh.linkora.Localization
import com.sakethh.linkora.network.Network
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.FileType
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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

open class ServerManagementViewModel(
    private val networkRepo: NetworkRepo,
    val preferencesRepository: PreferencesRepository,
    private val remoteSyncRepo: RemoteSyncRepo,
    private val fileManager: FileManager,
    private val permissionManager: PermissionManager,
    loadExistingCertificateInfo: Boolean = true
) : ViewModel() {
    val serverSetupState = mutableStateOf(
        ServerSetupState(
            isConnecting = false, isConnectedSuccessfully = false, isError = false
        )
    )


    fun resetState() {
        serverSetupState.value = ServerSetupState(
            isConnecting = false, isConnectedSuccessfully = false, isError = false
        )
    }

    fun testServerConnection(serverUrl: String, token: String) {
        viewModelScope.launch {
            val certificateFactory = CertificateFactory.getInstance("X.509")
            val signedCertificate: X509Certificate? =
                _generatedServerCertificate?.inputStream().use {
                    try {
                        certificateFactory.generateCertificate(it) as X509Certificate
                    } catch (e: Exception) {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(e.message.toString()))
                        null
                    }
                }
            try {

                Network.closeSyncServerClient()
                Network.configureSyncServerClient(
                    signedCertificate = signedCertificate,
                    bypassCertCheck = AppPreferences.skipCertCheckForSync.value
                )

                networkRepo.testServerConnection(serverUrl, token).collectLatest {
                    it.onSuccess {
                        serverSetupState.value = ServerSetupState(
                            isConnecting = false, isConnectedSuccessfully = true, isError = false
                        )
                        permissionManager.permittedToShowNotification()
                    }.onFailure { failureMessage ->
                        serverSetupState.value = ServerSetupState(
                            isConnecting = false, isConnectedSuccessfully = false, isError = true
                        )
                        pushUIEvent(UIEvent.Type.ShowSnackbar(failureMessage))
                    }.onLoading {
                        serverSetupState.value = ServerSetupState(
                            isConnecting = true, isConnectedSuccessfully = false, isError = false
                        )
                    }
                }
            } catch (e: Exception) {
                pushUIEvent(UIEvent.Type.ShowSnackbar(e.message.toString()))
            }
        }
    }

    val dataSyncLogs = mutableStateListOf<String>()
    private var saveServerConnectionAndSyncJob: Job? = null

    fun cancelServerConnectionAndSync(removeConnection: Boolean = true) {
        Network.closeSyncServerClient()
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
        AppVM.pauseSnapshots = true
        saveServerConnectionAndSyncJob?.cancel()
        dataSyncLogs.clear()
        saveServerConnectionAndSyncJob = viewModelScope.launch {
            _generatedServerCertificate?.let {
                withContext(Dispatchers.IO) {
                    fileManager.saveSyncServerCertificateInternally(file = it, onCompletion = {
                        pushUIEvent(
                            UIEvent.Type.ShowSnackbar(
                                Localization.getLocalizedString(
                                    Localization.Key.ServerCertificateSavedSuccessfully
                                )
                            )
                        )
                    })
                }
            }
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
                    }.onFailure { _ ->
                        it.pushSnackbarOnFailure()
                        cancel()
                    }
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
            AppVM.pauseSnapshots = false
            AppVM.forceSnapshot()
            onCompletion()
            dataSyncLogs.clear()
        }
    }

    val existingCertificateInfo = mutableStateOf("")

    init {
        if (loadExistingCertificateInfo && !AppPreferences.skipCertCheckForSync.value) {
            viewModelScope.launch {
                existingCertificateInfo.value = getExistingSyncServerCertificate(fileManager).run {
                    if (this == null) {
                        ""
                    } else {
                        "Certificate Authority:\n${this.issuerX500Principal.name}\nStatus: ${
                            try {
                                this.checkValidity()
                                "Valid"
                            } catch (e: Exception) {
                                e.printStackTrace()
                                "Invalid"
                            }
                        }".trim()
                    }
                }
            }
        }
    }

    companion object {
        suspend fun getExistingSyncServerCertificate(fileManager: FileManager): X509Certificate? {
            return if (AppPreferences.isServerConfigured()) {
                withContext(Dispatchers.IO) {
                    val certificateFactory = CertificateFactory.getInstance("X.509")
                    try {
                        fileManager.loadSyncServerCertificate().inputStream().use {
                            certificateFactory.generateCertificate(it) as X509Certificate
                        }
                    } catch (e: Exception) {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(e.message.toString()))
                        null
                    }
                }
            } else {
                null
            }
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
            Network.closeSyncServerClient()

            pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.getLocalizedString(Localization.Key.DeletedTheServerConnectionSuccessfully)))
        }.invokeOnCompletion {
            onDeleted()
        }
    }

    private var certImportJob: Job? = null
    private var _generatedServerCertificate: File? = null

    fun importSignedCertificate(onStart: () -> Unit, onCompletion: (filename: String) -> Unit) {
        certImportJob?.cancel()
        onStart()
        certImportJob = viewModelScope.launch {
            val generatedServerCertificate =
                fileManager.pickAValidFileForImporting(FileType.CER, onStart = {
                    onStart()
                    dataSyncLogs.add(Localization.Key.ReadingFile.getLocalizedString())
                })
            if (generatedServerCertificate == null) {
                pushUIEvent(UIEvent.Type.ShowSnackbar("Could not import the certificate file."))
                return@launch
            }
            _generatedServerCertificate = generatedServerCertificate
            onCompletion(generatedServerCertificate.name)
        }

        certImportJob?.invokeOnCompletion {
            it?.printStackTrace()
        }
    }

    fun updateCertificateBypassRule(bypass: Boolean) {
        viewModelScope.launch {
            preferencesRepository.changePreferenceValue(
                preferenceKey = booleanPreferencesKey(AppPreferenceType.SKIP_CERT_CHECK_FOR_SYNC_SERVER.name),
                newValue = bypass
            )
        }.invokeOnCompletion {
            AppPreferences.skipCertCheckForSync.value = bypass
        }
    }
}