package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.utils.postFlow
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.server.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNoteDTO
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

    override suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.DELETE_FOLDER.name,
            body = idBasedDTO,
        )
    }

    override suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name,
            body = idBasedDTO
        )
    }

    override suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name,
            body = idBasedDTO
        )
    }

    override suspend fun changeParentFolder(
        changeParentFolderDTO: ChangeParentFolderDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.CHANGE_PARENT_FOLDER.name,
            body = changeParentFolderDTO
        )
    }

    override suspend fun updateFolderName(
        updateFolderNameDTO: UpdateFolderNameDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.UPDATE_FOLDER_NAME.name,
            body = updateFolderNameDTO
        )
    }

    override suspend fun updateFolderNote(
        updateFolderNoteDTO: UpdateFolderNoteDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name,
            body = updateFolderNoteDTO
        )
    }

    override suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.DELETE_FOLDER_NOTE.name,
            body = idBasedDTO
        )
    }
}