package com.sakethh.linkora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.pushSnackbar
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.NetworkRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class AppVM(
    private val remoteSyncRepo: RemoteSyncRepo,
    private val preferencesRepository: PreferencesRepository,
    private val networkRepo: NetworkRepo
) : ViewModel() {
    init {
        readSocketEvents(viewModelScope, remoteSyncRepo)

        viewModelScope.launch {
            launch {
                if (AppPreferences.isServerConfigured()) {
                    networkRepo.testServerConnection(
                        serverUrl = AppPreferences.serverBaseUrl.value + RemoteRoute.SyncInLocalRoute.TEST_BEARER.name,
                        token = AppPreferences.serverSecurityToken.value
                    ).collectLatest {
                        it.onSuccess {
                            pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.SuccessfullyConnectedToTheServer.getLocalizedString()))
                        }.onFailure {
                            pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.ConnectionToServerFailed.getLocalizedString() + "\n" + it))
                        }
                    }
                }
            }
            launch {
                if (AppPreferences.canPushToServer()) {
                    with(remoteSyncRepo) {
                        channelFlow {
                            pushPendingSyncQueueToServer<Unit>().collectLatest {
                                it.pushSnackbarOnFailure()
                            }
                        }.collect()
                    }
                }
            }

            listOf(launch {
                if (AppPreferences.canReadFromServer()) {
                    remoteSyncRepo.applyUpdatesBasedOnRemoteTombstones(AppPreferences.lastSyncedLocally())
                        .collectLatest {
                            it.pushSnackbarOnFailure()
                        }
                }
            }, launch {
                if (AppPreferences.canReadFromServer()) {
                    remoteSyncRepo.applyUpdatesFromRemote(AppPreferences.lastSyncedLocally())
                        .collectLatest {
                            it.pushSnackbarOnFailure()
                        }
                }
            }).joinAll()
        }
    }

    companion object {
        private var socketEventJob: Job? = null

        fun shutdownSocketConnection() {
            socketEventJob?.cancel()
        }

        fun readSocketEvents(coroutineScope: CoroutineScope, remoteSyncRepo: RemoteSyncRepo) {
            if (AppPreferences.canReadFromServer().not()) return

            socketEventJob?.cancel()
            socketEventJob = coroutineScope.launch(CoroutineExceptionHandler { _, throwable ->
                throwable.printStackTrace()
                throwable.pushSnackbar(coroutineScope)
            }) {
                remoteSyncRepo.readSocketEvents(AppPreferences.getCorrelation()).collectLatest {
                    it.pushSnackbarOnFailure()
                }
            }
        }
    }
}