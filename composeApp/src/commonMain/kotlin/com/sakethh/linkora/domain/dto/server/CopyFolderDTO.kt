package com.sakethh.linkora.domain.dto.server

import kotlinx.serialization.Serializable

@Serializable
data class CopyFolderDTO(
    val currentFolder: CurrentFolder,
    val links: List<FolderLink>,
    val childFolders: List<CopyFolderDTO>
)

@Serializable
data class CurrentFolder(
    val localId: Long, val remoteId: Long
)

typealias FolderLink = CurrentFolder