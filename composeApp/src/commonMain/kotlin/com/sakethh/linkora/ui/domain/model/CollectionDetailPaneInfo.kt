package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.domain.model.Folder
import kotlinx.serialization.Serializable

@Serializable
data class CollectionDetailPaneInfo(
    val currentFolder: Folder?,
    val isAnyCollectionSelected: Boolean,
    val searchNavigated: SearchNavigated = SearchNavigated(
        navigatedFromSearchScreen = false,
        navigatedWithFolderId = -1
    )
)

@Serializable
data class SearchNavigated(
    val navigatedFromSearchScreen: Boolean,
    val navigatedWithFolderId: Long
)
