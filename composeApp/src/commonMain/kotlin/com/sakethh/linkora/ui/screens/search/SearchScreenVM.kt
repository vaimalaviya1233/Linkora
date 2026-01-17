package com.sakethh.linkora.ui.screens.search

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.domain.FolderType
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalTagsRepo
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.PageKey
import com.sakethh.linkora.ui.Paginator
import com.sakethh.linkora.ui.domain.PaginationState
import com.sakethh.linkora.ui.domain.model.LinkTagsPair
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.asStateInWhileSubscribed
import com.sakethh.linkora.utils.getRemoteOnlyFailureMsg
import com.sakethh.linkora.utils.ifNot
import com.sakethh.linkora.utils.onError
import com.sakethh.linkora.utils.onPagesFinished
import com.sakethh.linkora.utils.onRetrieving
import com.sakethh.linkora.utils.onRetrieved
import com.sakethh.linkora.utils.pushSnackbarOnFailure
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableMap
import java.util.TreeMap

class SearchScreenVM(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    private val localTagsRepo: LocalTagsRepo
) : ViewModel() {

    private val _searchQuery = mutableStateOf("")
    val searchQuery = _searchQuery

    private val _isSearchActive = mutableStateOf(false)
    val isSearchActive = _isSearchActive

    private var searchQueryResultsJob: Job? = null

    fun updateSearchActiveState(isActive: Boolean) {
        _isSearchActive.value = isActive
        isActive.ifNot {
            searchQueryResultsJob?.cancel()
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    private val _linkQueryResults = MutableStateFlow(emptyList<LinkTagsPair>())
    val linkQueryResults = _linkQueryResults.asStateFlow()

    private val _folderQueryResults = MutableStateFlow(emptyList<Folder>())
    val folderQueryResults = _folderQueryResults.asStateFlow()

    private val _tagQueryResults = MutableStateFlow(emptyList<Tag>())
    val tagQueryResults = _tagQueryResults.asStateFlow()

    private val _appliedLinkFilters = mutableStateListOf<LinkType>()
    val appliedLinkFilters = _appliedLinkFilters

    private val _availableLinkFilters = MutableStateFlow(emptySet<LinkType>())
    val availableLinkFilters = _availableLinkFilters.asStateFlow()

    private val _appliedFolderFilters = mutableStateListOf<FolderType>()
    val appliedFolderFilters = _appliedFolderFilters

    private val _availableFolderFilters = MutableStateFlow(emptySet<FolderType>())
    val availableFolderFilters = _availableFolderFilters.asStateFlow()

    private val _tagsAvailableForFiltering = mutableStateOf(false)
    val tagsAvailableForFiltering by _tagsAvailableForFiltering
    private val _appliedTagFiltering = mutableStateOf(false)
    val appliedTagFiltering by _appliedTagFiltering

    fun toggleLinkFilter(filter: LinkType) {
        if (_appliedLinkFilters.contains(filter).not()) {
            _appliedLinkFilters.add(filter)
        } else {
            _appliedLinkFilters.remove(filter)
        }
    }

    fun toggleTagFilter() {
        _appliedTagFiltering.value = !_appliedTagFiltering.value
    }

    fun toggleFolderFilter(filter: FolderType) {
        if (_appliedFolderFilters.contains(filter).not()) {
            _appliedFolderFilters.add(filter)
        } else {
            _appliedFolderFilters.remove(filter)
        }
    }

    init {
        viewModelScope.launch {
            combine(
                snapshotFlow { _searchQuery.value },
                snapshotFlow { AppPreferences.selectedSortingTypeType.value },
                snapshotFlow { _appliedFolderFilters.toList() },
                snapshotFlow { _appliedLinkFilters.toList() },
                snapshotFlow { _appliedTagFiltering.value }) { query, selectedSortingType, appliedFolderFilters, appliedLinksFilters, isTagFilteringApplied ->
                AllInputs(
                    query = query,
                    sortingType = selectedSortingType,
                    appliedFolderFilters = appliedFolderFilters,
                    appliedLinkFilters = appliedLinksFilters,
                    isTagFilterApplied = isTagFilteringApplied
                )
            }.flatMapLatest { allInputs ->

                if (allInputs.query.isBlank()) {
                    _linkQueryResults.emit(emptyList())
                    _folderQueryResults.emit(emptyList())
                    _tagQueryResults.emit(emptyList())
                    return@flatMapLatest emptyFlow()
                }
                val linkTagsPairFlow = localLinksRepo.search(
                    allInputs.query,
                    AppPreferences.selectedSortingTypeType.value
                ).flatMapLatest {
                    when (it) {
                        is Result.Failure<*> -> flowOf()
                        is Result.Loading<*> -> flowOf()
                        is Result.Success<List<Link>> -> {
                            val linkSearchResults = it.data
                            val linkIds = linkSearchResults.map { it.localId }
                            localTagsRepo.getTagsForLinks(linkIds).map { tagsMap ->
                                linkSearchResults.filter {
                                    !allInputs.isTagFilterApplied && (allInputs.appliedLinkFilters.isEmpty() || it.linkType in allInputs.appliedLinkFilters)
                                }.map {
                                    LinkTagsPair(
                                        link = it, tags = tagsMap[it.localId] ?: emptyList()
                                    )
                                }
                            }
                        }
                    }
                }

                val foldersFlow = localFoldersRepo.search(
                    allInputs.query,
                    AppPreferences.selectedSortingTypeType.value
                ).map {
                    when (it) {
                        is Result.Failure -> emptyList()
                        is Result.Loading -> emptyList()
                        is Result.Success<List<Folder>> -> it.data
                    }
                }
                val tagsFlow = localTagsRepo.search(
                    query = allInputs.query,
                    sortOption = AppPreferences.selectedSortingTypeType.value
                )
                combine(linkTagsPairFlow, foldersFlow, tagsFlow) { linkTagsPairs, folders, tags ->
                    _availableLinkFilters.emit(linkTagsPairs.map { it.link.linkType }.toSet())

                    _availableFolderFilters.emit(folders.map {
                        if (it.isArchived) {
                            FolderType.ARCHIVE_FOLDER
                        } else {
                            FolderType.REGULAR_FOLDER
                        }
                    }.toSet())

                    val filteredFolderResults = folders.filter {
                        !allInputs.isTagFilterApplied && (allInputs.appliedFolderFilters.isEmpty() || if (it.isArchived) {
                            FolderType.ARCHIVE_FOLDER
                        } else {
                            FolderType.REGULAR_FOLDER
                        } in _appliedFolderFilters)
                    }
                    _tagsAvailableForFiltering.value = tags.isNotEmpty()

                    val filteredTagsResult =
                        if (allInputs.isTagFilterApplied || (allInputs.appliedFolderFilters.isEmpty() && allInputs.appliedLinkFilters.isEmpty())) {
                            tags
                        } else {
                            emptyList()
                        }
                    Triple(linkTagsPairs, filteredFolderResults, filteredTagsResult)
                }
            }.collectLatest { (linkTagsPairs, folders, tags) ->
                _linkQueryResults.emit(linkTagsPairs)
                _tagQueryResults.emit(tags)
                _folderQueryResults.emit(folders)
            }
        }
    }

    fun addANewLinkToHistory(link: Link, tagIds: List<Long>?) {
        viewModelScope.launch {
            localLinksRepo.addANewLink(
                link = link.copy(
                    linkType = LinkType.HISTORY_LINK,
                    idOfLinkedFolder = null,
                ), linkSaveConfig = LinkSaveConfig(
                    forceAutoDetectTitle = false, forceSaveWithoutRetrievingData = true,

                    ), selectedTagIds = tagIds
            ).collectLatest {
                it.onSuccess {
                    if (it.isRemoteExecutionSuccessful.not()) {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(it.getRemoteOnlyFailureMsg()))
                    }
                }
                it.pushSnackbarOnFailure()
            }
        }
    }

    private val _historyLinkTagsPairsState = MutableStateFlow(
        PaginationState.retrieving<List<LinkTagsPair>>()
    )

    val historyLinkTagsPairsState = _historyLinkTagsPairsState.asStateInWhileSubscribed(
        initialValue = PaginationState.retrieving()
    )

    private val historyLinksPaginator = Paginator(
        onRetrieve = { nextPageStartIndex ->
            nextPageStartIndex to combine(
                snapshotFlow {
                    AppPreferences.selectedSortingTypeType.value
                },
                snapshotFlow {
                    AppPreferences.forceShuffleLinks.value
                },
            ) { selectedSortingType, forceShuffleLinks ->
                forceShuffleLinks to selectedSortingType
            }.flatMapLatest { (forceShuffleLinks, selectedSortingType) ->
                localLinksRepo.getLinks(
                    linkType = LinkType.HISTORY_LINK,
                    selectedSortingType,
                    pageSize = Constants.PAGE_SIZE,
                    startIndex = nextPageStartIndex
                )
                    .flatMapLatest {
                        when (it) {
                            is Result.Failure -> flowOf(Result.Failure(it.message))
                            is Result.Loading -> flowOf(Result.Loading())
                            is Result.Success -> {
                                val allLinks =
                                    if (forceShuffleLinks) it.data.shuffled() else it.data
                                val linkIds = allLinks.map { it.localId }
                                localTagsRepo.getTagsForLinks(linkIds).map { tagsForLinks ->
                                    allLinks.map {
                                        LinkTagsPair(
                                            link = it,
                                            tags = tagsForLinks[it.localId] ?: emptyList()
                                        )
                                    }
                                }.map {
                                    Result.Success(it)
                                }
                            }
                        }
                    }
            }
        },
        onRetrieved = _historyLinkTagsPairsState::onRetrieved,
        onError = _historyLinkTagsPairsState::onError,
        onRetrieving = _historyLinkTagsPairsState::onRetrieving,
        onPagesFinished = _historyLinkTagsPairsState::onPagesFinished,
        coroutineScope = viewModelScope
    )

    fun retrieveNextBatchOfHistoryLinks() {
        viewModelScope.launch {
            historyLinksPaginator.retrieveNextBatch()
        }
    }

    fun updateStartingIndexForHistoryPaginator(newIndex: Long) {
        viewModelScope.launch {
            historyLinksPaginator.updateFirstVisibleItemIndex(newIndex)
        }
    }

    init {
        viewModelScope.launch {
            historyLinksPaginator.retrieveNextBatch()
        }
        /*viewModelScope.launch {
            localLinksRepo.addMultipleLinks(List(500) {
                Link(
                    linkType = LinkType.HISTORY_LINK,
                    title = "$it",
                    url = "",
                    imgURL = "",
                    note = "",
                    idOfLinkedFolder = null
                )
            })
        }*/
    }
}