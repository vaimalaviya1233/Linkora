package com.sakethh.linkora.ui.screens.collections

import androidx.compose.runtime.Stable
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import kotlinx.coroutines.flow.StateFlow

@Stable
data class CollectionScreenParams(
    val isPaneSelected: StateFlow<Boolean>,
    val rootRegularFolders: StateFlow<List<Folder>>,
    val allTags: StateFlow<List<Tag>>,
    val peekPaneHistory: StateFlow<CollectionDetailPaneInfo?>,
    var currentCollectionSource: String,
    val performAction: (CollectionsAction) -> Unit,
)
