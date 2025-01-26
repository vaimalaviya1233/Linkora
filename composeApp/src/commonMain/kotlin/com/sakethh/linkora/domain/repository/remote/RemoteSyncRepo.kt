package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Result
import kotlinx.coroutines.flow.Flow

interface RemoteSyncRepo {
    suspend fun readSocketEvents(): Flow<Result<Unit>>
    suspend fun applyUpdatesBasedOnRemoteTombstones(timeStampAfter: Long): Flow<Result<Unit>>
    suspend fun pushPendingSyncQueueToServer(): Flow<Result<Unit>>
    suspend fun applyUpdatesFromRemote(timeStampAfter: Long): Flow<Result<Unit>>
}