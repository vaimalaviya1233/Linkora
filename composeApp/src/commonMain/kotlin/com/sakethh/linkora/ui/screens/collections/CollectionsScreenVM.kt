package com.sakethh.linkora.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.FoldersRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CollectionsScreenVM(
    private val foldersRepo: FoldersRepo
) : ViewModel() {

    private val _rootFolders = MutableStateFlow(emptyList<Folder>())
    val rootFolders = _rootFolders.asStateFlow()

    init {
        viewModelScope.launch {
            foldersRepo.getAllRootFoldersAsFlow().collectLatest {
                it.onSuccess {
                    _rootFolders.emit(it)
                }
            }
        }
    }

    fun insertANewFolder(
        folder: Folder, ignoreFolderAlreadyExistsThrowable: Boolean, onCompletion: () -> Unit
    ) {
        viewModelScope.launch {
            foldersRepo.insertANewFolder(folder, ignoreFolderAlreadyExistsThrowable).collectLatest {
                it.onSuccess {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = "The folder \"${folder.name}\" has been successfully created."))
                    onCompletion()
                }.onFailure {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = it))
                    onCompletion()
                }
            }
        }
    }
}