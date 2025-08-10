package com.sakethh.linkora.domain.dto.server

import com.sakethh.linkora.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class NewItemResponseDTO(
    val timeStampBasedResponse: TimeStampBasedResponse,
    val id: Long,
    val correlation: Correlation = AppPreferences.getCorrelation()
)