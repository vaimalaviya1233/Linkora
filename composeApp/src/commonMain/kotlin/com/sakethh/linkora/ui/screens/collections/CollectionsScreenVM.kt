package com.sakethh.linkora.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.common.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class CollectionsScreenVM(
    private val localFoldersRepo: LocalFoldersRepo
) : ViewModel() {

    private val _rootFolders = MutableStateFlow(emptyList<Folder>())
    val rootFolders = _rootFolders.asStateFlow()

    init {
        viewModelScope.launch {
            localFoldersRepo.getAllRootFoldersAsFlow().collectLatest {
                it.onSuccess {
                    _rootFolders.emit(it.data)
                }
            }
        }
    }

    fun insertANewFolder(
        folder: Folder, ignoreFolderAlreadyExistsThrowable: Boolean, onCompletion: () -> Unit
    ) {
        viewModelScope.launch {
            localFoldersRepo.insertANewFolder(folder, ignoreFolderAlreadyExistsThrowable)
                .collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            message = Localization.Key.FolderHasBeenCreatedSuccessful.getLocalizedString()
                                .replaceFirstPlaceHolderWith(folder.name) + if (it.isRemoteExecutionSuccessful.not()) "\n\n${Localization.Key.RemoteExecutionFailed.getLocalizedString()}\n" + it.remoteFailureMessage else ""
                        )
                    )
                    onCompletion()
                }.onFailure {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = it))
                    onCompletion()
                }
            }
        }
    }

    fun deleteAFolder(folder: Folder) {
        viewModelScope.launch {
            localFoldersRepo.deleteAFolder(folderID = folder.id).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.getLocalizedString(
                                Localization.Key.DeletedTheFolder
                            ).replaceFirstPlaceHolderWith(folder.name)
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun deleteTheNote(folder: Folder) {
        viewModelScope.launch {
            localFoldersRepo.deleteAFolderNote(folder.id).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.getLocalizedString(
                                Localization.Key.DeletedTheNote
                            ).replaceFirstPlaceHolderWith(folder.name)
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun archiveAFolder(folder: Folder) {
        viewModelScope.launch {
            localFoldersRepo.markFolderAsArchive(folder.id).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.getLocalizedString(
                                Localization.Key.ArchivedTheFolder
                            ).replaceFirstPlaceHolderWith(folder.name)
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }
    }
}