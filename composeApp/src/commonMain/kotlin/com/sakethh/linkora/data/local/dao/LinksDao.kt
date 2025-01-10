package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.sakethh.linkora.common.utils.LinkType
import com.sakethh.linkora.domain.model.link.Link

@Dao
interface LinksDao {
    @Insert
    suspend fun addANewLink(link: Link): Long

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