package com.sakethh.linkora.ui

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.DataSyncingNotificationService
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.pushSnackbar
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.asLinkType
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.NetworkRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import com.sakethh.linkora.ui.domain.TransferActionType
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM.Companion.clearAllSelections
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM.Companion.selectedFoldersViaLongClick
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM.Companion.selectedLinksViaLongClick
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch

class AppVM(
    private val remoteSyncRepo: RemoteSyncRepo,
    private val preferencesRepository: PreferencesRepository,
    private val networkRepo: NetworkRepo,
    private val linksRepo: LocalLinksRepo,
    private val foldersRepo: LocalFoldersRepo
) : ViewModel() {

    private val dataSyncingNotificationService = DataSyncingNotificationService()
    val isPerformingStartupSync = mutableStateOf(false)

    val transferActionType = mutableStateOf(TransferActionType.NONE)

    init {

        viewModelScope.launch {
            snapshotFlow {
                CollectionsScreenVM.isSelectionEnabled.value
            }.collectLatest {
                if (it.not()) {
                    transferActionType.value = TransferActionType.NONE
                }
            }
        }

        readSocketEvents(viewModelScope, remoteSyncRepo)

        viewModelScope.launch {
            if (AppPreferences.isServerConfigured()) {
                isPerformingStartupSync.value = true
                networkRepo.testServerConnection(
                    serverUrl = AppPreferences.serverBaseUrl.value + RemoteRoute.SyncInLocalRoute.TEST_BEARER.name,
                    token = AppPreferences.serverSecurityToken.value
                ).collectLatest {
                    it.onSuccess {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.SuccessfullyConnectedToTheServer.getLocalizedString()))
                        dataSyncingNotificationService.showNotification()
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
                                remoteSyncRepo.applyUpdatesBasedOnRemoteTombstones(
                                    AppPreferences.lastSyncedLocally(
                                        preferencesRepository
                                    )
                                ).collectLatest {
                                    it.pushSnackbarOnFailure()
                                }
                            }
                        }, launch {
                            if (AppPreferences.canReadFromServer()) {
                                remoteSyncRepo.applyUpdatesFromRemote(
                                    AppPreferences.lastSyncedLocally(
                                        preferencesRepository
                                    )
                                ).collectLatest {
                                    it.pushSnackbarOnFailure()
                                }
                            }
                        }).joinAll()
                    }.onFailure {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.ConnectionToServerFailed.getLocalizedString() + "\n" + it))
                    }
                }
            }
        }.invokeOnCompletion {
            isPerformingStartupSync.value = false
            dataSyncingNotificationService.clearNotification()
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

    fun moveSelectedItems(folderId: Long, onStart: () -> Unit, onCompletion: () -> Unit) {
        viewModelScope.launch {
            awaitAll(async {
                linksRepo.moveLinks(
                    folderId = folderId,
                    linkType = folderId.asLinkType(),
                    linkIds = CollectionsScreenVM.selectedLinksViaLongClick.toList().map {
                        it.localId
                    }).collectLatest {
                    it.onLoading {
                        onStart()
                    }
                    it.pushSnackbarOnFailure()
                }
            }, async {
                foldersRepo.moveFolders(
                    parentFolderId = folderId,
                    folderIDs = selectedFoldersViaLongClick.toList().map { it.localId })
                    .collectLatest {
                        it.pushSnackbarOnFailure()
                    }
            })
        }.invokeOnCompletion {
            onCompletion()
            CollectionsScreenVM.clearAllSelections()
        }
    }

    fun copySelectedItems(folderId: Long, onStart: () -> Unit, onCompletion: () -> Unit) {
        onStart()
        viewModelScope.launch {
            awaitAll(
                async {
                    linksRepo.copyLinks(
                        folderId = folderId,
                        linkType = folderId.asLinkType(),
                        links = selectedLinksViaLongClick.toList()
                    ).collect()
                },
            )
        }.invokeOnCompletion {
            onCompletion()
            clearAllSelections()
        }
    }

    fun archiveSelectedItems(onStart: () -> Unit, onCompletion: () -> Unit) {
        onStart()
        viewModelScope.launch {
            awaitAll(async {
                linksRepo.archiveMultipleLinks(
                    selectedLinksViaLongClick.toList().map { it.localId }).collect()
            }, async {
                foldersRepo.markMultipleFoldersAsArchive(
                    selectedFoldersViaLongClick.toList().map { it.localId }).collect()
            })
        }.invokeOnCompletion {
            onCompletion()
            clearAllSelections()
        }
    }

    fun deleteSelectedItems(onStart: () -> Unit, onCompletion: () -> Unit) {
        onStart()
        viewModelScope.launch {
            awaitAll(async {
                linksRepo.deleteMultipleLinks(
                    selectedLinksViaLongClick.toList().map { it.localId }).collect()
            }, async {
                foldersRepo.deleteMultipleFolders(
                    selectedFoldersViaLongClick.toList().map { it.localId }).collect()
            })
        }.invokeOnCompletion {
            clearAllSelections()
            onCompletion()
        }
    }

    fun markSelectedFoldersAsRoot(onStart: () -> Unit, onCompletion: () -> Unit) {
        onStart()
        viewModelScope.launch {
            foldersRepo.markFoldersAsRoot(selectedFoldersViaLongClick.toList().map { it.localId })
                .collect()
        }.invokeOnCompletion {
            clearAllSelections()
            onCompletion()
        }
    }

}