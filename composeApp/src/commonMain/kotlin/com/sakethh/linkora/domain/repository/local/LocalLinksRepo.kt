package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

interface LocalLinksRepo {
    suspend fun addANewLink(
        link: Link,
        linkSaveConfig: LinkSaveConfig
    ): Flow<Result<Unit>>

    fun sortByAToZ(linkType: LinkType): Flow<Result<List<Link>>>

    fun sortByZToA(linkType: LinkType): Flow<Result<List<Link>>>

    fun sortByLatestToOldest(linkType: LinkType): Flow<Result<List<Link>>>

    fun sortByOldestToLatest(linkType: LinkType): Flow<Result<List<Link>>>

    fun sortByAToZ(linkType: LinkType, parentFolderId: Long): Flow<Result<List<Link>>>

    fun sortByZToA(linkType: LinkType, parentFolderId: Long): Flow<Result<List<Link>>>

    fun sortByLatestToOldest(linkType: LinkType, parentFolderId: Long): Flow<Result<List<Link>>>

    fun sortByOldestToLatest(linkType: LinkType, parentFolderId: Long): Flow<Result<List<Link>>>

    suspend fun deleteLinksOfFolder(folderId: Long): Flow<Result<Unit>>

    suspend fun deleteALinkNote(linkId: Long): Flow<Result<Unit>>

    suspend fun deleteALink(linkId: Long): Flow<Result<Unit>>

    suspend fun archiveALink(linkId: Long): Flow<Result<Unit>>

    suspend fun updateLinkNote(linkId: Long, newNote: String): Flow<Result<Unit>>

    suspend fun updateLinkTitle(linkId: Long, newTitle: String): Flow<Result<Unit>>

    suspend fun markedAsImportant(linkUrl: String): Flow<Result<Boolean>>

    suspend fun isInArchive(url: String): Flow<Result<Boolean>>
}