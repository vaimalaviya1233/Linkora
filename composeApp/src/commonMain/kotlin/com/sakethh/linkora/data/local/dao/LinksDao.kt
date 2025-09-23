package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.sakethh.linkora.utils.LinkType
import com.sakethh.linkora.utils.Sorting
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinksDao {
    @Insert
    suspend fun addANewLink(link: Link): Long

    @Insert
    suspend fun addMultipleLinks(links: List<Link>): List<Long>

    @Query("DELETE FROM links WHERE idOfLinkedFolder = :folderId")
    suspend fun deleteLinksOfFolder(folderId: Long)

    @Query("UPDATE links SET note = '' WHERE localId=:linkId")
    suspend fun deleteALinkNote(linkId: Long)

    @Query("UPDATE links SET linkType = '${LinkType.ARCHIVE_LINK}' WHERE localId=:linkId")
    suspend fun archiveALink(linkId: Long)

    @Query("UPDATE links SET linkType = '${LinkType.ARCHIVE_LINK}', lastModified = :eventTimestamp WHERE localId IN (:linkIds)")
    suspend fun archiveMultipleLinks(linkIds: List<Long>, eventTimestamp: Long)

    @Query("DELETE FROM links WHERE localId = :linkId")
    suspend fun deleteALink(linkId: Long)

    @Query("DELETE FROM links WHERE localId IN (:linkIds)")
    suspend fun deleteMultipleLinks(linkIds: List<Long>)

    @Query("DELETE FROM links WHERE url = :url")
    suspend fun deleteALink(url: String)

    @Query("UPDATE links SET note = :newNote WHERE localId=:linkId")
    suspend fun updateLinkNote(linkId: Long, newNote: String)

    @Query("UPDATE links SET title = :newTitle WHERE localId=:linkId")
    suspend fun updateLinkTitle(linkId: Long, newTitle: String)

    @Query("SELECT CASE WHEN linkType = 'ARCHIVE_LINK' THEN 1 ELSE 0 END FROM links WHERE url=:url")
    suspend fun isInArchive(url: String): Boolean

    @Query(
        """
    SELECT * FROM links
    WHERE (LOWER(title) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(note) LIKE '%' || LOWER(:query) || '%'
           OR LOWER(url) LIKE '%' || LOWER(:query) || '%')
    ORDER BY
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN title COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN title COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN localId END DESC,
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN localId END ASC
    """
    )
    fun search(query: String, sortOption: String): Flow<List<Link>>

    @Query(
        """
    SELECT * FROM links
    WHERE
        linkType = :linkType
    ORDER BY
        CASE WHEN :sortOption = 'A_TO_Z' THEN title COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = 'Z_TO_A' THEN title COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = 'NEW_TO_OLD' THEN localId END DESC,
        CASE WHEN :sortOption = 'OLD_TO_NEW' THEN localId END ASC
"""
    )
    fun getSortedLinks(
        linkType: com.sakethh.linkora.domain.LinkType, sortOption: String
    ): Flow<List<Link>>

    @Query(
        """
    SELECT * FROM links 
    ORDER BY 
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN title COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN title COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN localId END DESC,
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN localId END ASC
    """
    )
    fun getAllLinks(
        sortOption: String
    ): Flow<List<Link>>


    @Query(
        """
    SELECT * FROM links 
    WHERE linkType = :linkType AND idOfLinkedFolder = :parentFolderId 
    ORDER BY 
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN title COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN title COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN localId END DESC,
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN localId END ASC
    """
    )
    fun getSortedLinks(
        linkType: com.sakethh.linkora.domain.LinkType, parentFolderId: Long, sortOption: String
    ): Flow<List<Link>>

    @Query(
        """
        SELECT link.* 
        FROM links link
        INNER JOIN link_tags linkTag ON link.localId = linkTag.linkId
        WHERE linkTag.tagId = :tagId
        ORDER BY 
            CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN link.title COLLATE NOCASE END ASC,
            CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN link.title COLLATE NOCASE END DESC,
            CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN link.localId END DESC,
            CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN link.localId END ASC
    """
    )
    fun getSortedLinks(
        tagId: Long,
        sortOption: String,
    ): Flow<List<Link>>

    @Query("SELECT * FROM links")
    suspend fun getAllLinks(): List<Link>

    @Query("SELECT * FROM links")
    fun getAllLinksAsFlow(): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE idOfLinkedFolder=:folderId")
    suspend fun getLinksOfThisFolderAsList(folderId: Long): List<Link>

    @Query("DELETE FROM links")
    suspend fun deleteAllLinks()


    @Update
    suspend fun updateALink(link: Link)

    @Query("SELECT remoteId FROM links WHERE localId = :localId LIMIT 1")
    suspend fun getRemoteIdOfLocalLink(localId: Long): Long?

    @Query("SELECT localId FROM links WHERE remoteId = :remoteId LIMIT 1")
    suspend fun getLocalIdOfALink(remoteId: Long): Long?

    @Query("SELECT MAX(localId) FROM links")
    suspend fun getLatestId(): Long

    @Query("SELECT * FROM links WHERE localId = :localId LIMIT 1")
    suspend fun getLink(localId: Long): Link

    @Query("SELECT * FROM links WHERE remoteId IS NULL")
    suspend fun getUnSyncedLinks(): List<Link>

    @Query("UPDATE links SET lastModified = :timestamp WHERE localId=:localLinkId")
    suspend fun updateLinkTimestamp(timestamp: Long, localLinkId: Long)

    @Query("UPDATE links SET lastModified = :timestamp WHERE localId IN (:localLinkIds)")
    suspend fun updateLinksTimestamp(timestamp: Long, localLinkIds: List<Long>)

    @Query("SELECT EXISTS(SELECT 1 FROM links WHERE linkType = :linkType AND url = :url)")
    suspend fun doesLinkExist(linkType: com.sakethh.linkora.domain.LinkType, url: String): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM links WHERE linkType = '${LinkType.FOLDER_LINK}' AND idOfLinkedFolder =:folderId AND url = :url)")
    suspend fun doesLinkExist(folderId: Long, url: String): Boolean

    @Query("UPDATE links SET localId = :newId WHERE localId=:existingId")
    suspend fun changeIdOfALink(existingId: Long, newId: Long)

    @Query("DELETE FROM links WHERE url=:url AND linkType = 'HISTORY_LINK'")
    suspend fun deleteLinksFromHistory(url: String)

    @Query("DELETE FROM links WHERE localId IN (:linkIds)")
    suspend fun deleteLinks(linkIds: List<Long>)

    @Query("UPDATE links SET idOfLinkedFolder = :folderId, linkType = :linkType, lastModified = :eventTimestamp WHERE localId IN (:linkIds)")
    suspend fun moveLinks(
        folderId: Long?,
        linkType: com.sakethh.linkora.domain.LinkType,
        linkIds: List<Long>,
        eventTimestamp: Long
    )

    @Query("SELECT localId FROM links WHERE lastModified = :eventTimestamp AND idOfLinkedFolder = :parentFolderId AND linkType = :linkType AND remoteId IS NULL")
    suspend fun getIdsOfCopiedLinks(
        eventTimestamp: Long,
        parentFolderId: Long,
        linkType: com.sakethh.linkora.domain.LinkType,
    ): List<Long>

    @Query("UPDATE links SET remoteId = :remoteId WHERE localId = :localId")
    suspend fun updateRemoteLinkId(
        localId: Long, remoteId: Long
    )

    @Query("UPDATE links SET linkType = '${LinkType.SAVED_LINK}', lastModified = :eventTimestamp WHERE localId IN (:linksIds)")
    suspend fun unarchiveLinks(linksIds: List<Long>, eventTimestamp: Long)
}