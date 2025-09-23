package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.tag.Tag
import kotlinx.serialization.Serializable

@Serializable
data class CollectionDetailPaneInfo(
    val currentFolder: Folder?,
    val currentTag: Tag?,
    val collectionType: CollectionType?,
    val isAnyCollectionSelected: Boolean
)

enum class CollectionType {
    FOLDER, TAG
}

@Serializable
data class SearchNavigated(
    val navigatedFromSearchScreen: Boolean,
    val navigatedWithFolderId: Long
)
