package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class AddANewPanelDTO(
    val panelName: String,
    val correlation: Correlation = AppPreferences.getCorrelation(),
    val eventTimestamp: Long = 0,
    val offlineSyncItemId: Long = 0
)