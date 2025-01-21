package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import kotlinx.coroutines.flow.Flow

interface LocalPanelsRepo {
    suspend fun addaNewPanel(panel: Panel): Flow<Result<Unit>>
    suspend fun deleteAPanel(id: Long): Flow<Result<Unit>>
    suspend fun updateAPanelName(newName: String, panelId: Long): Flow<Result<Unit>>
    suspend fun addANewFolderInAPanel(panelFolder: PanelFolder): Flow<Result<Unit>>
    suspend fun deleteAFolderFromAllPanels(folderID: Long)
    suspend fun deleteAFolderFromAPanel(panelId: Long, folderID: Long): Flow<Result<Unit>>
    fun getAllThePanels(): Flow<List<Panel>>
    suspend fun getAllThePanelsAsAList(): List<Panel>
    suspend fun getAllThePanelFoldersAsAList(): List<PanelFolder>
    fun getAllTheFoldersFromAPanel(panelId: Long): Flow<List<PanelFolder>>
    suspend fun getPanel(panelId: Long): Panel

    suspend fun addMultiplePanels(panels: List<Panel>)
    suspend fun addMultiplePanelFolders(panelFolders: List<PanelFolder>)

    suspend fun deleteAllPanels()
    suspend fun deleteAllPanelFolders()
    suspend fun getLatestPanelID(): Long
}