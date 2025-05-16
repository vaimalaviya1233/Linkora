package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sakethh.linkora.domain.model.Snapshot

@Dao
interface SnapshotDao {
    @Query("SELECT * FROM snapshot WHERE id = :id")
    suspend fun getASnapshot(id: Long): Snapshot

    @Insert
    suspend fun addASnapshot(snapshot: Snapshot): Long

    @Query("DELETE FROM snapshot WHERE id = :id")
    suspend fun deleteASnapshot(id: Long)
}