package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sakethh.linkora.common.utils.LinkType
import com.sakethh.linkora.common.utils.Sorting
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinksDao {
    @Insert
    suspend fun addANewLink(link: Link): Long

    @Insert
    suspend fun addMultipleLinks(links: List<Link>)

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

    @Query(
        "SELECT * FROM links \n" +
                "    WHERE (LOWER(title) LIKE '%' || LOWER(:query) || '%' \n" +
                "           OR LOWER(note) LIKE '%' || LOWER(:query) || '%') \n" +
                "    ORDER BY \n" +
                "        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN title COLLATE NOCASE END ASC,\n" +
                "        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN title COLLATE NOCASE END DESC,\n" +
                "        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN id END DESC,\n" +
                "        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN id END ASC"
    )
    fun search(query: String, sortOption: String): Flow<List<Link>>

    @Query(
        """
    SELECT * FROM links 
    WHERE linkType = :linkType
    ORDER BY 
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN title COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN title COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN id END DESC,
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN id END ASC
    """
    )
    fun sortLinks(
        linkType: com.sakethh.linkora.domain.LinkType, sortOption: String
    ): Flow<List<Link>>

    @Query(
        """
    SELECT * FROM links 
    ORDER BY 
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN title COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN title COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN id END DESC,
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN id END ASC
    """
    )
    fun sortAllLinks(
        sortOption: String
    ): Flow<List<Link>>


    @Query(
        """
    SELECT * FROM links 
    WHERE linkType = :linkType AND idOfLinkedFolder = :parentFolderId 
    ORDER BY 
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN title COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN title COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN id END DESC,
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN id END ASC
    """
    )
    fun sortLinks(
        linkType: com.sakethh.linkora.domain.LinkType, parentFolderId: Long, sortOption: String
    ): Flow<List<Link>>

    @Query("SELECT * FROM links")
    suspend fun getAllLinks(): List<Link>

    @Query("SELECT * FROM links WHERE idOfLinkedFolder=:folderId")
    suspend fun getLinksOfThisFolderAsList(folderId: Long): List<Link>

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()


    @Update
    suspend fun updateALink(link: Link)
}