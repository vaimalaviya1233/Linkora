package com.sakethh.linkora.ui.utils

import com.sakethh.linkora.Localization
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

/**
There's also `AndroidUIEvent`, which I initially created for handling Android-specific APIs and behavior.
But now, some of that functionality isn't strictly Android-specific and can be managed in common code.
So this setup feels fine for now. Even though there are two different event objects—one for common and one for Android-specific cases—
I could eventually merge them into this single `UIEvent` object. For now, it's not a problem.
 **/
object UIEvent {
    private val _uiEvents = MutableSharedFlow<Type>() // `StateFlow` won't emit the same value again, so to make sure we're playing it safe, `SharedFlow` is the way 🤪
    val uiEvents = _uiEvents.asSharedFlow()

    suspend fun pushUIEvent(type: Type) {
        _uiEvents.emit(type)
    }

    fun CoroutineScope.pushUIEvent(type: Type) {
        this.launch {
            _uiEvents.emit(type)
        }
    }

    suspend fun Localization.Key.pushLocalizedSnackbar(append: String = "") {
        _uiEvents.emit(Type.ShowSnackbar(this.getLocalizedString() + append))
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

        data object MinimizeTheApp : Type

        data object Nothing : Type
    }

}