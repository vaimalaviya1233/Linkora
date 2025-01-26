package com.sakethh.linkora.domain.dto.server.folder

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class FolderDTO(
    val id: Long,
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    val isArchived: Boolean,
    val correlation: Correlation = AppPreferences.getCorrelation(),
    val pendingQueueSyncLocalId: Long = 0
)
