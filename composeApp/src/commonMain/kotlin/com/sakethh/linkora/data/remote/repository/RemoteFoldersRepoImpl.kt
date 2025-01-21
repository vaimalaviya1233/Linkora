package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.utils.postFlow
import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.AddFolderDTO
import com.sakethh.linkora.domain.dto.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.repository.remote.RemoteFoldersRepo
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class RemoteFoldersRepoImpl(
    private val httpClient: HttpClient,
    private val baseUrl: () -> String,
    private val authToken: () -> String
) : RemoteFoldersRepo {

    override suspend fun createFolder(addFolderDTO: AddFolderDTO): Flow<Result<NewItemResponseDTO>> {
        return postFlow<AddFolderDTO, NewItemResponseDTO>(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Folder.CREATE_FOLDER.name,
            body = addFolderDTO,
        )
    }

    override suspend fun deleteFolder(folderId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.DELETE_FOLDER.name,
            body = folderId,
        )
    }

    override suspend fun markAsArchive(folderId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Folder.MARK_AS_ARCHIVE.name,
            body = folderId.toString()
        )
    }

    override suspend fun markAsRegularFolder(folderId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name,
            body = folderId,
        )
    }

    override suspend fun changeParentFolder(
        folderId: Long, newParentFolderId: Long?
    ): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.CHANGE_PARENT_FOLDER.name,
            body = ChangeParentFolderDTO(folderId, newParentFolderId),
        )
    }

    override suspend fun updateFolderName(
        folderId: Long, newFolderName: String
    ): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.UPDATE_FOLDER_NAME.name,
            body = UpdateFolderNameDTO(folderId, newFolderName),
        )
    }

    override suspend fun updateFolderNote(folderId: Long, newNote: String): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name,
            body = UpdateFolderNoteDTO(folderId, newNote),
        )
    }

    override suspend fun deleteFolderNote(folderId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.DELETE_FOLDER_NOTE.name,
            body = folderId,
        )
    }
}