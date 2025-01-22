package com.sakethh.linkora.domain.dto.server.link

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class UpdateTitleOfTheLinkDTO(
    val linkId: Long,
    val newTitleOfTheLink: String,
    val correlationId: String = AppPreferences.correlationId
)
