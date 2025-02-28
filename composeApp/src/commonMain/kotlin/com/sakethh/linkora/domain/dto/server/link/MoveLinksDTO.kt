package com.sakethh.linkora.domain.dto.server.link

import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.dto.server.Correlation
import kotlinx.serialization.Serializable

@Serializable
data class MoveLinksDTO(
    val linkIds: List<Long>,
    val parentFolderId: Long?,
    val linkType: LinkType,
    val eventTimestamp: Long,
    val correlation: Correlation = AppPreferences.getCorrelation()
)
