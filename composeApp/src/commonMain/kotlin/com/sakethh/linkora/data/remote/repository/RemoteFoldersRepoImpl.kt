package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.utils.postFlow
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.server.folder.ChangeParentFolderDTO
import com.sakethh.linkora.domain.dto.server.folder.MarkSelectedFoldersAsRootDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.repository.remote.RemoteFoldersRepo
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class RemoteFoldersRepoImpl(
    private val syncServerClient:()-> HttpClient,
    private val baseUrl: () -> String,
    private val authToken: () -> String
) : RemoteFoldersRepo {

    override suspend fun createFolder(addFolderDTO: AddFolderDTO): Flow<Result<NewItemResponseDTO>> {
        return postFlow<AddFolderDTO, NewItemResponseDTO>(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Folder.CREATE_FOLDER.name,
            outgoingBody = addFolderDTO,
        )
    }

    override suspend fun deleteFolder(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.DELETE_FOLDER.name,
            outgoingBody = idBasedDTO,
        )
    }

    override suspend fun markAsArchive(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name,
            outgoingBody = idBasedDTO
        )
    }

    override suspend fun markAsRegularFolder(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name,
            outgoingBody = idBasedDTO
        )
    }

    override suspend fun changeParentFolder(
        changeParentFolderDTO: ChangeParentFolderDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.CHANGE_PARENT_FOLDER.name,
            outgoingBody = changeParentFolderDTO
        )
    }

    override suspend fun updateFolderName(
        updateFolderNameDTO: UpdateFolderNameDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.UPDATE_FOLDER_NAME.name,
            outgoingBody = updateFolderNameDTO
        )
    }

    override suspend fun updateFolderNote(
        updateFolderNoteDTO: UpdateFolderNoteDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name,
            outgoingBody = updateFolderNoteDTO
        )
    }

    override suspend fun deleteFolderNote(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken, endPoint = RemoteRoute.Folder.DELETE_FOLDER_NOTE.name,
            outgoingBody = idBasedDTO
        )
    }

    override suspend fun markSelectedFoldersAsRoot(markSelectedFoldersAsRootDTO: MarkSelectedFoldersAsRootDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Folder.MARK_FOLDERS_AS_ROOT.name,
            outgoingBody = markSelectedFoldersAsRootDTO
        )
    }
}