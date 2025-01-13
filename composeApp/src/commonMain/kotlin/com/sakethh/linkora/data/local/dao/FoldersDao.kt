package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sakethh.linkora.common.utils.Sorting
import com.sakethh.linkora.domain.model.Folder
import kotlinx.coroutines.flow.Flow

@Dao
interface FoldersDao {
    @Insert
    suspend fun insertANewFolder(folders: Folder): Long

    @Query("INSERT INTO folders(name, note,parentFolderID,isArchived) SELECT name, note, :parentFolderID, isArchived FROM folders WHERE localId= :actualFolderId")
    suspend fun duplicateAFolder(actualFolderId: Long, parentFolderID: Long?): Long

    @Insert
    suspend fun insertMultipleNewFolders(foldersTable: List<Folder>)

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL AND isArchived = 1")
    fun getAllArchiveFoldersAsFlow(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL AND isArchived = 1")
    suspend fun getAllArchiveFoldersAsList(): List<Folder>

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL AND isArchived = 0")
    fun getAllRootFoldersAsFlow(): Flow<List<Folder>>

    @Query("SELECT * FROM folders WHERE parentFolderID IS NULL")
    suspend fun getAllRootFoldersAsList(): List<Folder>

    @Query("SELECT * FROM folders")
    suspend fun getAllFolders(): List<Folder>

    @Query("SELECT localId FROM folders WHERE localId = (SELECT MAX(localId) FROM folders)")
    suspend fun getLatestFoldersTableID(): Long

    @Query("SELECT COUNT(*) FROM links WHERE idOfLinkedFolder = :folderID")
    suspend fun getSizeOfLinksOfThisFolder(folderID: Long): Int

    @Query("SELECT * FROM folders WHERE localId = :folderID")
    suspend fun getThisFolderData(folderID: Long): Folder

    @Query("SELECT MAX(localId) FROM folders")
    suspend fun getLastIDOfFoldersTable(): Long

    @Query("SELECT COUNT(*) FROM folders WHERE name = :folderName AND parentFolderID = :parentFolderID")
    suspend fun doesThisChildFolderExists(folderName: String, parentFolderID: Long?): Int

    @Query("SELECT COUNT(*) FROM folders WHERE name = :folderName AND parentFolderID IS NULL")
    suspend fun doesThisRootFolderExists(folderName: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM folders WHERE localId = :folderID AND isArchived = 1)")
    suspend fun isThisFolderMarkedAsArchive(folderID: Long): Boolean

    @Query("SELECT * FROM folders ORDER BY localId DESC LIMIT 1")
    suspend fun getNewestFolder(): Folder

    @Query("SELECT COUNT(localId) FROM folders")
    fun getFoldersCount(): Flow<Int>

    @Query("UPDATE folders SET parentFolderID = :targetParentId WHERE localId IN (:sourceFolderIds)")
    suspend fun changeTheParentIdOfASpecificFolder(
        sourceFolderIds: List<Long>,
        targetParentId: Long?
    )

    @Query("UPDATE folders SET parentFolderID = :targetParentId WHERE localId = :sourceFolderId")
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

    @Query("UPDATE folders SET name = :newFolderName WHERE localId = :folderID")
    suspend fun renameAFolderName(folderID: Long, newFolderName: String)


    @Query("UPDATE folders SET isArchived = 1 WHERE localId=:folderID")
    suspend fun markFolderAsArchive(folderID: Long)

    @Query("UPDATE folders SET isArchived = 1 WHERE localId in (:folderIDs)")
    suspend fun markMultipleFoldersAsArchive(folderIDs: Array<Long>)

    @Query("UPDATE folders SET isArchived = 0 WHERE localId=:folderID")
    suspend fun markFolderAsRegularFolder(folderID: Long)


    @Query("UPDATE folders SET note = :newNote WHERE localId = :folderID")
    suspend fun renameAFolderNote(folderID: Long, newNote: String)

    @Update
    suspend fun updateAFolderData(foldersTable: Folder)

    @Query("UPDATE folders SET note = \"\" WHERE localId = :folderID")
    suspend fun deleteAFolderNote(folderID: Long)

    @Query("DELETE from folders WHERE localId = :folderID")
    suspend fun deleteAFolder(folderID: Long)

    @Query("DELETE from folders WHERE parentFolderID = :parentFolderId")
    suspend fun deleteChildFoldersOfThisParentID(parentFolderId: Long)

    @Query("SELECT (SELECT COUNT(*) FROM folders) == 0")
    suspend fun isFoldersTableEmpty(): Boolean

    @Query(
        """
    SELECT * FROM folders 
    WHERE parentFolderID = :parentFolderId
    ORDER BY 
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN localId END ASC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN localId END DESC,
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN name COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN name COLLATE NOCASE END DESC
    """
    )
    fun sortFolders(
        parentFolderId: Long,
        sortOption: String
    ): Flow<List<Folder>>


    @Query(
        """
    SELECT * FROM folders 
    WHERE parentFolderID IS NULL
    ORDER BY 
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN localId END ASC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN localId END DESC,
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN name COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN name COLLATE NOCASE END DESC
    """
    )
    fun sortFolders(sortOption: String): Flow<List<Folder>>

    @Query(
        "SELECT * FROM folders \n" +
                "    WHERE (LOWER(name) LIKE '%' || LOWER(:query) || '%' \n" +
                "           OR LOWER(note) LIKE '%' || LOWER(:query) || '%') \n" +
                "    ORDER BY \n" +
                "        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN name COLLATE NOCASE END ASC,\n" +
                "        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN name COLLATE NOCASE END DESC,\n" +
                "        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN localId END DESC,\n" +
                "        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN localId END ASC"
    )
    fun search(query: String, sortOption: String): Flow<List<Folder>>

    @Query("DELETE FROM folders")
    suspend fun deleteAllFolders()
}