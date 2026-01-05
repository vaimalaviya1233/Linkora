package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sakethh.linkora.domain.model.RefreshLink

@Dao
interface RefreshLinkDao {
    @Insert
    suspend fun insertAProcessedId(refreshLink: RefreshLink)

    @Query("SELECT refreshedLinkId FROM RefreshLink")
    suspend fun getProcessedLinkIds(): List<Long>

    @Query("DELETE FROM RefreshLink") // TRUNCATE TABLE is not a thing here with room/sqlite
    suspend fun deleteAllIds()
}