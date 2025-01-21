package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.utils.postFlow
import com.sakethh.linkora.domain.LinkRoute
import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.AddLinkDTO
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
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
            endPoint = LinkRoute.CREATE_A_NEW_LINK.name,
            body = addLinkDTO
        )
    }

    override suspend fun deleteALink(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = LinkRoute.DELETE_A_LINK.name,
            body = remoteLinkId
        )
    }

    override suspend fun renameALinkTitle(
        remoteLinkId: Long, newTitle: String
    ): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = LinkRoute.UPDATE_LINK_TITLE.name,
            body = remoteLinkId
        )
    }

    override suspend fun renameALinkNote(
        remoteLinkId: Long, newNote: String
    ): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = LinkRoute.UPDATE_LINK_NOTE.name,
            body = remoteLinkId
        )
    }

    override suspend fun archiveALink(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = LinkRoute.ARCHIVE_LINK.name,
            body = remoteLinkId
        )
    }

    override suspend fun unArchiveALink(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = LinkRoute.UNARCHIVE_LINK.name,
            body = remoteLinkId
        )
    }

    override suspend fun markALinkAsImp(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = LinkRoute.MARK_AS_IMP.name,
            body = remoteLinkId
        )
    }

    override suspend fun markALinkAsNonImp(remoteLinkId: Long): Flow<Result<Message>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = LinkRoute.UNMARK_AS_IMP.name,
            body = remoteLinkId
        )
    }
}