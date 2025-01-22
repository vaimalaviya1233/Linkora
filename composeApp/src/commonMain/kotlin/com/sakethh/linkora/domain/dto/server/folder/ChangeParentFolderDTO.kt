package com.sakethh.linkora.domain.dto.server.folder

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class ChangeParentFolderDTO(
    val folderId: Long,
    val newParentFolderId: Long?,
    val correlationId: String = AppPreferences.correlationId
)