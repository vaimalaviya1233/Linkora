package com.sakethh.linkora.domain.dto.server.folder

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class UpdateFolderNameDTO(
    val folderId: Long,
    val newFolderName: String,
    val eventTimestamp: Long = 0,
    val correlation: Correlation = AppPreferences.getCorrelation()
)