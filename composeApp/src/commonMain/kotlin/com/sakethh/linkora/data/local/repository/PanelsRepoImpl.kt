package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.data.local.dao.PanelsDao
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.repository.local.PanelsRepo
import kotlinx.coroutines.flow.Flow

class PanelsRepoImpl(private val panelsDao: PanelsDao) : PanelsRepo {
    override suspend fun addaNewPanel(panel: Panel) {
        panelsDao.addaNewPanel(panel)
    }

    override suspend fun deleteAPanel(id: Long) {
        panelsDao.deleteAPanel(id)
        panelsDao.deleteConnectedFoldersOfPanel(id)
    }

    override suspend fun updateAPanelName(newName: String, panelId: Long) {
        panelsDao.updateAPanelName(newName, panelId)
    }

    override suspend fun addANewFolderInAPanel(panelFolder: PanelFolder) {
        panelsDao.addANewFolderInAPanel(panelFolder)
    }

    override suspend fun deleteAFolderFromAllPanels(folderID: Long) {
        panelsDao.deleteAFolderFromAllPanels(folderID)
    }

    override suspend fun getLatestPanelID(): Long {
        return panelsDao.getLatestPanelID()
    }
    override suspend fun deleteAFolderFromAPanel(panelId: Long, folderID: Long) {
        panelsDao.deleteAFolderFromAPanel(panelId, folderID)
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