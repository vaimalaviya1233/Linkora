package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class PanelDTO(
    val panelId: Long,
    val panelName: String,
    val correlationId: String = AppPreferences.correlationId
)
