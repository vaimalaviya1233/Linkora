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

    @Query("DELETE FROM links WHERE idOfLinkedFolder = :folderId")
    suspend fun deleteLinksOfFolder(folderId: Long)

    @Query("UPDATE links SET note = '' WHERE id=:linkId")
    suspend fun deleteALinkNote(linkId: Long)

    @Query("UPDATE links SET linkType = '${LinkType.ARCHIVE_LINK}' WHERE id=:linkId")
    suspend fun archiveALink(linkId: Long)

    @Query("DELETE FROM links WHERE id = :linkId")
    suspend fun deleteALink(linkId: Long)

    @Query("UPDATE links SET note = :newNote WHERE id=:linkId")
    suspend fun updateLinkNote(linkId: Long, newNote: String)

    @Query("UPDATE links SET title = :newTitle WHERE id=:linkId")
    suspend fun updateLinkTitle(linkId: Long, newTitle: String)

    @Query("SELECT linkType = '${LinkType.IMPORTANT_LINK}' OR markedAsImportant FROM links WHERE url=:url")
    suspend fun markedAsImportant(url: String): Boolean

    @Query("SELECT linkType = '${LinkType.ARCHIVE_LINK}' FROM links WHERE url=:url")
    suspend fun isInArchive(url: String): Boolean
}