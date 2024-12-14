package com.sakethh.linkora.domain.model

data class Folder(
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    val id: Long
)
