package com.sakethh.linkora.ui.screens.collections

import androidx.compose.runtime.Stable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.FlatChildFolderData
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.ui.PageKey
import com.sakethh.linkora.ui.domain.PaginationState
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.domain.model.LinkTagsPair
import kotlinx.coroutines.flow.StateFlow

@Stable
data class CollectionDetailPaneParams(
    val linkTagsPairs: StateFlow<PaginationState<Map<PageKey, List<LinkTagsPair>>>>,
    val childFoldersFlat: StateFlow<PaginationState<Map<PageKey, List<FlatChildFolderData>>>>,
    val rootArchiveFolders: StateFlow<PaginationState<Map<PageKey, List<Folder>>>>,
    val collectionDetailPaneInfo: CollectionDetailPaneInfo?,
    val peekPaneHistory: StateFlow<CollectionDetailPaneInfo?>,
    val appliedFiltersForAllLinks: SnapshotStateList<LinkType>,
    val performAction: (CollectionsAction) -> Unit
)
