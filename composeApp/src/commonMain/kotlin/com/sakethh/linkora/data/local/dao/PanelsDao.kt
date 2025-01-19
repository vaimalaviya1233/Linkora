package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import kotlinx.coroutines.flow.Flow

@Dao
interface PanelsDao {
    @Query("SELECT MAX(panelId) FROM panel")
    suspend fun getLatestPanelID(): Long

    @Insert
    suspend fun addaNewPanel(panel: Panel)

    @Insert
    suspend fun addMultiplePanels(panels: List<Panel>)

    @Insert
    suspend fun addMultiplePanelFolders(panelFolders: List<PanelFolder>)

    @Query("DELETE FROM panel WHERE panelId = :id")
    suspend fun deleteAPanel(id: Long)

    @Query("UPDATE panel SET panelName = :newName WHERE panelId = :panelId")
    suspend fun updateAPanelName(newName: String, panelId: Long)

    @Query("DELETE FROM panel_folder WHERE connectedPanelId = :panelId")
    suspend fun deleteConnectedFoldersOfPanel(panelId: Long)

    @Insert
    suspend fun addANewFolderInAPanel(panelFolder: PanelFolder)

    @Query("DELETE FROM panel_folder WHERE connectedPanelId = :panelId AND folderId = :folderID ")
    suspend fun deleteAFolderFromAPanel(panelId: Long, folderID: Long)

    @Query("DELETE FROM panel_folder WHERE folderId = :folderID")
    suspend fun deleteAFolderFromAllPanels(folderID: Long)

    @Query("SELECT * FROM panel")
    fun getAllThePanels(): Flow<List<Panel>>

    @Query("SELECT * FROM panel")
    suspend fun getAllThePanelsAsAList(): List<Panel>

    @Query("SELECT * FROM panel_folder")
    suspend fun getAllThePanelFoldersAsAList(): List<PanelFolder>

    @Query("SELECT * FROM panel_folder WHERE connectedPanelId = :panelId")
    fun getAllTheFoldersFromAPanel(panelId: Long): Flow<List<PanelFolder>>

    @Query("DELETE FROM panel")
    suspend fun deleteAllPanels()

    @Query("DELETE FROM panel_folder")
    suspend fun deleteAllPanelFolders()

    @Query("SELECT * FROM panel WHERE panelId=:panelId LIMIT 1") // there will always be only 1 panel with the given ID, but added `LIMIT 1` because why not.
    suspend fun getPanel(panelId: Long): Panel
}