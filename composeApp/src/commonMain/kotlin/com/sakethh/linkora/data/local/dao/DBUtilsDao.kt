package com.sakethh.linkora.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.FlatChildFolderData
import com.sakethh.linkora.utils.Sorting
import kotlinx.coroutines.flow.Flow

@Dao
interface DBUtilsDao {

    @Query("SELECT COALESCE(MAX(localId), 0) FROM folders")
    suspend fun getFoldersRowCount(): Long


    @Query("""
    SELECT * FROM (
    -- TIL COMMENTING IN SQL LOLLL
        SELECT 
            'FOLDER' AS itemType, 
            folders.lastModified,
            -- `Folder` Columns
            folders.name AS folderName, 
            folders.note AS folderNote, 
            folders.parentFolderId AS folderParentId, 
            folders.localId AS folderLocalId, 
            folders.remoteId AS folderRemoteId, 
            folders.isArchived AS folderIsArchived,
            -- `Link` Columns (NULLs)
            NULL AS linkType, NULL AS linkLocalId, NULL AS linkRemoteId, NULL AS linkTitle, 
            NULL AS linkUrl, NULL AS linkHost, NULL AS linkImgUrl, NULL AS linkNote, 
            NULL AS linkIdOfLinkedFolder, NULL AS linkUserAgent, NULL AS linkMediaType,
            NULL AS linkTagsJson
        FROM folders 
        WHERE folders.parentFolderID = :parentFolderId

        UNION ALL

        SELECT 
            'LINK' AS itemType, 
            links.lastModified,
            -- `Folder` Columns (NULLs)
            NULL, NULL, NULL, NULL, NULL, NULL,
            -- `Link` Columns
            links.linkType, links.localId, links.remoteId, links.title, 
            links.url, links.baseURL, links.imgURL, links.note, 
            links.idOfLinkedFolder, links.userAgent, links.mediaType,
            -- `Tags` JSON
            (SELECT json_group_array(
                json_object(
                    'localId', tags.localId,
                    'remoteId', tags.remoteId,
                    'lastModified', tags.lastModified,
                    'name', tags.name
                )
            ) FROM tags 
              INNER JOIN link_tags ON tags.localId = link_tags.tagId 
              WHERE link_tags.linkId = links.localId
            ) AS linkTagsJson
        FROM links 
        WHERE links.linkType = :linkType AND links.idOfLinkedFolder = :parentFolderId
    ) 
    ORDER BY 
        CASE WHEN itemType = 'FOLDER' THEN 0 ELSE 1 END ASC,
        CASE WHEN :sortOption = '${Sorting.A_TO_Z}' THEN COALESCE(folderName, linkTitle) COLLATE NOCASE END ASC,
        CASE WHEN :sortOption = '${Sorting.Z_TO_A}' THEN COALESCE(folderName, linkTitle) COLLATE NOCASE END DESC,
        CASE WHEN :sortOption = '${Sorting.OLD_TO_NEW}' THEN COALESCE(folderLocalId, linkLocalId) END ASC,
        CASE WHEN :sortOption = '${Sorting.NEW_TO_OLD}' THEN COALESCE(folderLocalId, linkLocalId) END DESC
    LIMIT :pageSize 
    OFFSET :startIndex
""")
    fun getFlatChildFolderData(
        parentFolderId: Long,
        linkType: LinkType,
        sortOption: String,
        pageSize: Int,
        startIndex: Long
    ): Flow<List<FlatChildFolderData>> // UNION ALL in a typical RDBMS/SQL is a "god-sent" thing, i'm gonna tweet this
}