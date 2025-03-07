package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.MoveItemsDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import kotlinx.coroutines.flow.Flow

interface RemoteMultiActionRepo {
    suspend fun archiveMultipleItems(archiveMultipleItemsDTO: ArchiveMultipleItemsDTO): Flow<Result<TimeStampBasedResponse>>
    suspend fun deleteMultipleItems(deleteMultipleItemsDTO: DeleteMultipleItemsDTO): Flow<Result<TimeStampBasedResponse>>
    suspend fun moveMultipleItems(moveItemsDTO: MoveItemsDTO): Flow<Result<TimeStampBasedResponse>>
    suspend fun copyMultipleItems(copyItemsDTO: MoveItemsDTO): Flow<Result<TimeStampBasedResponse>>
}