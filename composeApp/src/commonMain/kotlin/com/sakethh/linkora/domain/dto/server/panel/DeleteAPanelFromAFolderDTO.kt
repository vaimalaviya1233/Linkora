package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class DeleteAPanelFromAFolderDTO(
    val panelId: Long,
    val folderID: Long,
    val eventTimestamp: Long = 0,
    val correlation: Correlation = AppPreferences.getCorrelation()
)
