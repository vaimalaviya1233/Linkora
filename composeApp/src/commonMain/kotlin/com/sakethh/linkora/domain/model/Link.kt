package com.sakethh.linkora.domain.model

data class Link(
    val linkType: LinkType,
    val id: Long,
    val linkTitle: String,
    val webURL: String,
    val baseURL: String,
    val imgURL: String,
    val infoForSaving: String,
    val lastModified: String,
    val isLinkedWithSavedLinks: Boolean,
    val isLinkedWithFolders: Boolean,
    val idOfLinkedFolder: Long?,
    val userAgent: String?,
)