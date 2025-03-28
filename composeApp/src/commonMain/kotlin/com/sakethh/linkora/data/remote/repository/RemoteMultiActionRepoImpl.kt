package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.utils.postFlow
import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.CopyItemsDTO
import com.sakethh.linkora.domain.dto.server.CopyItemsHTTPResponseDTO
import com.sakethh.linkora.domain.dto.server.MarkItemsRegularDTO
import com.sakethh.linkora.domain.dto.server.MoveItemsDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.repository.remote.RemoteMultiActionRepo
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow

class RemoteMultiActionRepoImpl(
    private val httpClient: HttpClient,
    private val baseUrl: () -> String,
    private val authToken: () -> String
) : RemoteMultiActionRepo {
    override suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.MultiAction.ARCHIVE_MULTIPLE_ITEMS.name,
            body = archiveMultipleItemsDTO
        )
    }

    override suspend fun deleteMultipleItems(deleteMultipleItemsDTO: DeleteMultipleItemsDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.MultiAction.DELETE_MULTIPLE_ITEMS.name,
            body = deleteMultipleItemsDTO
        )
    }

    override suspend fun moveMultipleItems(moveItemsDTO: MoveItemsDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.MultiAction.MOVE_EXISTING_ITEMS.name,
            body = moveItemsDTO
        )
    }

    override suspend fun copyMultipleItems(copyItemsDTO: CopyItemsDTO): Flow<Result<CopyItemsHTTPResponseDTO>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.MultiAction.COPY_EXISTING_ITEMS.name,
            body = copyItemsDTO
        )
    }

    override suspend fun markItemsAsRegular(markItemsRegularDTO: MarkItemsRegularDTO): Flow<Result<TimeStampBasedResponse>> {
        return postFlow(
            httpClient = httpClient,
            baseUrl = baseUrl,
            authToken = authToken,
            endPoint = RemoteRoute.MultiAction.UNARCHIVE_MULTIPLE_ITEMS.name,
            body = markItemsRegularDTO
        )
    }
}