package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sakethh.linkora.common.utils.LinkType
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinksDao {
    @Insert
    suspend fun addANewLink(link: Link): Long

    @Query("SELECT * FROM links WHERE linkType = '${LinkType.SAVED_LINK}'")
    fun getAllSavedLinks(): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = '${LinkType.IMPORTANT_LINK}'")
    fun getAllImportantLinks(): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = '${LinkType.FOLDER_LINK}' AND idOfLinkedFolder = :folderId")
    fun getLinksFromFolder(folderId: Long): Flow<List<Link>>
}