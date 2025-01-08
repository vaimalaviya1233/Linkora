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
    suspend fun insertANewFolder(folders: Folder): Long

    @Query("INSERT INTO folders(name, note,parentFolderID,isArchived) SELECT name, note, :parentFolderID, isArchived FROM folders WHERE id= :actualFolderId")
    suspend fun duplicateAFolder(actualFolderId: Long, parentFolderID: Long?): Long

    @Insert
    suspend fun insertMultipleNewFolders(foldersTable: List<Folder>)

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL AND isArchived = 1")
    fun getAllArchiveFoldersAsFlow(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL AND isArchived = 1")
    suspend fun getAllArchiveFoldersAsList(): List<Folder>

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL")
    fun getAllRootFoldersAsFlow(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL")
    suspend fun getAllRootFoldersAsList(): List<Folder>

    @Query("SELECT * FROM folders")
    suspend fun getAllFolders(): List<Folder>

    @Query("SELECT COUNT(*) FROM links WHERE idOfLinkedFolder = :folderID")
    suspend fun getSizeOfLinksOfThisFolder(folderID: Long): Int

    @Query("SELECT * FROM folders WHERE id = :folderID")
    suspend fun getThisFolderData(folderID: Long): Folder

    @Query("SELECT MAX(id) FROM folders")
    suspend fun getLastIDOfFoldersTable(): Long

    @Query("SELECT COUNT(*) FROM folders WHERE name = :folderName AND parentFolderID = :parentFolderID")
    suspend fun doesThisChildFolderExists(folderName: String, parentFolderID: Long?): Int

    @Query("SELECT COUNT(*) FROM folders WHERE name = :folderName AND parentFolderID IS NULL")
    suspend fun doesThisRootFolderExists(folderName: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM folders WHERE id = :folderID AND isArchived = 1)")
    suspend fun isThisFolderMarkedAsArchive(folderID: Long): Boolean

    @Query("SELECT * FROM folders ORDER BY id DESC LIMIT 1")
    suspend fun getNewestFolder(): Folder

    @Query("SELECT COUNT(id) FROM folders")
    fun getFoldersCount(): Flow<Int>

    @Query("UPDATE folders SET parentFolderID = :targetParentId WHERE id IN (:sourceFolderIds)")
    suspend fun changeTheParentIdOfASpecificFolder(
        sourceFolderIds: List<Long>,
        targetParentId: Long?
    )

    @Query("UPDATE folders SET parentFolderID = :targetParentId WHERE id = :sourceFolderId")
    suspend fun changeTheParentIdOfASpecificFolder(
        sourceFolderId: Long,
        targetParentId: Long?
    )

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderID")
    fun getChildFoldersOfThisParentIDAsFlow(parentFolderID: Long?): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID = :parentFolderID")
    suspend fun getChildFoldersOfThisParentIDAsAList(parentFolderID: Long?): List<Folder>

    @Query("SELECT COUNT(*) FROM folders WHERE parentFolderID = :parentFolderID")
    suspend fun getSizeOfChildFoldersOfThisParentID(parentFolderID: Long?): Int

    @Query("UPDATE folders SET name = :newFolderName WHERE id = :folderID")
    suspend fun renameAFolderName(folderID: Long, newFolderName: String)


    @Query("UPDATE folders SET isArchived = 1 WHERE id=:folderID")
    suspend fun markFolderAsArchive(folderID: Long)

    @Query("UPDATE folders SET isArchived = 1 WHERE id in (:folderIDs)")
    suspend fun markMultipleFoldersAsArchive(folderIDs: Array<Long>)

    @Query("UPDATE folders SET isArchived = 0 WHERE id=:folderID")
    suspend fun markFolderAsRegularFolder(folderID: Long)


    @Query("UPDATE folders SET note = :newNote WHERE id = :folderID")
    suspend fun renameAFolderNote(folderID: Long, newNote: String)

    @Update
    suspend fun updateAFolderData(foldersTable: Folder)

    @Query("UPDATE folders SET note = \"\" WHERE id = :folderID")
    suspend fun deleteAFolderNote(folderID: Long)

    @Query("DELETE from folders WHERE id = :folderID")
    suspend fun deleteAFolder(folderID: Long)

    @Query("DELETE from folders WHERE parentFolderID = :parentFolderId")
    suspend fun deleteChildFoldersOfThisParentID(parentFolderId: Long)

    @Query("SELECT (SELECT COUNT(*) FROM folders) == 0")
    suspend fun isFoldersTableEmpty(): Boolean
}