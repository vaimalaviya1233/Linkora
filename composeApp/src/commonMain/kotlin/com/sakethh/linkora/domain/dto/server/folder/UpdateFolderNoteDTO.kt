package com.sakethh.linkora.domain.dto.server.folder

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNoteDTO(
    val folderId: Long,
    val newNote: String,
    val correlationId: String = AppPreferences.correlationId
)