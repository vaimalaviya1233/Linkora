package com.sakethh.linkora.domain.dto.server.link

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType
import kotlinx.serialization.Serializable

@Serializable
data class LinkDTO(
    val id: Long,
    val linkType: LinkType,
    val title: String,
    val url: String,
    val baseURL: String,
    val imgURL: String,
    val note: String,
    val lastModified: String,
    val idOfLinkedFolder: Long?,
    val userAgent: String?,
    val markedAsImportant: Boolean,
    val mediaType: MediaType,
    val correlationId: String = AppPreferences.correlationId
)