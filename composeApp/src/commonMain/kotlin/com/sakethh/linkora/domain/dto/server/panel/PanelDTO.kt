package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class PanelDTO(
    val panelId: Long,
    val panelName: String,
    val correlation: Correlation = AppPreferences.getCorrelation(),
    val eventTimestamp: Long
)
