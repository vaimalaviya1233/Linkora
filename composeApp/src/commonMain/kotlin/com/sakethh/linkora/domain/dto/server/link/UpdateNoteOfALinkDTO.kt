package com.sakethh.linkora.domain.dto.server.link

import com.sakethh.linkora.common.preferences.AppPreferences
import kotlinx.serialization.Serializable

@Serializable
data class UpdateNoteOfALinkDTO(
    val linkId: Long,
    val newNote: String,
    val correlationId: String = AppPreferences.correlationId
)