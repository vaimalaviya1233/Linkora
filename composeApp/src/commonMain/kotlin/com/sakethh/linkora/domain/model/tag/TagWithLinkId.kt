package com.sakethh.linkora.domain.model.tag

import androidx.room.Embedded

data class TagWithLinkId(
    val linkId: Long,
    @Embedded
    val tag: Tag
)