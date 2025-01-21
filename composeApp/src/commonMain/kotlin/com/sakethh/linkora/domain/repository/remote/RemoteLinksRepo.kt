package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.NewItemResponseDTO
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.coroutines.flow.Flow

interface RemoteLinksRepo {
    suspend fun addANewLink(link: Link): Flow<Result<NewItemResponseDTO>>
    suspend fun deleteALink(remoteLinkId: Long): Flow<Result<Message>>
    suspend fun renameALinkTitle(remoteLinkId: Long, newTitle: String): Flow<Result<Message>>
    suspend fun renameALinkNote(remoteLinkId: Long, newNote: String): Flow<Result<Message>>
    suspend fun archiveALink(remoteLinkId: Long): Flow<Result<Message>>
    suspend fun unArchiveALink(remoteLinkId: Long): Flow<Result<Message>>
}