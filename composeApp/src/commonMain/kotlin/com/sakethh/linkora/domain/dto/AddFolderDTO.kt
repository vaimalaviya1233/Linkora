package com.sakethh.linkora.domain.dto

import kotlinx.serialization.Serializable

@Serializable
data class AddFolderDTO(
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    val isArchived: Boolean
)