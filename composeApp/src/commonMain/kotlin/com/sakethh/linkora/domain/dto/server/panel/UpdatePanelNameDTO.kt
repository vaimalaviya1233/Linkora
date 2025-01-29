package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class UpdatePanelNameDTO(
    val newName: String, val panelId: Long,
    val correlation: Correlation = AppPreferences.getCorrelation()
)
