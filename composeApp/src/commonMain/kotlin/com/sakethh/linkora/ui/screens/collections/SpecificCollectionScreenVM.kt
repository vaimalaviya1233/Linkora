package com.sakethh.linkora.ui.screens.collections

import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushLocalizedSnackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SpecificCollectionScreenVM(
    localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    loadRootFoldersOnInit: Boolean
) : CollectionsScreenVM(localFoldersRepo, loadRootFoldersOnInit) {

    fun addANewLink(link: Link, linkSaveConfig: LinkSaveConfig, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localLinksRepo.addANewLink(link, linkSaveConfig).collectLatest {
                it.onSuccess {
                    onCompletion()
                    Localization.Key.SavedTheLink.pushLocalizedSnackbar()
                }.onFailure {
                    onCompletion()
                    UIEvent.pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                }
            }
        }
    }
}