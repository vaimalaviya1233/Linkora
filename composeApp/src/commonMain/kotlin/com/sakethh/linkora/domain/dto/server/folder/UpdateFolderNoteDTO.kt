package com.sakethh.linkora.domain.dto.server.folder

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import com.sakethh.linkora.domain.dto.server.LocalIdSerializer
import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNoteDTO(
    val folderId: Long,
    val newNote: String,
    val correlation: Correlation = AppPreferences.getCorrelation(),
    @Serializable(with = LocalIdSerializer::class)
    val pendingQueueSyncLocalId: Long = 0
)