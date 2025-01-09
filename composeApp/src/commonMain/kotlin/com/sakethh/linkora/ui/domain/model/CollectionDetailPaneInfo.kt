package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.domain.model.Folder

data class CollectionDetailPaneInfo(
    val currentFolder: Folder?,
    val isAnyCollectionSelected: Boolean
)
