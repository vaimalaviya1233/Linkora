package com.sakethh.linkora.data.local.dao.sorting

import androidx.room.Dao
import androidx.room.Query
import com.sakethh.linkora.domain.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FoldersSortingDao {
    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderId ORDER BY name COLLATE NOCASE ASC")
    fun sortByAToZ(parentFolderId: Long?): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderId ORDER BY name COLLATE NOCASE DESC")
    fun sortByZToA(parentFolderId: Long?): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderId ORDER BY localId COLLATE NOCASE DESC")
    fun sortByLatestToOldest(parentFolderId: Long?): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderId ORDER BY localId COLLATE NOCASE ASC")
    fun sortByOldestToLatest(parentFolderId: Long?): Flow<List<Folder>>
}