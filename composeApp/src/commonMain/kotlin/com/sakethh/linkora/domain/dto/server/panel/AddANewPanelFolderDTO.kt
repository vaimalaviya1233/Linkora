package com.sakethh.linkora.domain.dto.server.panel

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class AddANewPanelFolderDTO(
    val folderId: Long,
    val panelPosition: Long,
    val folderName: String,
    val connectedPanelId: Long,
    val correlationId: String = AppPreferences.correlationId
)