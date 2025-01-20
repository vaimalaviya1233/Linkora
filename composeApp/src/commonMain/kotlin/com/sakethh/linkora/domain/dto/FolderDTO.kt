package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class FolderDTO(
    val id: Long? = null,
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    val isArchived: Boolean
)