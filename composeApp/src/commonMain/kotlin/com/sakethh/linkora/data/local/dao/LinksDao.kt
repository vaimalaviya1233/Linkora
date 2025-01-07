package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import com.sakethh.linkora.domain.model.link.Link

@Dao
interface LinksDao {
    @Insert
    suspend fun addANewLink(link: Link): Long
}