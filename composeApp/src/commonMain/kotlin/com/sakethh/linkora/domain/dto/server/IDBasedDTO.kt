package com.sakethh.linkora.domain.dto.server

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class IDBasedDTO(
    val id: Long,
    val eventTimestamp: Long = 0,
    val correlation: Correlation = AppPreferences.getCorrelation(),
)
