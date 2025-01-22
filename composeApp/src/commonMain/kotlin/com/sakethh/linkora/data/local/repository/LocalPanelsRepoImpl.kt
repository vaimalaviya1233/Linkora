package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.PanelsDao
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.DeleteAPanelFromAFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.remote.RemotePanelsRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

class LocalPanelsRepoImpl(
    private val panelsDao: PanelsDao,
    private val remotePanelsRepo: RemotePanelsRepo,
    private val foldersDao: FoldersDao
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
            }) {
            panelsDao.updateAPanelName(newName, panelId)
        }
    }

    override suspend fun addANewFolderInAPanel(
        panelFolder: PanelFolder,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val newPanelFolderId = panelsDao.getLatestPanelFolderID() + 1
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                val remoteIdOfFolder = foldersDao.getRemoteIdOfAFolder(panelFolder.folderId)
                val remoteIdOfConnectedPanel =
                    panelsDao.getRemoteIdOfPanel(panelFolder.connectedPanelId)
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
            }) {
            panelsDao.addANewFolderInAPanel(panelFolder.copy(localId = newPanelFolderId))
        }
    }

    override suspend fun deleteAFolderFromAllPanels(folderID: Long) {
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