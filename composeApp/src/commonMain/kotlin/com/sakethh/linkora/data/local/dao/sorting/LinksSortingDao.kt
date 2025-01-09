package com.sakethh.linkora.data.local.dao.sorting

import androidx.room.Dao
import androidx.room.Query
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

@Dao
interface LinksSortingDao {

    @Query("SELECT * FROM links WHERE linkType = :linkType ORDER BY title COLLATE NOCASE ASC")
    fun sortByAToZ(linkType: LinkType): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = :linkType ORDER BY title COLLATE NOCASE DESC")
    fun sortByZToA(linkType: LinkType): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = :linkType ORDER BY id DESC")
    fun sortByLatestToOldest(linkType: LinkType): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = :linkType ORDER BY id ASC")
    fun sortByOldestToLatest(linkType: LinkType): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = :linkType AND idOfLinkedFolder = :parentFolderId ORDER BY title COLLATE NOCASE ASC")
    fun sortByAToZ(linkType: LinkType, parentFolderId: Long): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = :linkType AND idOfLinkedFolder = :parentFolderId ORDER BY title COLLATE NOCASE DESC")
    fun sortByZToA(linkType: LinkType, parentFolderId: Long): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = :linkType AND idOfLinkedFolder = :parentFolderId ORDER BY id DESC")
    fun sortByLatestToOldest(linkType: LinkType, parentFolderId: Long): Flow<List<Link>>

    @Query("SELECT * FROM links WHERE linkType = :linkType AND idOfLinkedFolder = :parentFolderId ORDER BY id ASC")
    fun sortByOldestToLatest(linkType: LinkType, parentFolderId: Long): Flow<List<Link>>
}