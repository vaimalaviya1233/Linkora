package com.sakethh.linkora.ui.screens.collections

import androidx.compose.runtime.Stable
import androidx.compose.runtime.State
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.ui.domain.PaginationState
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.domain.model.LinkTagsPair
import kotlinx.coroutines.flow.StateFlow

@Stable
data class CollectionDetailPaneParams(
    val linkTagsPairs: StateFlow<List<LinkTagsPair>>,
    val childFolders: StateFlow<List<Folder>>,
    val rootArchiveFolders: StateFlow<PaginationState<Map<Int, List<Folder>>>>,
    val collectionDetailPaneInfo: CollectionDetailPaneInfo?,
    val peekPaneHistory: StateFlow<CollectionDetailPaneInfo?>,
    val availableFiltersForAllLinks: StateFlow<Set<LinkType>>,
    val appliedFiltersForAllLinks: SnapshotStateList<LinkType>,
    val performAction: (CollectionsAction) -> Unit
)
