package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.domain.model.link.Link
import kotlinx.serialization.Serializable

@Serializable
data class LinkTagsPair(
    val link: Link,
    val tags: List<Tag>
)