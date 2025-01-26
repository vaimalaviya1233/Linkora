package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import com.sakethh.linkora.domain.dto.server.LocalIdSerializer
import kotlinx.serialization.Serializable

@Serializable
data class PanelDTO(
    val panelId: Long,
    val panelName: String,
    val correlation: Correlation = AppPreferences.getCorrelation(),
    @Serializable(with = LocalIdSerializer::class)
    val pendingQueueSyncLocalId: Long = 0
)
