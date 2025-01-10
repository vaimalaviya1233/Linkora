package com.sakethh.linkora.ui.screens.search

import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM

class SearchScreenVM(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
) : CollectionsScreenVM(localFoldersRepo, localLinksRepo, loadRootFoldersOnInit = false) {
    init {
        updateCollectionDetailPaneInfo(
            CollectionDetailPaneInfo(
                Folder(
                    name = Localization.Key.History.getLocalizedString(),
                    note = "",
                    parentFolderId = null,
                    localId = Constants.HISTORY_ID
                ), isAnyCollectionSelected = true
            )
        )
    }
}