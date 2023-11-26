package com.sakethh.linkora.localDB.dao.crud

import androidx.room.Dao
import androidx.room.Query
import com.sakethh.linkora.localDB.dto.ArchivedFolders
import com.sakethh.linkora.localDB.dto.ArchivedLinks
import com.sakethh.linkora.localDB.dto.FoldersTable
import com.sakethh.linkora.localDB.dto.ImportantLinks
import com.sakethh.linkora.localDB.dto.LinksTable
import com.sakethh.linkora.localDB.dto.RecentlyVisited
import kotlinx.coroutines.flow.Flow

@Dao
interface ReadDao {
    @Query("SELECT * FROM links_table WHERE isLinkedWithSavedLinks = 1")
    fun getAllSavedLinks(): Flow<List<LinksTable>>

    @Query("SELECT * FROM links_table")
    fun getAllFromLinksTable(): Flow<List<LinksTable>>

    @Query("SELECT * FROM recently_visited_table")
    fun getAllRecentlyVisitedLinks(): Flow<List<RecentlyVisited>>

    @Query("SELECT * FROM important_links_table")
    fun getAllImpLinks(): Flow<List<ImportantLinks>>

    @Query("SELECT * FROM archived_links_table")
    fun getAllArchiveLinks(): Flow<List<ArchivedLinks>>

    @Query("SELECT * FROM archived_folders_table")
    fun getAllArchiveFolders(): Flow<List<ArchivedFolders>>


    @Query("SELECT * FROM links_table WHERE isLinkedWithArchivedFolder=1")
    fun getAllArchiveFoldersLinks(): Flow<List<LinksTable>>

    @Query("SELECT * FROM folders_table WHERE parentFolderID IS NULL")
    fun getAllRootFolders(): Flow<List<FoldersTable>>

    @Query("SELECT * FROM links_table WHERE isLinkedWithFolders=1 AND keyOfLinkedFolder=:folderID")
    fun getThisFolderData(folderID: Long): Flow<List<LinksTable>>

    @Query("SELECT * FROM links_table WHERE isLinkedWithArchivedFolder=1 AND keyOfArchiveLinkedFolder=:folderID")
    fun getThisArchiveFolderData(folderID: Long): Flow<List<LinksTable>>

    @Query("SELECT EXISTS(SELECT * FROM important_links_table WHERE webURL = :webURL)")
    suspend fun doesThisExistsInImpLinks(webURL: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM links_table WHERE webURL = :webURL AND isLinkedWithSavedLinks=1)")
    suspend fun doesThisExistsInSavedLinks(webURL: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM links_table WHERE webURL = :webURL AND keyOfLinkedFolder=:folderID)")
    suspend fun doesThisLinkExistsInAFolder(webURL: String, folderID: Long): Boolean

    @Query("SELECT EXISTS(SELECT * FROM archived_links_table WHERE webURL = :webURL)")
    suspend fun doesThisExistsInArchiveLinks(webURL: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM recently_visited_table WHERE webURL = :webURL)")
    suspend fun doesThisExistsInRecentlyVisitedLinks(webURL: String): Boolean

    @Query("SELECT EXISTS(SELECT * FROM folders_table WHERE id = :folderID AND folderName = :folderName)")
    suspend fun doesThisFolderExists(folderName: String, folderID: Long?): Boolean

    @Query("SELECT EXISTS(SELECT * FROM archived_folders_table WHERE id = :folderID)")
    suspend fun doesThisArchiveFolderExists(folderID: Long): Boolean

    @Query("SELECT * FROM folders_table ORDER BY id DESC LIMIT 1")
    suspend fun getLatestAddedFolder(): FoldersTable

    @Query("SELECT COUNT(id) FROM folders_table")
    fun getFoldersCount(): Flow<Int>

    @Query("SELECT (SELECT COUNT(*) FROM links_table) == 0")
    suspend fun isLinksTableEmpty(): Boolean

    @Query("SELECT (SELECT COUNT(*) FROM folders_table) == 0")
    suspend fun isFoldersTableEmpty(): Boolean

    @Query("SELECT (SELECT COUNT(*) FROM archived_links_table) == 0")
    suspend fun isArchivedLinksTableEmpty(): Boolean

    @Query("SELECT (SELECT COUNT(*) FROM archived_folders_table) == 0")
    suspend fun isArchivedFoldersTableEmpty(): Boolean

    @Query("SELECT (SELECT COUNT(*) FROM important_links_table) == 0")
    suspend fun isImpLinksTableEmpty(): Boolean

    @Query("SELECT (SELECT COUNT(*) FROM recently_visited_table) == 0")
    suspend fun isHistoryLinksTableEmpty(): Boolean
}