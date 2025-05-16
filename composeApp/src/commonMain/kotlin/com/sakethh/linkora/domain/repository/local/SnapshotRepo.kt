package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.model.Snapshot

interface SnapshotRepo {
    suspend fun getASnapshot(id: Long): Snapshot

    suspend fun addASnapshot(snapshot: Snapshot): Long

    suspend fun deleteASnapshot(id: Long)
}