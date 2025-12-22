package com.sakethh.linkora.ui.screens.collections

import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo

sealed interface CollectionsAction {
    data object PopFromDetailPane : CollectionsAction
    data object ClearDetailPaneHistoryUntilLast : CollectionsAction
    data class AddANewLink(
        val link: Link,
        val linkSaveConfig: LinkSaveConfig,
        val onCompletion: () -> Unit,
        val pushSnackbarOnSuccess: Boolean,
        val selectedTags: List<Tag>
    ) : CollectionsAction

    data class PushToDetailPane(val collectionDetailPaneInfo: CollectionDetailPaneInfo) :
        CollectionsAction

    data class ToggleAllLinksFilter(val filter: LinkType) : CollectionsAction
}