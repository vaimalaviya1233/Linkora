package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.utils.postFlow
import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.link.AddLinkDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateNoteOfALinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateTitleOfTheLinkDTO
import com.sakethh.linkora.domain.repository.remote.RemoteLinksRepo
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class RemoteLinksRepoImpl(
    private val httpClient: HttpClient,
    private val baseUrl: () -> String,
    private val authToken: () -> String
) : RemoteLinksRepo {
    override suspend fun addANewLink(addLinkDTO: AddLinkDTO): Flow<Result<NewItemResponseDTO>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.CREATE_A_NEW_LINK.name,
            body = addLinkDTO
        )
    }

    override suspend fun deleteALink(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.DELETE_A_LINK.name,
            body = IDBasedDTO(remoteLinkId)
        )
    }

    override suspend fun renameALinkTitle(
        remoteLinkId: Long, newTitle: String
    ): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UPDATE_LINK_TITLE.name,
            body = UpdateTitleOfTheLinkDTO(linkId = remoteLinkId, newTitleOfTheLink = newTitle)
        )
    }

    override suspend fun renameALinkNote(
        remoteLinkId: Long, newNote: String
    ): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UPDATE_LINK_NOTE.name,
            body = UpdateNoteOfALinkDTO(remoteLinkId, newNote)
        )
    }

    override suspend fun archiveALink(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.ARCHIVE_LINK.name,
            body = IDBasedDTO(remoteLinkId)
        )
    }

    override suspend fun unArchiveALink(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UNARCHIVE_LINK.name,
            body = IDBasedDTO(remoteLinkId)
        )
    }

    override suspend fun markALinkAsImp(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.MARK_AS_IMP.name,
            body = IDBasedDTO(remoteLinkId)
        )
    }

    override suspend fun markALinkAsNonImp(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UNMARK_AS_IMP.name,
            body = IDBasedDTO(remoteLinkId)
        )
    }

    override suspend fun updateLink(linkDTO: LinkDTO): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.Link.UPDATE_LINK.name,
            body = linkDTO
        )
    }
}