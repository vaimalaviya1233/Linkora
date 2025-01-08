package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

interface LocalLinksRepo {
    suspend fun addANewLink(
        link: Link,
        linkSaveConfig: LinkSaveConfig
    ): Flow<Result<Unit>>

    fun getAllSavedLinks(): Flow<Result<List<Link>>>

    fun getAllImportantLinks(): Flow<Result<List<Link>>>

    fun getLinksFromFolder(folderId: Long): Flow<Result<List<Link>>>

    suspend fun deleteLinksOfFolder(folderId: Long)
}