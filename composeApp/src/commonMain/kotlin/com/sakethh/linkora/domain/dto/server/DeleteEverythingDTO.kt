package com.sakethh.linkora.domain.dto.server

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable
import java.time.Instant

@Serializable
data class DeleteEverythingDTO(
    val eventTimestamp: Long = Instant.now().epochSecond, val correlation: Correlation = AppPreferences.getCorrelation()
)