package com.sakethh.linkora.domain.dto.server.folder

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class AddFolderDTO(
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    val isArchived: Boolean,
    val correlationId: String = AppPreferences.correlationId
)