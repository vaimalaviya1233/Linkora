package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.utils.postFlow
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.DeleteAFolderFromAPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.repository.remote.RemotePanelsRepo
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class RemotePanelsRepoImpl(
    private val syncServerClient:()-> HttpClient,
    private val baseUrl: () -> String,
    private val authToken: () -> String
) : RemotePanelsRepo {
    override suspend fun addANewPanel(addANewPanelDTO: AddANewPanelDTO): Flow<Result<NewItemResponseDTO>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Panel.ADD_A_NEW_PANEL.name,
            body = addANewPanelDTO
        )
    }

    override suspend fun addANewFolderInAPanel(addANewPanelFolderDTO: AddANewPanelFolderDTO): Flow<Result<NewItemResponseDTO>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name,
            body = addANewPanelFolderDTO
        )
    }

    override suspend fun deleteAPanel(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Panel.DELETE_A_PANEL.name,
            body = idBasedDTO
        )
    }

    override suspend fun updateAPanelName(updatePanelNameDTO: UpdatePanelNameDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name,
            body = updatePanelNameDTO
        )
    }

    override suspend fun deleteAFolderFromAllPanels(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Panel.DELETE_A_FOLDER_FROM_ALL_PANELS.name,
            body = idBasedDTO
        )
    }

    override suspend fun deleteAFolderFromAPanel(deleteAFolderFromAPanelDTO: DeleteAFolderFromAPanelDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Panel.DELETE_A_FOLDER_FROM_A_PANEL.name,
            body = deleteAFolderFromAPanelDTO
        )
    }
}