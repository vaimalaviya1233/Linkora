package com.sakethh.linkora.ui.screens.search

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

class SearchScreenVM(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
) : CollectionsScreenVM(localFoldersRepo, localLinksRepo, loadRootFoldersOnInit = false) {

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

    private val _queryResultLinks = MutableStateFlow(emptyList<Link>())
    val queryResultLinks = _queryResultLinks.asStateFlow()

    init {
        viewModelScope.launch {
            combine(snapshotFlow {
                _searchQuery.value
            }, snapshotFlow {
                AppPreferences.selectedSortingTypeType.value
            }) { query, _ ->
                query
            }.collectLatest { query ->
                linkoraLog(query)
                if (query.isBlank()) {
                    _queryResultLinks.emit(emptyList())
                    return@collectLatest
                }
                localLinksRepo.search(query, AppPreferences.selectedSortingTypeType.value)
                    .collectLatest {
                        it.onSuccess {
                            _queryResultLinks.emit(it.data)
                        }.pushSnackbarOnFailure()
                    }
            }
        }
    }

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