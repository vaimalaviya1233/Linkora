package com.sakethh.linkora.domain.dto.server.tag

import com.sakethh.linkora.domain.dto.server.Correlation
import com.sakethh.linkora.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class CreateTagDTO(
    val name: String,
    val eventTimestamp: Long,
    val correlation: Correlation = AppPreferences.getCorrelation(),
    val offlineSyncItemId: Long = 0
)