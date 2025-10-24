package com.sakethh.linkora.ui.screens.collections

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.Localization
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalTagsRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.domain.model.CollectionType
import com.sakethh.linkora.ui.domain.model.LinkTagsPair
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushLocalizedSnackbar
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.utils.getRemoteOnlyFailureMsg
import com.sakethh.linkora.utils.isNull
import com.sakethh.linkora.utils.pushSnackbarOnFailure
import com.sakethh.linkora.utils.replaceFirstPlaceHolderWith
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

open class CollectionsScreenVM(
    val localFoldersRepo: LocalFoldersRepo,
    val localLinksRepo: LocalLinksRepo,
    val localTagsRepo: LocalTagsRepo,
    loadNonArchivedRootFoldersOnInit: Boolean = true,
    loadArchivedRootFoldersOnInit: Boolean = true,
    val collectionDetailPaneInfo: CollectionDetailPaneInfo? = null,
    val platform: Platform,
    preferencesRepo: PreferencesRepository? = null
) : ViewModel() {

    companion object {
        val selectedLinkTagPairsViaLongClick = mutableStateListOf<LinkTagsPair>()
        val selectedFoldersViaLongClick = mutableStateListOf<Folder>()
        val isSelectionEnabled = mutableStateOf(false)

        fun clearAllSelections() {
            isSelectionEnabled.value = false
            selectedLinkTagPairsViaLongClick.clear()
            selectedFoldersViaLongClick.clear()
        }
    }

    private val _detailPaneHistory = MutableStateFlow<List<CollectionDetailPaneInfo>>(emptyList())

    val isPaneSelected = _detailPaneHistory.map { it.isNotEmpty() }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val peekPaneHistory = _detailPaneHistory.map {
        try {
            it.last()
        } catch (_: Exception) {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun clearDetailPaneHistoryUntilLast() {
        viewModelScope.launch {
            _detailPaneHistory.update {
                listOf(it.last())
            }
        }
    }

    fun clearDetailPaneHistory() {
        viewModelScope.launch {
            _detailPaneHistory.emit(emptyList())
        }
    }

    fun pushToDetailPane(collectionDetailPaneInfo: CollectionDetailPaneInfo) {
        _detailPaneHistory.update {
            it + collectionDetailPaneInfo
        }
    }

    fun popFromDetailPane(): CollectionDetailPaneInfo? {
        _detailPaneHistory.update {
            try {
                it.dropLast(1)
            } catch (_: Exception) {
                emptyList()
            }
        }
        return peekPaneHistory.value
    }

    private fun updateCollectionDetailPaneInfoAndCollectData(collectionDetailPaneInfo: CollectionDetailPaneInfo) {

        emptyCollectableLinks()
        emptyCollectableChildFolders()

        if (collectionDetailPaneInfo.collectionType == CollectionType.TAG && collectionDetailPaneInfo.currentTag != null) {
            updateCollectableLinks(tagId = collectionDetailPaneInfo.currentTag.localId)
            return
        }

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
                    LinkType.FOLDER_LINK, collectionDetailPaneInfo.currentFolder?.localId
                )
                updateCollectableChildFolders(collectionDetailPaneInfo.currentFolder?.localId!!)
            }
        }
    }

    private val _availableFiltersForAllLinks = MutableStateFlow(emptySet<LinkType>())
    val availableFiltersForAllLinks = _availableFiltersForAllLinks.asStateFlow()

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
            combine(
                snapshotFlow {
                    _appliedFiltersForAllLinks.toList()
                },
                snapshotFlow {
                    AppPreferences.selectedSortingTypeType.value
                },
                snapshotFlow {
                    AppPreferences.forceShuffleLinks.value
                },
            ) { appliedFilters, sortingType, forceShuffleLinks ->
                Triple(appliedFilters, sortingType, forceShuffleLinks)
            }.collectLatest { (appliedFilters, sortingType, forceShuffleLinks) ->
                localLinksRepo.sortAllLinks(sortingType).collectLatest {
                    it.onSuccess { result ->
                        _availableFiltersForAllLinks.emit(result.data.map { it.linkType }.toSet())
                        val filteredResults = result.data.filter {
                            appliedFilters.isEmpty() || it.linkType in appliedFilters
                        }
                        _linkTagsPairs.apply {
                            if (forceShuffleLinks) {
                                emit(filteredResults.shuffled().map {
                                    LinkTagsPair(
                                        link = it,
                                        tags = localTagsRepo.getTagsBasedOnTheLinkId(it.localId)
                                            .first()
                                    )
                                })
                            } else {
                                emit(filteredResults.map {
                                    LinkTagsPair(
                                        link = it,
                                        tags = localTagsRepo.getTagsBasedOnTheLinkId(it.localId)
                                            .first()
                                    )
                                })
                            }
                        }
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

    private val _allTags = MutableStateFlow<List<Tag>>(value = emptyList())
    val allTags = _allTags.asStateFlow()

    private val _selectedTags = mutableStateListOf<Tag>()
    val selectedTags: List<Tag> = _selectedTags

    fun selectATag(tag: Tag) {
        if (!_selectedTags.contains(tag)) {
            _selectedTags.add(tag)
        }
    }

    fun unSelectATag(tag: Tag) {
        _selectedTags.remove(tag)
    }

    fun createATag(tagName: String, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localTagsRepo.createATag(Tag(name = tagName)).collect()
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    private val _currentCollectionSource get() = if (AppPreferences.selectedCollectionSourceId == 0) Localization.Key.Folders.getLocalizedString() else "Tags"
    var currentCollectionSource by mutableStateOf(_currentCollectionSource)

    var foldersSearchQuery by mutableStateOf("")
    private val _foldersSearchQueryResult = MutableStateFlow(emptyList<Folder>())
    val foldersSearchQueryResult = _foldersSearchQueryResult.asStateFlow()

    init {
        viewModelScope.launch {
            combine(snapshotFlow {
                foldersSearchQuery
            }, snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }) { query, sortingType ->
                Pair(query, sortingType)
            }.cancellable().collectLatest { (query, sortingType) ->
                localFoldersRepo.search(query = query, sortOption = sortingType).collectLatest {
                    it.onSuccess {
                        _foldersSearchQueryResult.emit(it.data)
                    }
                }
            }
        }

        if (preferencesRepo != null) {
            viewModelScope.launch {
                snapshotFlow {
                    AppPreferences.selectedCollectionSourceId
                }.cancellable().collectLatest {
                    currentCollectionSource = _currentCollectionSource
                    preferencesRepo.changePreferenceValue(
                        preferenceKey = intPreferencesKey(
                            AppPreferenceType.COLLECTION_SOURCE_ID.name
                        ), newValue = it
                    )
                }
            }

            viewModelScope.launch {
                snapshotFlow {
                    AppPreferences.showTagsInAddNewLinkDialogBox
                }.cancellable().collectLatest {
                    preferencesRepo.changePreferenceValue(
                        preferenceKey = booleanPreferencesKey(
                            AppPreferenceType.SHOW_TAGS_BY_DEFAULT_IN_ADD_LINK.name
                        ), newValue = it
                    )
                }
            }
        }
        viewModelScope.launch {
            snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }.flatMapLatest {
                localTagsRepo.getAllTags(it)
            }.collectLatest {
                _allTags.emit(it)
            }
        }

        viewModelScope.launch {

            if (loadNonArchivedRootFoldersOnInit && loadArchivedRootFoldersOnInit) {
                loadFolders { folders ->
                    _rootArchiveFolders.emit(folders.filter { it.isArchived })
                    _rootRegularFolders.emit(folders.filterNot { it.isArchived })
                }
            }

            if (loadArchivedRootFoldersOnInit && !loadNonArchivedRootFoldersOnInit) {
                loadFolders { folders ->
                    _rootArchiveFolders.emit(folders.filter { it.isArchived })
                }
            }

            if (loadArchivedRootFoldersOnInit.not() && loadNonArchivedRootFoldersOnInit) {
                loadFolders { folders ->
                    _rootRegularFolders.emit(folders.filterNot { it.isArchived })
                }
            }
        }
        if (collectionDetailPaneInfo != null) {
            updateCollectionDetailPaneInfoAndCollectData(collectionDetailPaneInfo)
        } else {
            viewModelScope.launch {
                peekPaneHistory.collectLatest {
                    if (it != null) {
                        updateCollectionDetailPaneInfoAndCollectData(it)
                    }
                }
            }
        }
    }

    private suspend fun loadFolders(init: suspend (folders: List<Folder>) -> Unit) {
        snapshotFlow {
            AppPreferences.selectedSortingTypeType.value
        }.flatMapLatest { sortingType ->
            localFoldersRepo.getRootFolders(sortingType)
        }.collectLatest { result ->
            result.onSuccess { folders ->
                init(folders.data)
            }.pushSnackbarOnFailure()
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
            snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }.flatMapLatest { sortingType ->
                localFoldersRepo.getChildFolders(parentFolderId, sortingType)
            }.collectAndEmitChildFolders()
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
                                    .replaceFirstPlaceHolderWith(folder.name) + it.getRemoteOnlyFailureMsg()
                            )
                        )
                    }.onFailure {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(message = it))
                    }
                }
        }.invokeOnCompletion {
            onCompletion()
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
                            )
                                .replaceFirstPlaceHolderWith(folder.name) + it.getRemoteOnlyFailureMsg()
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
            localLinksRepo.deleteALink(link.localId).collectLatest {
                it.onSuccess {
                    Localization.Key.DeletedTheLink.pushLocalizedSnackbar(append = it.getRemoteOnlyFailureMsg())
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun deleteTheNote(folder: Folder, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localFoldersRepo.deleteAFolderNote(folder.localId).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            Localization.getLocalizedString(
                                Localization.Key.DeletedTheNoteOfAFolder
                            )
                                .replaceFirstPlaceHolderWith(folder.name) + it.getRemoteOnlyFailureMsg()
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun deleteTheNote(link: Link, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localLinksRepo.deleteALinkNote(link.localId).collectLatest {
                it.onSuccess {
                    Localization.Key.DeletedTheNoteOfALink.pushLocalizedSnackbar(append = it.getRemoteOnlyFailureMsg())
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
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
                                )
                                    .replaceFirstPlaceHolderWith(folder.name) + it.getRemoteOnlyFailureMsg()
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
                                )
                                    .replaceFirstPlaceHolderWith(folder.name) + it.getRemoteOnlyFailureMsg()
                            )
                        )
                    }.pushSnackbarOnFailure()
                }
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun markALinkAsImp(link: Link, tagIds: List<Long>?, onCompletion: () -> Unit) {
        viewModelScope.launch {
            if (link.linkType == LinkType.IMPORTANT_LINK) {
                deleteALink(link, onCompletion = {})
                return@launch
            }
            localLinksRepo.addANewLink(
                link = link.copy(
                    idOfLinkedFolder = Constants.IMPORTANT_LINKS_ID,
                    localId = 0, linkType = LinkType.IMPORTANT_LINK,
                ),
                linkSaveConfig = LinkSaveConfig.forceSaveWithoutRetrieving(),
                selectedTagIds = tagIds
            ).collectLatest {
                it.onSuccess {
                    pushUIEvent(UIEvent.Type.ShowSnackbar("Added Copy to Important Links"))
                }
                it.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun refreshLinkMetadata(link: Link, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localLinksRepo.refreshLinkMetadata(link).collectLatest {
                it.onSuccess {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.LinkRefreshedSuccessfully.getLocalizedString() + it.getRemoteOnlyFailureMsg()))
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun archiveALink(link: Link, onCompletion: () -> Unit) {
        viewModelScope.launch {
            if (link.linkType == LinkType.ARCHIVE_LINK) {
                // we can also revert to the same folder from where it was originally archived, but this should be fine
                localLinksRepo.updateALink(
                    link = link.copy(
                        linkType = LinkType.SAVED_LINK, idOfLinkedFolder = null
                    ), updatedLinkTagsPair = null
                ).collectLatest {
                    it.onSuccess {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.UnArchived.getLocalizedString() + it.getRemoteOnlyFailureMsg()))
                    }
                    it.pushSnackbarOnFailure()
                }
            } else {
                localLinksRepo.archiveALink(link.localId).collectLatest {
                    it.onSuccess {
                        Localization.Key.ArchivedTheLink.pushLocalizedSnackbar(append = it.getRemoteOnlyFailureMsg())
                    }.pushSnackbarOnFailure()
                }
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun updateLink(updatedLinkTagsPair: LinkTagsPair, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localLinksRepo.updateALink(
                link = updatedLinkTagsPair.link, updatedLinkTagsPair = updatedLinkTagsPair
            ).collectLatest {
                it.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun updateFolder(newFolderData: Folder, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localFoldersRepo.updateFolder(newFolderData).collectLatest {
                it.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    private val _linkTagsPairs = MutableStateFlow(emptyList<LinkTagsPair>())
    val linkTagsPairs = _linkTagsPairs.asStateFlow()

    private var collectableLinksJob: Job? = null

    private fun emptyCollectableLinks() {
        viewModelScope.launch(Dispatchers.Main) {
            _linkTagsPairs.emit(emptyList())
        }
    }


    suspend fun Flow<Result<List<Link>>>.collectAndEmitLinks(emitter: MutableStateFlow<List<LinkTagsPair>>) {
        val shufflePreferenceFlow = snapshotFlow { AppPreferences.forceShuffleLinks.value }

        val linkTagsPairFlow = combine(this, shufflePreferenceFlow) { linksResult, shouldShuffle ->
            linksResult to shouldShuffle
        }.flatMapLatest { (linksResult, shouldShuffle) ->
            when (linksResult) {
                is Result.Loading, is Result.Failure -> flowOf(emptyList())
                is Result.Success -> {
                    val links = linksResult.data
                    if (links.isEmpty()) {
                        flowOf(emptyList())
                    } else {
                        val linksIds = links.map { it.localId }
                        localTagsRepo.getTagsForLinks(linksIds).map { tagsMap ->
                            val pairs = links.map { link ->
                                LinkTagsPair(
                                    link = link, tags = tagsMap[link.localId] ?: emptyList()
                                )
                            }
                            if (shouldShuffle) pairs.shuffled() else pairs
                        }
                    }
                }
            }
        }
        linkTagsPairFlow.collectLatest {
            emitter.emit(it)
        }
    }

    private fun updateCollectableLinks(linkType: LinkType, folderId: Long? = null) {
        collectableLinksJob?.cancel()
        collectableLinksJob = viewModelScope.launch(Dispatchers.Main) {
            _linkTagsPairs.emit(emptyList())
            snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }.collectLatest { sortingType ->
                if (folderId.isNull()) {
                    localLinksRepo.getSortedLinks(linkType, sortingType)
                } else {
                    localLinksRepo.getSortedLinks(linkType, folderId!!, sortingType)
                }.collectAndEmitLinks(_linkTagsPairs)
            }
        }
    }

    private fun updateCollectableLinks(tagId: Long) {
        collectableLinksJob?.cancel()
        collectableLinksJob = viewModelScope.launch(Dispatchers.Main) {
            _linkTagsPairs.emit(emptyList())
            snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }.collectLatest { sortingType ->
                localLinksRepo.getSortedLinks(tagId = tagId, sortOption = sortingType)
                    .collectAndEmitLinks(emitter = _linkTagsPairs)
            }
        }
    }

    fun clearSelectedTags() {
        _selectedTags.clear()
    }

    fun deleteATag(tagId: Long, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localTagsRepo.deleteATag(tagId).collect()
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun renameATag(localId: Long, newName: String, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localTagsRepo.renameATag(
                localTagId = localId, newName = newName
            ).collect()
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun addANewLink(
        link: Link,
        selectedTags: List<Tag>?,
        linkSaveConfig: LinkSaveConfig,
        onCompletion: () -> Unit,
        pushSnackbarOnSuccess: Boolean = true
    ) {
        viewModelScope.launch {
            localLinksRepo.addANewLink(
                link = link, selectedTagIds = selectedTags?.map {
                    it.localId
                }, linkSaveConfig = linkSaveConfig
            ).collectLatest {
                it.onSuccess {
                    onCompletion()
                    if (pushSnackbarOnSuccess) {
                        Localization.Key.SavedTheLink.pushLocalizedSnackbar(append = it.getRemoteOnlyFailureMsg())
                    }
                    clearSelectedTags()
                }.onFailure {
                    onCompletion()
                    UIEvent.pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                    clearSelectedTags()
                }
            }
        }
    }
}