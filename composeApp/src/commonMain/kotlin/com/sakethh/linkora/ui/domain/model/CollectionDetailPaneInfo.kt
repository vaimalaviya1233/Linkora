package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.domain.model.Folder
import kotlinx.serialization.Serializable

@Serializable
data class CollectionDetailPaneInfo(
    val currentFolder: Folder?,
    val isAnyCollectionSelected: Boolean
)
