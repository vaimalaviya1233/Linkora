package com.sakethh.linkora.ui.screens.collections

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.common.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushLocalizedSnackbar
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

open class CollectionsScreenVM(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    loadRootFoldersOnInit: Boolean = true
) : ViewModel() {

    private val _rootFolders = MutableStateFlow(emptyList<Folder>())
    val rootFolders = _rootFolders.asStateFlow()

    init {
        linkoraLog("CollectionScreenVM initialized with loadRootFoldersOnInit: $loadRootFoldersOnInit")
        if (loadRootFoldersOnInit) {
            viewModelScope.launch {
                localFoldersRepo.getAllRootFoldersAsFlow().collectLatest {
                    it.onSuccess {
                        _rootFolders.emit(it.data)
                    }.pushSnackbarOnFailure()
                }
            }
        }
    }

    private val _childFolders = MutableStateFlow(emptyList<Folder>())
    val childFolders = _childFolders.asStateFlow()

    private var collectableChildFoldersJob: Job? = null

    fun emptyCollectableChildFolders() {
        viewModelScope.launch {
            _childFolders.emit(emptyList())
        }
    }

    private suspend fun Flow<Result<List<Folder>>>.collectAndEmitChildFolders() {
        return this.cancellable().collectLatest {
            it.onSuccess {
                _childFolders.emit(it.data)
            }.pushSnackbarOnFailure()
        }
    }

    fun updateCollectableChildFolders(parentFolderId: Long) {
        collectableChildFoldersJob?.cancel()
        collectableChildFoldersJob = viewModelScope.launch {
            _childFolders.emit(emptyList())
            localFoldersRepo.getChildFoldersOfThisParentIDAsFlow(parentFolderId)
                .collectAndEmitChildFolders()
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

    fun updateFolderNote(folderId: Long, newNote: String, pushSnackbarOnSuccess: Boolean = true) {
        viewModelScope.launch {
            localFoldersRepo.renameAFolderNote(folderId, newNote).collectLatest {
                it.onSuccess {
                    if (pushSnackbarOnSuccess) {
                        pushUIEvent(
                            UIEvent.Type.ShowSnackbar(
                                Localization.getLocalizedString(
                                    Localization.Key.UpdatedTheNote
                                )
                            )
                        )
                    }
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun updateFolderName(
        folder: Folder,
        newName: String,
        ignoreFolderAlreadyExistsThrowable: Boolean
    ) {
        viewModelScope.launch {
            localFoldersRepo.renameAFolderName(
                folderID = folder.id,
                existingFolderName = folder.name,
                newFolderName = newName,
                ignoreFolderAlreadyExistsException = ignoreFolderAlreadyExistsThrowable
            ).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.getLocalizedString(
                                Localization.Key.UpdatedTheFolderData
                            )
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }
    }

    private val _links = MutableStateFlow(emptyList<Link>())
    val links = _links.asStateFlow()

    private var collectableLinksJob: Job? = null

    fun emptyCollectableLinks() {
        viewModelScope.launch {
            _links.emit(emptyList())
        }
    }

    private suspend fun Flow<Result<List<Link>>>.collectAndEmitLinks() {
        return this.cancellable().collectLatest {
            it.onSuccess {
                _links.emit(it.data)
            }.pushSnackbarOnFailure()
        }
    }

    fun updateCollectableLinks(linkType: LinkType, folderId: Long? = null) {
        collectableLinksJob?.cancel()
        collectableLinksJob = viewModelScope.launch {
            _links.emit(emptyList())
            when (linkType) {
                LinkType.SAVED_LINK -> {
                    localLinksRepo.getAllSavedLinks().collectAndEmitLinks()
                }

                LinkType.FOLDER_LINK -> {
                    if (folderId.isNull()) return@launch
                    localLinksRepo.getLinksFromFolder(folderId as Long).collectAndEmitLinks()
                }

                LinkType.HISTORY_LINK -> {

                }

                LinkType.IMPORTANT_LINK -> {
                    localLinksRepo.getAllImportantLinks().collectAndEmitLinks()
                }

                LinkType.ARCHIVE_LINK -> {

                }
            }
        }
    }

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