package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import kotlinx.coroutines.flow.Flow

interface RemoteFoldersRepo {
    suspend fun createFolder(folder: Folder): Flow<Result<Message>>
    suspend fun deleteFolder(folderId: Long): Flow<Result<Message>>
    suspend fun getChildFolders(parentFolderId: Long): Flow<Result<List<Folder>>>
    suspend fun getRootFolders(): Flow<Result<List<Folder>>>
    suspend fun markAsArchive(folderId: Long): Flow<Result<Message>>
    suspend fun markAsRegularFolder(folderId: Long): Flow<Result<Message>>
    suspend fun changeParentFolder(folderId: Long, newParentFolderId: Long): Flow<Result<Message>>
    suspend fun updateFolderName(folderId: Long, newFolderName: String): Flow<Result<Message>>

    suspend fun updateFolderNote(folderId: Long, newNote: String): Flow<Result<Message>>
    suspend fun deleteFolderNote(folderId: Long): Flow<Result<Message>>
}