package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import kotlinx.coroutines.flow.Flow

interface RemoteMultiActionRepo {
    suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Flow<Result<TimeStampBasedResponse>>
}