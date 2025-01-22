package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.link.AddLinkDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import kotlinx.coroutines.flow.Flow

interface RemoteLinksRepo {
    suspend fun addANewLink(addLinkDTO: AddLinkDTO): Flow<Result<NewItemResponseDTO>>
    suspend fun deleteALink(remoteLinkId: Long): Flow<Result<Message>>
    suspend fun renameALinkTitle(remoteLinkId: Long, newTitle: String): Flow<Result<Message>>
    suspend fun renameALinkNote(remoteLinkId: Long, newNote: String): Flow<Result<Message>>
    suspend fun archiveALink(remoteLinkId: Long): Flow<Result<Message>>
    suspend fun unArchiveALink(remoteLinkId: Long): Flow<Result<Message>>
    suspend fun markALinkAsImp(remoteLinkId: Long): Flow<Result<Message>>
    suspend fun markALinkAsNonImp(remoteLinkId: Long): Flow<Result<Message>>
    suspend fun updateLink(linkDTO: LinkDTO): Flow<Result<Message>>
}