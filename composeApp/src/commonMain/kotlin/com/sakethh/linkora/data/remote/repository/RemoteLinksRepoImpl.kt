package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.utils.postFlow
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.link.AddLinkDTO
import com.sakethh.linkora.domain.dto.server.link.DeleteDuplicateLinksDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateNoteOfALinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateTitleOfTheLinkDTO
import com.sakethh.linkora.domain.repository.remote.RemoteLinksRepo
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class RemoteLinksRepoImpl(
    private val syncServerClient: () ->HttpClient,
    private val baseUrl: () -> String,
    private val authToken: () -> String
) : RemoteLinksRepo {
    override suspend fun addANewLink(addLinkDTO: AddLinkDTO): Flow<Result<NewItemResponseDTO>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.CREATE_A_NEW_LINK.name,
            body = addLinkDTO
        )
    }

    override suspend fun deleteALink(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.DELETE_A_LINK.name,
            body = idBasedDTO
        )
    }

    override suspend fun updateLinkTitle(
        updateTitleOfTheLinkDTO: UpdateTitleOfTheLinkDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UPDATE_LINK_TITLE.name,
            body = updateTitleOfTheLinkDTO
        )
    }

    override suspend fun updateALinkNote(
        updateNoteOfALinkDTO: UpdateNoteOfALinkDTO
    ): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UPDATE_LINK_NOTE.name,
            body = updateNoteOfALinkDTO
        )
    }

    override suspend fun archiveALink(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.ARCHIVE_LINK.name,
            body = idBasedDTO
        )
    }

    override suspend fun unArchiveALink(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UNARCHIVE_LINK.name,
            body = idBasedDTO
        )
    }

    override suspend fun markALinkAsImp(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.MARK_AS_IMP.name,
            body = idBasedDTO
        )
    }

    override suspend fun markALinkAsNonImp(idBasedDTO: IDBasedDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UNMARK_AS_IMP.name,
            body = idBasedDTO
        )
    }

    override suspend fun updateLink(linkDTO: LinkDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UPDATE_LINK.name,
            body = linkDTO
        )
    }

    override suspend fun deleteDuplicateLinks(deleteDuplicateLinksDTO: DeleteDuplicateLinksDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            syncServerClient = syncServerClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.DELETE_DUPLICATE_LINKS.name,
            body = deleteDuplicateLinksDTO
        )
    }
}