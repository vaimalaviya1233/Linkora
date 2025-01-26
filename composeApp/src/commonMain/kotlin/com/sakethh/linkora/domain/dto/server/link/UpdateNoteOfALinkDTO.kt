package com.sakethh.linkora.domain.dto.server.link

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteOfALinkDTO(
    val linkId: Long,
    val newNote: String,
    val correlation: Correlation = AppPreferences.getCorrelation(),
    val pendingQueueSyncLocalId: Long = 0
)