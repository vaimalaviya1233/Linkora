package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePanelNameDTO(
    val newName: String, val panelId: Long,
    val correlationId: String = AppPreferences.correlationId
)
