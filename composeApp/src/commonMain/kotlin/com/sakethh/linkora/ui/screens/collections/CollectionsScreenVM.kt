package com.sakethh.linkora.ui.screens.collections

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.isNotNull
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
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetVM
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushLocalizedSnackbar
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class CollectionsScreenVM(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    loadRootFoldersOnInit: Boolean = true,
    collectionDetailPaneInfo: CollectionDetailPaneInfo? = null
) : ViewModel() {

    companion object {
        private val _collectionDetailPaneInfo = mutableStateOf(
            CollectionDetailPaneInfo(
            currentFolder = null, isAnyCollectionSelected = false
            )
        )
        val collectionDetailPaneInfo = _collectionDetailPaneInfo

        fun updateCollectionDetailPaneInfo(collectionDetailPaneInfo: CollectionDetailPaneInfo) {
            _collectionDetailPaneInfo.value = collectionDetailPaneInfo
        }

        fun resetCollectionDetailPaneInfo() {
            _collectionDetailPaneInfo.value = CollectionDetailPaneInfo(
                currentFolder = null, isAnyCollectionSelected = false
            )
        }
    }

    fun updateCollectionDetailPaneInfoAndCollectData(collectionDetailPaneInfo: CollectionDetailPaneInfo) {
        _collectionDetailPaneInfo.value = collectionDetailPaneInfo

        if (collectionDetailPaneInfo.isAnyCollectionSelected.not()) return

        emptyCollectableLinks()
        emptyCollectableChildFolders()

        when (collectionDetailPaneInfo.currentFolder?.localId) {
            Constants.SAVED_LINKS_ID -> {
                updateCollectableLinks(LinkType.SAVED_LINK)
            }

            Constants.IMPORTANT_LINKS_ID -> {
                updateCollectableLinks(LinkType.IMPORTANT_LINK)
            }

            Constants.ALL_LINKS_ID -> {
                updateFilterableAllLinks()
            }

            Constants.ARCHIVE_ID -> {
                updateCollectableLinks(LinkType.ARCHIVE_LINK)
            }

            Constants.HISTORY_ID -> {
                updateCollectableLinks(LinkType.HISTORY_LINK)
            }

            else -> {
                updateCollectableLinks(
                    LinkType.FOLDER_LINK,
                    collectionDetailPaneInfo.currentFolder?.localId
                )
                updateCollectableChildFolders(collectionDetailPaneInfo.currentFolder?.localId!!)
            }
        }
    }

    private val _availableFiltersForAllLinks = mutableStateListOf<LinkType>()
    val availableFiltersForAllLinks = _availableFiltersForAllLinks

    private val _appliedFiltersForAllLinks = mutableStateListOf<LinkType>()
    val appliedFiltersForAllLinks = _appliedFiltersForAllLinks

    fun toggleAllLinksFilter(filter: LinkType) {
        if (_appliedFiltersForAllLinks.contains(filter).not()) {
            _appliedFiltersForAllLinks.add(filter)
        } else {
            _appliedFiltersForAllLinks.remove(filter)
        }
    }

    private fun updateFilterableAllLinks() {
        collectableLinksJob?.cancel()
        collectableLinksJob = viewModelScope.launch {
            combine(snapshotFlow {
                _appliedFiltersForAllLinks.toList()
            }, snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }) { appliedFilters, sortingType ->
                appliedFilters to sortingType
            }.collectLatest { (appliedFilters, sortingType) ->
                _availableFiltersForAllLinks.clear()
                localLinksRepo.sortAllLinks(sortingType).collectLatest {
                    it.onSuccess { result ->
                        _availableFiltersForAllLinks.addAll(result.data.map { it.linkType }
                            .distinct())
                        val filteredResults = result.data.filter {
                            appliedFilters.isEmpty() || it.linkType in appliedFilters
                        }
                        _links.emit(filteredResults)
                    }
                    it.pushSnackbarOnFailure()
                }
            }
        }
    }

    private val _rootRegularFolders = MutableStateFlow(emptyList<Folder>())
    val rootRegularFolders = _rootRegularFolders.asStateFlow()

    private val _rootArchiveFolders = MutableStateFlow(emptyList<Folder>())
    val rootArchiveFolders = _rootArchiveFolders.asStateFlow()

    private fun Flow<Result<List<Folder>>>.collectAndEmitRootFolders() {
        viewModelScope.launch {
            collectLatest { result ->
                result.onSuccess { folders ->
                    _rootArchiveFolders.emit(folders.data.filter { it.isArchived })
                    _rootRegularFolders.emit(folders.data.filterNot { it.isArchived })
                }.pushSnackbarOnFailure()
            }
        }
    }

    private val triggerForFoldersSorting = mutableStateOf(false)
    private val triggerForLinksSorting = mutableStateOf(false)

    fun triggerFoldersSorting() {
        triggerForFoldersSorting.value = triggerForFoldersSorting.value.not()
    }

    fun triggerLinksSorting() {
        triggerForLinksSorting.value = triggerForLinksSorting.value.not()
    }

    init {
        linkoraLog("CollectionScreenVM initialized with loadRootFoldersOnInit: $loadRootFoldersOnInit")
        if (loadRootFoldersOnInit) {
            combine(snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }, snapshotFlow {
                triggerForFoldersSorting.value
            }) { sortingType, _ ->
                localFoldersRepo.sortFolders(sortingType).collectAndEmitRootFolders()
            }.launchIn(viewModelScope)
        }
        if (collectionDetailPaneInfo.isNotNull()) {
            updateCollectionDetailPaneInfoAndCollectData(collectionDetailPaneInfo!!)
        }
    }

    private val _childFolders = MutableStateFlow(emptyList<Folder>())
    val childFolders = _childFolders.asStateFlow()

    private var collectableChildFoldersJob: Job? = null

    private fun emptyCollectableChildFolders() {
        viewModelScope.launch(Dispatchers.Main) {
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

    private fun updateCollectableChildFolders(parentFolderId: Long) {
        collectableChildFoldersJob?.cancel()
        collectableChildFoldersJob = viewModelScope.launch {
            combine(snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }, snapshotFlow {
                triggerForFoldersSorting.value
            }) { sortingType, _ ->
                sortingType
            }.collectLatest { sortingType ->
                localFoldersRepo.sortFolders(parentFolderId, sortingType)
                    .collectAndEmitChildFolders()
            }
        }
    }

    fun getFolder(id: Long): Folder {
        lateinit var folder: Folder
        runBlocking {
            localFoldersRepo.getThisFolderData(id).collectLatest {
                it.onSuccess {
                    folder = it.data
                }.pushSnackbarOnFailure()
            }
        }
        return folder
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

    fun deleteAFolder(folder: Folder, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localFoldersRepo.deleteAFolder(folderID = folder.localId).collectLatest {
                it.onSuccess {
                    onCompletion()
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.getLocalizedString(
                                Localization.Key.DeletedTheFolder
                            ).replaceFirstPlaceHolderWith(folder.name)
                        )
                    )
                }.onFailure {
                    onCompletion()
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun deleteALink(link: Link, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localLinksRepo.deleteALink(link.id).collectLatest {
                it.onSuccess {
                    Localization.Key.DeletedTheLink.pushLocalizedSnackbar()
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun deleteTheNote(folder: Folder) {
        viewModelScope.launch {
            localFoldersRepo.deleteAFolderNote(folder.localId).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.getLocalizedString(
                                Localization.Key.DeletedTheNoteOfAFolder
                            ).replaceFirstPlaceHolderWith(folder.name)
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun deleteTheNote(link: Link) {
        viewModelScope.launch {
            localLinksRepo.deleteALinkNote(link.id).collectLatest {
                it.onSuccess {
                    Localization.Key.DeletedTheNoteOfALink.pushLocalizedSnackbar()
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun archiveAFolder(folder: Folder, onCompletion: () -> Unit) {
        viewModelScope.launch {
            if (folder.isArchived) {
                localFoldersRepo.markFolderAsRegularFolder(folder.localId).collectLatest {
                    it.onSuccess {
                        pushUIEvent(
                            UIEvent.Type.ShowSnackbar(
                                Localization.getLocalizedString(
                                    Localization.Key.UnArchivedTheFolder
                                ).replaceFirstPlaceHolderWith(folder.name)
                            )
                        )
                    }.pushSnackbarOnFailure()
                }
            } else {
                localFoldersRepo.markFolderAsArchive(folder.localId).collectLatest {
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
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun markALinkAsImp(link: Link) {
        viewModelScope.launch {
            if (MenuBtmSheetVM.isCurrentLinkMarkedAsImp) {
                if (link.linkType == LinkType.IMPORTANT_LINK) {
                    deleteALink(link, onCompletion = {})
                } else {
                    localLinksRepo.updateALink(link.copy(markedAsImportant = false)).collectLatest {
                        it.pushSnackbarOnFailure()
                    }
                }
            } else {
                localLinksRepo.updateALink(link.copy(markedAsImportant = true)).collectLatest {
                    it.pushSnackbarOnFailure()
                }
            }
        }
    }

    fun refreshLinkMetadata(link: Link) {
        viewModelScope.launch {
            localLinksRepo.refreshLinkMetadata(link).collectLatest {
                it.onSuccess {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.LinkRefreshedSuccessfully.getLocalizedString()))
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun archiveALink(link: Link, onCompletion: () -> Unit) {
        viewModelScope.launch {
            if (link.linkType == LinkType.ARCHIVE_LINK) {
                // we can also revert to the same folder from where it was originally archived, but this should be fine
                localLinksRepo.updateALink(
                    link.copy(
                        linkType = LinkType.SAVED_LINK, idOfLinkedFolder = null
                    )
                ).collectLatest {
                    it.onSuccess {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.UnArchived.getLocalizedString()))
                    }
                    it.pushSnackbarOnFailure()
                }
            } else {
                localLinksRepo.archiveALink(link.id).collectLatest {
                    it.onSuccess {
                        Localization.Key.ArchivedTheLink.pushLocalizedSnackbar()
                    }.pushSnackbarOnFailure()
                }
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun updateFolderNote(
        folderId: Long,
        newNote: String,
        onCompletion: () -> Unit,
        pushSnackbarOnSuccess: Boolean = true
    ) {
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
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun updateLinkNote(
        linkId: Long,
        newNote: String,
        onCompletion: () -> Unit,
        pushSnackbarOnSuccess: Boolean = true
    ) {
        viewModelScope.launch {
            localLinksRepo.updateLinkNote(linkId, newNote).collectLatest {
                it.onSuccess {
                    if (pushSnackbarOnSuccess) {
                        Localization.Key.UpdatedTheNote.pushLocalizedSnackbar()
                    }
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun updateLinkTitle(linkId: Long, newTitle: String, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localLinksRepo.updateLinkTitle(linkId, newTitle).collectLatest {
                it.onSuccess {
                    Localization.Key.UpdatedTheTitle.pushLocalizedSnackbar()
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun updateFolderName(
        folder: Folder,
        newName: String, ignoreFolderAlreadyExistsThrowable: Boolean, onCompletion: () -> Unit
    ) {
        viewModelScope.launch {
            localFoldersRepo.renameAFolderName(
                folderID = folder.localId,
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
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    private val _links = MutableStateFlow(emptyList<Link>())
    val links = _links.asStateFlow()

    private var collectableLinksJob: Job? = null

    private fun emptyCollectableLinks() {
        viewModelScope.launch(Dispatchers.Main) {
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

    private fun updateCollectableLinks(linkType: LinkType, folderId: Long? = null) {
        collectableLinksJob?.cancel()
        collectableLinksJob = viewModelScope.launch(Dispatchers.Main) {
            _links.emit(emptyList())
            combine(snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }, snapshotFlow {
                triggerForLinksSorting.value
            }) { sortingType, _ ->
                sortingType
            }.collectLatest { sortingType ->
                if (folderId.isNull()) {
                    localLinksRepo.sortLinks(linkType, sortingType)
                } else {
                    localLinksRepo.sortLinks(linkType, folderId!!, sortingType)
                }.collectAndEmitLinks()
            }
        }
    }

    fun addANewLink(
        link: Link,
        linkSaveConfig: LinkSaveConfig,
        onCompletion: () -> Unit,
        pushSnackbarOnSuccess: Boolean = true
    ) {
        viewModelScope.launch {
            localLinksRepo.addANewLink(link, linkSaveConfig).collectLatest {
                it.onSuccess {
                    onCompletion()
                    if (pushSnackbarOnSuccess) {
                        Localization.Key.SavedTheLink.pushLocalizedSnackbar()
                    }
                }.onFailure {
                    onCompletion()
                    UIEvent.pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                }
            }
        }
    }
}