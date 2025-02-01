package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.folder.AddFolderDTO
import kotlinx.coroutines.flow.Flow

interface RemoteFoldersRepo {

    // these function names are the same as the ones on the server-side, just to make lookup easier

    suspend fun createFolder(addFolderDTO: AddFolderDTO): Flow<Result<NewItemResponseDTO>>
    suspend fun deleteFolder(folderId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun markAsArchive(folderId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun markAsRegularFolder(folderId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun changeParentFolder(
        folderId: Long,
        newParentFolderId: Long?
    ): Flow<Result<TimeStampBasedResponse>>

    suspend fun updateFolderName(
        folderId: Long,
        newFolderName: String
    ): Flow<Result<TimeStampBasedResponse>>

    suspend fun updateFolderNote(
        folderId: Long,
        newNote: String
    ): Flow<Result<TimeStampBasedResponse>>

    suspend fun deleteFolderNote(folderId: Long): Flow<Result<TimeStampBasedResponse>>
}