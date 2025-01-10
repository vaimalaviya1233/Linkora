package com.sakethh.linkora.ui.utils

import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

object UIEvent {
    private val _uiChannel = Channel<Type>()
    val uiEventsReadOnlyChannel = _uiChannel.receiveAsFlow()

    suspend fun pushUIEvent(type: Type) {
        _uiChannel.send(type)
    }

    fun CoroutineScope.pushUIEvent(type: Type) {
        this.launch {
            _uiChannel.send(type)
        }
    }

    suspend fun Localization.Key.pushLocalizedSnackbar() {
        _uiChannel.send(Type.ShowSnackbar(this.getLocalizedString()))
    }

    sealed interface Type {
        data class ShowSnackbar(val message: String) : Type

        data object ShowAddANewLinkDialogBox : Type

        data object ShowAddANewFolderDialogBox : Type

        data class ShowMenuBtmSheetUI(
            val menuBtmSheetFor: MenuBtmSheetType,
            val selectedLinkForMenuBtmSheet: Link?,
            val selectedFolderForMenuBtmSheet: Folder?
        ) : Type

        data object ShowSortingBtmSheetUI : Type

        data object ShowDeleteDialogBox : Type

        data object ShowRenameDialogBox : Type
    }

}