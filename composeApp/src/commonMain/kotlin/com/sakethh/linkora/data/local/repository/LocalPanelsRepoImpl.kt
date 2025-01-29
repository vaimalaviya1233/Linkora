package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.PanelsDao
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.DeleteAPanelFromAFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.remote.RemotePanelsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class LocalPanelsRepoImpl(
    private val panelsDao: PanelsDao,
    private val remotePanelsRepo: RemotePanelsRepo,
    private val foldersDao: FoldersDao,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo
) : LocalPanelsRepo {
    override suspend fun addaNewPanel(panel: Panel, viaSocket: Boolean): Flow<Result<Unit>> {
        val newPanelId = panelsDao.getLatestPanelID() + 1
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                remotePanelsRepo.addANewPanel(AddANewPanelDTO(panel.panelName))
            },
            remoteOperationOnSuccess = {
                panelsDao.updateAPanel(panelsDao.getPanel(newPanelId).copy(remoteId = it.id))
            }, onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Panel.ADD_A_NEW_PANEL.name,
                        payload = Json.encodeToString(
                            AddANewPanelDTO(panel.panelName, offlineSyncItemId = newPanelId)
                        )
                    )
                )
            }) {
            panelsDao.addaNewPanel(panel.copy(localId = newPanelId))
        }
    }

    override suspend fun deleteAPanel(id: Long, viaSocket: Boolean): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(id)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(), remoteOperation = {
                if (remotePanelId != null) {
                    remotePanelsRepo.deleteAPanel(remotePanelId)
                    // server already handles this internally, so no need to push it externally: remotePanelsRepo.deleteAllFoldersFromAPanel(remotePanelId)
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Panel.DELETE_A_PANEL.name,
                            payload = Json.encodeToString(
                                IDBasedDTO(
                                    id = id
                                )
                            )
                        )
                    )
            }) {
            panelsDao.deleteAPanel(id)
            panelsDao.deleteConnectedFoldersOfPanel(id)
        }
    }

    override suspend fun updateAPanelName(
        newName: String,
        panelId: Long,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(panelId)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(), remoteOperation = {
                if (remotePanelId != null) {
                    remotePanelsRepo.updateAPanelName(UpdatePanelNameDTO(newName, remotePanelId))
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name,
                            payload = Json.encodeToString(
                                UpdatePanelNameDTO(
                                    newName, panelId
                                )
                            )
                        )
                    )
            }) {
            panelsDao.updateAPanelName(newName, panelId)
        }
    }

    override suspend fun updatePanel(panel: Panel) {
        panelsDao.updateAPanel(panel)
    }
    override suspend fun addANewFolderInAPanel(
        panelFolder: PanelFolder,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val newPanelFolderId = panelsDao.getLatestPanelFolderID() + 1
        val remoteIdOfFolder = foldersDao.getRemoteIdOfAFolder(panelFolder.folderId)
        val remoteIdOfConnectedPanel = panelsDao.getRemoteIdOfPanel(panelFolder.connectedPanelId)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteIdOfFolder != null && remoteIdOfConnectedPanel != null) {
                    remotePanelsRepo.addANewFolderInAPanel(
                        AddANewPanelFolderDTO(
                            folderId = remoteIdOfFolder,
                            panelPosition = panelFolder.panelPosition,
                            folderName = panelFolder.folderName,
                            connectedPanelId = remoteIdOfConnectedPanel
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                panelsDao.updateAPanelFolder(
                    panelsDao.getPanelFolder(newPanelFolderId).copy(remoteId = it.id)
                )
            }, onRemoteOperationFailure = {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name,
                            payload = Json.encodeToString(
                                AddANewPanelFolderDTO(
                                    folderId = panelFolder.folderId,
                                    panelPosition = panelFolder.panelPosition,
                                    folderName = panelFolder.folderName,
                                    connectedPanelId = panelFolder.connectedPanelId,
                                    offlineSyncItemId = newPanelFolderId
                                )
                            )
                        )
                    )
            }) {
            panelsDao.addANewFolderInAPanel(panelFolder.copy(localId = newPanelFolderId))
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
        panelId: Long, folderID: Long,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remotePanelId = panelsDao.getRemoteIdOfPanel(panelId)
        val remoteFolderId = foldersDao.getRemoteIdOfAFolder(folderID)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(), remoteOperation = {
                if (remotePanelId != null && remoteFolderId != null) {
                    remotePanelsRepo.deleteAFolderFromAPanel(
                        DeleteAPanelFromAFolderDTO(
                            panelId = remotePanelId, folderID = remoteFolderId
                        )
                    )
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Panel.DELETE_A_FOLDER_FROM_A_PANEL.name,
                            payload = Json.encodeToString(
                                DeleteAPanelFromAFolderDTO(
                                    panelId = panelId, folderID = folderID
                                )
                            )
                        )
                    )
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
}