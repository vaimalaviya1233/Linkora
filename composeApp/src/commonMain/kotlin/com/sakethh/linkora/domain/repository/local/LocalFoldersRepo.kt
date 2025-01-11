package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import kotlinx.coroutines.flow.Flow


interface LocalFoldersRepo {
    suspend fun insertANewFolder(
        folder: Folder,
        ignoreFolderAlreadyExistsException: Boolean
    ): Flow<Result<Message>>

    suspend fun duplicateAFolder(actualFolderId: Long, parentFolderID: Long?): Flow<Result<Long>>

    suspend fun insertMultipleNewFolders(foldersTable: List<Folder>): Flow<Result<Unit>>

    suspend fun getAllArchiveFoldersAsList(): Flow<Result<List<Folder>>>

    suspend fun getAllRootFoldersAsList(): Flow<Result<List<Folder>>>

    suspend fun getAllFolders(): Flow<Result<List<Folder>>>

    suspend fun getSizeOfLinksOfThisFolder(folderID: Long): Flow<Result<Int>>

    suspend fun getThisFolderData(folderID: Long): Flow<Result<Folder>>

    suspend fun getLastIDOfFoldersTable(): Flow<Result<Long>>

    suspend fun doesThisChildFolderExists(
        folderName: String,
        parentFolderID: Long?
    ): Flow<Result<Int>>

    suspend fun doesThisRootFolderExists(folderName: String): Flow<Result<Boolean>>

    suspend fun isThisFolderMarkedAsArchive(folderID: Long): Flow<Result<Boolean>>

    suspend fun getNewestFolder(): Flow<Result<Folder>>

    fun getFoldersCount(): Flow<Result<Int>>

    suspend fun changeTheParentIdOfASpecificFolder(
        sourceFolderId: Long,
        targetParentId: Long?
    ): Flow<Result<Unit>>

    suspend fun sortFolders(sortOption: String): Flow<Result<List<Folder>>>

    suspend fun sortFolders(
        parentFolderId: Long,
        sortOption: String
    ): Flow<Result<List<Folder>>>

    fun sortFoldersAsNonResultFlow(
        parentFolderId: Long,
        sortOption: String
    ): Flow<List<Folder>>


    suspend fun getChildFoldersOfThisParentIDAsAList(parentFolderID: Long?): Flow<Result<List<Folder>>>

    suspend fun getSizeOfChildFoldersOfThisParentID(parentFolderID: Long?): Flow<Result<Int>>

    suspend fun renameAFolderName(folderID: Long, existingFolderName:String, newFolderName: String,ignoreFolderAlreadyExistsException:Boolean): Flow<Result<Unit>>


    suspend fun markFolderAsArchive(folderID: Long): Flow<Result<Unit>>

    suspend fun markMultipleFoldersAsArchive(folderIDs: Array<Long>): Flow<Result<Unit>>

    suspend fun markFolderAsRegularFolder(folderID: Long): Flow<Result<Unit>>

    suspend fun renameAFolderNote(folderID: Long, newNote: String): Flow<Result<Unit>>

    suspend fun updateAFolderData(folder: Folder): Flow<Result<Unit>>

    suspend fun deleteAFolderNote(folderID: Long): Flow<Result<Unit>>

    suspend fun deleteAFolder(folderID: Long): Flow<Result<Unit>>

    suspend fun deleteChildFoldersOfThisParentID(parentFolderId: Long): Flow<Result<Unit>>

    suspend fun isFoldersTableEmpty(): Flow<Result<Boolean>>

    fun search(query: String, sortOption: String): Flow<Result<List<Folder>>>
}