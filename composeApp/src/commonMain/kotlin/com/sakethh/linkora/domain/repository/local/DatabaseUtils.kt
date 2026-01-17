package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.FlatChildFolderData
import kotlinx.coroutines.flow.Flow

interface DatabaseUtils {
    suspend fun resetDatabase()

    suspend fun getFoldersRowCount(): Long

    fun getChildFolderData(
        parentFolderId: Long,
        linkType: LinkType,
        sortOption: String,
        pageSize: Int,
        startIndex: Long
    ): Flow<Result<List<FlatChildFolderData>>>


}