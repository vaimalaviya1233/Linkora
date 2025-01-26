package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sakethh.linkora.domain.model.PendingSyncQueue

@Dao
interface PendingSyncQueueDao {
    @Insert
    suspend fun addInQueue(pendingSyncQueue: PendingSyncQueue)

    @Query("DELETE FROM pending_sync_queue WHERE id = :id")
    suspend fun deleteFromQueue(id: Long)

    @Query("SELECT * FROM pending_sync_queue")
    suspend fun getAllItemsFromQueue(): List<PendingSyncQueue>
}