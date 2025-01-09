package com.sakethh.linkora.data.local.dao.sorting

import androidx.room.Dao
import androidx.room.Query
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.domain.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FoldersSortingDao {

    fun sortByAToZ(parentFolderId: Long?): Flow<List<Folder>> {
        return if (parentFolderId.isNull()) {
            sortByAToZForRoot()
        } else {
            sortByAToZForNonRoot(parentFolderId!!)
        }
    }

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL ORDER BY name COLLATE NOCASE ASC")
    fun sortByAToZForRoot(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID =:parentFolderId ORDER BY name COLLATE NOCASE ASC")
    fun sortByAToZForNonRoot(parentFolderId: Long): Flow<List<Folder>>


    fun sortByZToA(parentFolderId: Long?): Flow<List<Folder>> {
        return if (parentFolderId.isNull()) {
            sortByZToAForRoot()
        } else {
            sortByZToAForNonRoot(parentFolderId!!)
        }
    }

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL ORDER BY name COLLATE NOCASE DESC")
    fun sortByZToAForRoot(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderId ORDER BY name COLLATE NOCASE DESC")
    fun sortByZToAForNonRoot(parentFolderId: Long): Flow<List<Folder>>


    fun sortByLatestToOldest(parentFolderId: Long?): Flow<List<Folder>> {
        return if (parentFolderId.isNull()) {
            sortByLatestToOldestForRoot()
        } else {
            sortByLatestToOldestForNonRoot(parentFolderId!!)
        }
    }


    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL ORDER BY localId DESC")
    fun sortByLatestToOldestForRoot(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderId ORDER BY localId DESC")
    fun sortByLatestToOldestForNonRoot(parentFolderId: Long): Flow<List<Folder>>


    fun sortByOldestToLatest(parentFolderId: Long?): Flow<List<Folder>> {
        return if (parentFolderId.isNull()) {
            sortByOldestToLatestForRoot()
        } else {
            sortByOldestToLatestForNonRoot(parentFolderId!!)
        }
    }

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL ORDER BY localId ASC")
    fun sortByOldestToLatestForRoot(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderId ORDER BY localId ASC")
    fun sortByOldestToLatestForNonRoot(parentFolderId: Long): Flow<List<Folder>>
}
