package com.sakethh.linkora.domain.dto.server

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class NewItemResponseDTO(
    val message: String,
    val id: Long,
    val correlationId: String = AppPreferences.correlationId
)