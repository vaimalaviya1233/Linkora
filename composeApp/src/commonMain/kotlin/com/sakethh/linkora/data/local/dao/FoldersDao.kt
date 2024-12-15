package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sakethh.linkora.domain.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FoldersDao {
    @Insert
    suspend fun insertANewFolder(folder: Folder): Long

    @Query("INSERT INTO folder(name, note,parentFolderID,isArchived) SELECT name, note, :parentFolderID, isArchived FROM folder WHERE id= :actualFolderId")
    suspend fun duplicateAFolder(actualFolderId: Long, parentFolderID: Long?): Long

    @Insert
    suspend fun insertMultipleNewFolders(foldersTable: List<Folder>)

    @Query("SELECT * FROM folder WHERE parentFolderID IS NULL AND isArchived = 1")
    fun getAllArchiveFoldersAsFlow(): Flow<List<Folder>>

    @Query("SELECT * FROM folder WHERE parentFolderID IS NULL AND isArchived = 1")
    suspend fun getAllArchiveFoldersAsList(): List<Folder>

    @Query("SELECT * FROM folder WHERE parentFolderID IS NULL AND isArchived = 0")
    fun getAllRootFoldersAsFlow(): Flow<List<Folder>>

    @Query("SELECT * FROM folder WHERE parentFolderID IS NULL AND isArchived = 0")
    suspend fun getAllRootFoldersAsList(): List<Folder>

    @Query("SELECT * FROM folder")
    suspend fun getAllFolders(): List<Folder>

    @Query("SELECT COUNT(*) FROM link WHERE isLinkedWithFolders=1 AND idOfLinkedFolder = :folderID")
    suspend fun getSizeOfLinksOfThisFolder(folderID: Long): Int

    @Query("SELECT * FROM folder WHERE id = :folderID")
    suspend fun getThisFolderData(folderID: Long): Folder

    @Query("SELECT MAX(id) FROM folder")
    suspend fun getLastIDOfFoldersTable(): Long

    @Query("SELECT COUNT(*) FROM folder WHERE name = :folderName AND parentFolderID = :parentFolderID")
    suspend fun doesThisChildFolderExists(folderName: String, parentFolderID: Long?): Int

    @Query("SELECT COUNT(*) FROM folder WHERE name = :folderName AND parentFolderID IS NULL")
    suspend fun doesThisRootFolderExists(folderName: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM folder WHERE id = :folderID AND isArchived = 1)")
    suspend fun isThisFolderMarkedAsArchive(folderID: Long): Boolean

    @Query("SELECT * FROM folder ORDER BY id DESC LIMIT 1")
    suspend fun getNewestFolder(): Folder

    @Query("SELECT COUNT(id) FROM folder")
    fun getFoldersCount(): Flow<Int>

    @Query("UPDATE folder SET parentFolderID = :targetParentId WHERE id IN (:sourceFolderId)")
    suspend fun changeTheParentIdOfASpecificFolder(
        sourceFolderId: List<Long>,
        targetParentId: Long?
    )

    @Query("SELECT * FROM folder WHERE parentFolderID = :parentFolderID AND isArchived = 0")
    fun getChildFoldersOfThisParentIDAsFlow(parentFolderID: Long?): Flow<List<Folder>>

    @Query("SELECT * FROM folder WHERE parentFolderID = :parentFolderID AND isArchived = 0")
    suspend fun getChildFoldersOfThisParentIDAsAList(parentFolderID: Long?): List<Folder>

    @Query("SELECT COUNT(*) FROM folder WHERE parentFolderID = :parentFolderID")
    suspend fun getSizeOfChildFoldersOfThisParentID(parentFolderID: Long?): Int

    @Query("UPDATE folder SET name = :newFolderName WHERE id = :folderID")
    suspend fun renameAFolderName(folderID: Long, newFolderName: String)


    @Query("UPDATE folder SET isArchived = 1 WHERE id=:folderID")
    suspend fun markFolderAsArchive(folderID: Long)

    @Query("UPDATE folder SET isArchived = 1 WHERE id in (:folderIDs)")
    suspend fun markMultipleFoldersAsArchive(folderIDs: Array<Long>)

    @Query("UPDATE folder SET isArchived = 0 WHERE id=:folderID")
    suspend fun markFolderAsRegularFolder(folderID: Long)


    @Query("UPDATE folder SET note = :newNote WHERE id = :folderID")
    suspend fun renameAFolderNote(folderID: Long, newNote: String)

    @Update
    suspend fun updateAFolderData(foldersTable: Folder)

    @Query("UPDATE folder SET note = \"\" WHERE id = :folderID")
    suspend fun deleteAFolderNote(folderID: Long)

    @Query("DELETE from folder WHERE id = :folderID")
    suspend fun deleteAFolder(folderID: Long)

    @Query("DELETE from folder WHERE parentFolderID = :parentFolderId")
    suspend fun deleteChildFoldersOfThisParentID(parentFolderId: Long)

    @Query("SELECT (SELECT COUNT(*) FROM folder) == 0")
    suspend fun isFoldersTableEmpty(): Boolean
}