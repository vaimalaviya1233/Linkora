package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.NewItemResponseDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.link.AddLinkDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import kotlinx.coroutines.flow.Flow

interface RemoteLinksRepo {
    suspend fun addANewLink(addLinkDTO: AddLinkDTO): Flow<Result<NewItemResponseDTO>>
    suspend fun deleteALink(remoteLinkId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun renameALinkTitle(
        remoteLinkId: Long,
        newTitle: String
    ): Flow<Result<TimeStampBasedResponse>>

    suspend fun renameALinkNote(
        remoteLinkId: Long,
        newNote: String
    ): Flow<Result<TimeStampBasedResponse>>

    suspend fun archiveALink(remoteLinkId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun unArchiveALink(remoteLinkId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun markALinkAsImp(remoteLinkId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun markALinkAsNonImp(remoteLinkId: Long): Flow<Result<TimeStampBasedResponse>>
    suspend fun updateLink(linkDTO: LinkDTO): Flow<Result<TimeStampBasedResponse>>
}