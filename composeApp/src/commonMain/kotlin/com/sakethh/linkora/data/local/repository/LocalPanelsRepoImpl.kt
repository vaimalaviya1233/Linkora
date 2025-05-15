package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.common.utils.updateLastSyncedWithServerTimeStamp
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.PanelsDao
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.DeleteAFolderFromAPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemotePanelsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.properties.Delegates

class LocalPanelsRepoImpl(
    private val panelsDao: PanelsDao,
    private val remotePanelsRepo: RemotePanelsRepo,
    private val foldersDao: FoldersDao,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val preferencesRepository: PreferencesRepository
) : LocalPanelsRepo {
    override suspend fun addaNewPanel(panel: Panel, viaSocket: Boolean): Flow<Result<Unit>> {
        var newPanelId: Long? = null
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (newPanelId == null) return@performLocalOperationWithRemoteSyncFlow emptyFlow()

                remotePanelsRepo.addANewPanel(
                    AddANewPanelDTO(
                        panel.panelName, eventTimestamp = eventTimestamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                if (newPanelId == null) return@performLocalOperationWithRemoteSyncFlow

                panelsDao.updateAPanel(
                    panelsDao.getPanel(newPanelId).copy(
                        remoteId = it.id, lastModified = it.timeStampBasedResponse.eventTimestamp
                    )
                )
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.timeStampBasedResponse.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (newPanelId == null) return@performLocalOperationWithRemoteSyncFlow

                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Panel.ADD_A_NEW_PANEL.name,
                        payload = Json.encodeToString(
                            AddANewPanelDTO(
                                panel.panelName,
                                offlineSyncItemId = newPanelId!!,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            newPanelId =
                panelsDao.addaNewPanel(panel.copy(localId = 0, lastModified = eventTimestamp))
        }
    }

    override suspend fun deleteAPanel(id: Long, viaSocket: Boolean): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(id)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remotePanelId != null) {
                    remotePanelsRepo.deleteAPanel(
                        IDBasedDTO(
                            id = id, eventTimestamp = Instant.now().epochSecond
                        )
                    )
                    // server already handles this internally, so no need to push it externally: remotePanelsRepo.deleteAllFoldersFromAPanel(remotePanelId)
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            }, onRemoteOperationFailure = {
                if (remotePanelId != null) {pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Panel.DELETE_A_PANEL.name,
                        payload = Json.encodeToString(
                            IDBasedDTO(
                                id = remotePanelId,
                                eventTimestamp = 0
                            )
                        )
                    )
                )}
            }) {
            panelsDao.deleteAPanel(id)
            panelsDao.deleteConnectedFoldersOfPanel(id)
        }
    }

    override suspend fun updateAPanelName(
        newName: String, panelId: Long, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(panelId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remotePanelId != null) {
                    remotePanelsRepo.updateAPanelName(
                        UpdatePanelNameDTO(
                            newName, remotePanelId, eventTimestamp = eventTimestamp
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                panelsDao.updatePanelTimestamp(panelId, it.eventTimestamp)
            }, onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name,
                        payload = Json.encodeToString(
                            UpdatePanelNameDTO(
                                newName, panelId,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            panelsDao.updateAPanelName(newName, panelId)
            panelsDao.updatePanelTimestamp(panelId, eventTimestamp)
        }
    }

    override suspend fun updatePanel(panel: Panel) {
        panelsDao.updateAPanel(panel)
    }

    override suspend fun updateAPanelFolder(panelFolder: PanelFolder) {
        panelsDao.updateAPanelFolder(panelFolder)
    }

    override suspend fun addANewFolderInAPanel(
        panelFolder: PanelFolder, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        var newPanelFolderId: Long? = null
        val remoteIdOfFolder = foldersDao.getRemoteIdOfAFolder(panelFolder.folderId)
        val remoteIdOfConnectedPanel = panelsDao.getRemoteIdOfPanel(panelFolder.connectedPanelId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (newPanelFolderId == null) return@performLocalOperationWithRemoteSyncFlow emptyFlow()

                if (remoteIdOfFolder != null && remoteIdOfConnectedPanel != null) {
                    remotePanelsRepo.addANewFolderInAPanel(
                        AddANewPanelFolderDTO(
                            folderId = remoteIdOfFolder,
                            panelPosition = panelFolder.panelPosition,
                            folderName = panelFolder.folderName,
                            connectedPanelId = remoteIdOfConnectedPanel,
                            eventTimestamp = eventTimestamp
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                if (newPanelFolderId == null) return@performLocalOperationWithRemoteSyncFlow

                panelsDao.updateAPanelFolder(
                    panelsDao.getPanelFolder(newPanelFolderId).copy(
                        remoteId = it.id, lastModified = it.timeStampBasedResponse.eventTimestamp
                    )
                )
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.timeStampBasedResponse.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (newPanelFolderId == null) return@performLocalOperationWithRemoteSyncFlow

                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name,
                        payload = Json.encodeToString(
                            AddANewPanelFolderDTO(
                                folderId = panelFolder.folderId,
                                panelPosition = panelFolder.panelPosition,
                                folderName = panelFolder.folderName,
                                connectedPanelId = panelFolder.connectedPanelId,
                                offlineSyncItemId = newPanelFolderId!!,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            newPanelFolderId = panelsDao.addANewFolderInAPanel(
                panelFolder.copy(
                    localId = 0, lastModified = eventTimestamp
                )
            )
            newPanelFolderId
        }
    }

    override suspend fun deleteAFolderFromAllPanels(folderID: Long) {
        // server already handles this internally, so no need to push it externally
        panelsDao.deleteAFolderFromAllPanels(folderID)
    }

    override suspend fun getLatestPanelID(): Long {
        return panelsDao.getLatestPanelID()
    }

    override suspend fun deleteAFolderFromAPanel(
        panelId: Long, folderID: Long, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(panelId)
        val remoteFolderId = foldersDao.getRemoteIdOfAFolder(folderID)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remotePanelId != null && remoteFolderId != null) {
                    remotePanelsRepo.deleteAFolderFromAPanel(
                        DeleteAFolderFromAPanelDTO(
                            panelId = remotePanelId,
                            folderID = remoteFolderId,
                            eventTimestamp = eventTimestamp
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            }, onRemoteOperationFailure = {
                if (remotePanelId != null && remoteFolderId != null) {pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Panel.DELETE_A_FOLDER_FROM_A_PANEL.name,
                        payload = Json.encodeToString(
                            DeleteAFolderFromAPanelDTO(
                                panelId = remotePanelId,
                                folderID = remoteFolderId,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )}
            }) {
            panelsDao.deleteAFolderFromAPanel(panelId, folderID)
        }
    }

    override fun getAllThePanels(): Flow<List<Panel>> {
        return panelsDao.getAllThePanels()
    }

    override suspend fun getAllThePanelsAsAList(): List<Panel> {
        return panelsDao.getAllThePanelsAsAList()
    }

    override suspend fun getAllThePanelFoldersAsAList(): List<PanelFolder> {
        return panelsDao.getAllThePanelFoldersAsAList()
    }

    override fun getAllThePanelFoldersAsAFlow(): Flow<List<PanelFolder>> {
        return panelsDao.getAllThePanelFoldersAsAFlow()
    }

    override fun getAllTheFoldersFromAPanel(panelId: Long): Flow<List<PanelFolder>> {
        return panelsDao.getAllTheFoldersFromAPanel(panelId)
    }

    override suspend fun getPanel(panelId: Long): Panel {
        return panelsDao.getPanel(panelId)
    }

    override suspend fun getLocalPanelId(remoteId: Long): Long? {
        return panelsDao.getLocalPanelId(remoteId)
    }

    override suspend fun getRemotePanelId(localId: Long): Long? {
        return panelsDao.getRemoteIdOfPanel(localId)
    }

    override suspend fun getLocalPanelFolderId(remoteId: Long): Long? {
        return panelsDao.getLocalPanelFolderId(remoteId)
    }

    override suspend fun updateAFolderName(folderID: Long, newName: String) {
        return panelsDao.updateAFolderName(folderID, newName)
    }

    override suspend fun addMultiplePanelFolders(panelFolders: List<PanelFolder>) {
        panelsDao.addMultiplePanelFolders(panelFolders)
    }

    override suspend fun deleteAllPanels() {
        panelsDao.deleteAllPanels()
    }

    override suspend fun deleteAllPanelFolders() {
        panelsDao.deleteAllPanelFolders()
    }

    override suspend fun addMultiplePanels(panels: List<Panel>) {
        panelsDao.addMultiplePanels(panels)
    }

    override suspend fun getUnSyncedPanelFolders(): List<PanelFolder> {
        return panelsDao.getUnSyncedPanelFolders()
    }

    override suspend fun getUnSyncedPanels(): List<Panel> {
        return panelsDao.getUnSyncedPanels()
    }
}