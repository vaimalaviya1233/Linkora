package com.sakethh.linkora.domain.repository.local

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.tag.LinkTag
import com.sakethh.linkora.domain.model.tag.Tag
import kotlinx.coroutines.flow.Flow

interface LocalTagsRepo {
    suspend fun createATag(tag: Tag,viaSocket: Boolean = false): Flow<Result<Long>>

    suspend fun createLinkTags(linksTags: List<LinkTag>)

    suspend fun deleteLinkTagsBasedOnLink(linkId: Long)

    suspend fun deleteLinkTagsBasedOnTag(tagId: Long)
    suspend fun deleteLinkTagsBasedOnTags(tagIds: List<Long>)
    suspend fun deleteATag(id: Long,viaSocket: Boolean = false): Flow<Result<Unit>>

    suspend fun renameATag(localTagId: Long, newName: String, viaSocket: Boolean = false) : Flow<Result<Unit>>
    suspend fun getLastInsertedIdFromTags(): Long

    fun getAllTags(sortOption: String): Flow<List<Tag>>
    suspend fun getAllTagsAsList(): List<Tag>
    suspend fun getAllLinkTagsAsList(): List<LinkTag>
    fun getAllLinkTags(): Flow<List<LinkTag>>

    fun getTagsBasedOnTheLinkId(linkId: Long): Flow<List<Tag>>

    fun getTagsForLinks(linkIds: List<Long>): Flow<Map<Long, List<Tag>>>

    fun search(query: String,sortOption: String): Flow<List<Tag>>
}