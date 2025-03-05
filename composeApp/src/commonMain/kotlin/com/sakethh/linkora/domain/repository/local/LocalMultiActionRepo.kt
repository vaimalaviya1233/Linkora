package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.Result
import kotlinx.coroutines.flow.Flow

interface LocalMultiActionRepo {
    suspend fun archiveMultipleItems(linkIds: List<Long>, folderIds: List<Long>,viaSocket: Boolean = false): Flow<Result<Unit>>
    suspend fun deleteMultipleItems(
        linkIds: List<Long>,
        folderIds: List<Long>,
        viaSocket: Boolean = false
    ): Flow<Result<Unit>>
}