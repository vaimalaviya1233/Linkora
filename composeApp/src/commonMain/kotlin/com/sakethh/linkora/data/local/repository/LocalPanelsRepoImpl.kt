package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.PanelsDao
import com.sakethh.linkora.domain.SyncServerRoute
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
import com.sakethh.linkora.utils.getSystemEpochSeconds
import com.sakethh.linkora.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.utils.updateLastSyncedWithServerTimeStamp
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.json.Json

class LocalPanelsRepoImpl(
    private val panelsDao: PanelsDao,
    private val remotePanelsRepo: RemotePanelsRepo,
    private val foldersDao: FoldersDao,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val preferencesRepository: PreferencesRepository
) : LocalPanelsRepo {
    override suspend fun addaNewPanel(panel: Panel, viaSocket: Boolean): Flow<Result<Long>> {
        var newPanelId: Long? = null
        val eventTimestamp = getSystemEpochSeconds()
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
                        operation = SyncServerRoute.ADD_A_NEW_PANEL.name,
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
            newPanelId
        }
    }

    override suspend fun deleteAPanel(id: Long, viaSocket: Boolean): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(id)
        val eventTimestamp = getSystemEpochSeconds()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                require(remotePanelId != null)
                remotePanelsRepo.deleteAPanel(
                    IDBasedDTO(
                        id = remotePanelId, eventTimestamp = getSystemEpochSeconds()
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (remotePanelId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = SyncServerRoute.DELETE_A_PANEL.name,
                            payload = Json.encodeToString(
                                IDBasedDTO(
                                    id = remotePanelId, eventTimestamp = eventTimestamp
                                )
                            )
                        )
                    )
                }
            }) {
            panelsDao.deleteAPanel(id)
            panelsDao.deleteConnectedFoldersOfPanel(id)
        }
    }

    override suspend fun updateAPanelName(
        newName: String, panelId: Long, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(panelId)
        val eventTimestamp = getSystemEpochSeconds()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = !viaSocket,
            remoteOperation = {
                require(remotePanelId != null)
                remotePanelsRepo.updateAPanelName(
                    UpdatePanelNameDTO(
                        newName, remotePanelId, eventTimestamp = eventTimestamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                panelsDao.updatePanelTimestamp(panelId, it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = SyncServerRoute.UPDATE_A_PANEL_NAME.name,
                        payload = Json.encodeToString(
                            UpdatePanelNameDTO(
                                newName, panelId, eventTimestamp = eventTimestamp
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
        val remoteIdOfFolder = foldersDao.getRemoteFolderId(panelFolder.folderId)
        val remoteIdOfConnectedPanel = panelsDao.getRemoteIdOfPanel(panelFolder.connectedPanelId)
        val eventTimestamp = getSystemEpochSeconds()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (newPanelFolderId == null) return@performLocalOperationWithRemoteSyncFlow emptyFlow()
                require(remoteIdOfFolder != null && remoteIdOfConnectedPanel != null)
                remotePanelsRepo.addANewFolderInAPanel(
                    AddANewPanelFolderDTO(
                        folderId = remoteIdOfFolder,
                        panelPosition = panelFolder.panelPosition,
                        folderName = panelFolder.folderName,
                        connectedPanelId = remoteIdOfConnectedPanel,
                        eventTimestamp = eventTimestamp
                    )
                )
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
                        operation = SyncServerRoute.ADD_A_NEW_FOLDER_IN_A_PANEL.name,
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
        val remoteFolderId = foldersDao.getRemoteFolderId(folderID)
        val eventTimestamp = getSystemEpochSeconds()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                require(remotePanelId != null && remoteFolderId != null)
                remotePanelsRepo.deleteAFolderFromAPanel(
                    DeleteAFolderFromAPanelDTO(
                        panelId = remotePanelId,
                        folderID = remoteFolderId,
                        eventTimestamp = eventTimestamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (remotePanelId != null && remoteFolderId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = SyncServerRoute.DELETE_A_FOLDER_FROM_A_PANEL.name,
                            payload = Json.encodeToString(
                                DeleteAFolderFromAPanelDTO(
                                    panelId = remotePanelId,
                                    folderID = remoteFolderId,
                                    eventTimestamp = eventTimestamp
                                )
                            )
                        )
                    )
                }
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

    override suspend fun getPanelFolder(localId: Long): PanelFolder {
        return panelsDao.getPanelFolder(localId)
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