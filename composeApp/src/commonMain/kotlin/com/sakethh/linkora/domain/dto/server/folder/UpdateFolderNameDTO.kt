package com.sakethh.linkora.domain.dto.server.folder

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNameDTO(
    val folderId: Long,
    val newFolderName: String,
    val correlationId: String = AppPreferences.correlationId
)