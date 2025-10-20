package com.sakethh.linkora.ui.screens.search

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.linkora.Localization
import com.sakethh.linkora.di.linkoraViewModel
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.asLocalizedString
import com.sakethh.linkora.domain.asMenuBtmSheetType
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.components.CollectionLayoutManager
import com.sakethh.linkora.ui.components.SortingIconButton
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import com.sakethh.linkora.ui.domain.CurrentFABContext
import com.sakethh.linkora.ui.domain.FABContext
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.domain.model.CollectionType
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.DataEmptyScreen
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.pressScaleEffect
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.rememberLocalizedString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(currentFABContext: (CurrentFABContext)-> Unit) {
    LaunchedEffect(Unit) {
        currentFABContext(CurrentFABContext.ROOT)
    }
    val searchScreenVM: SearchScreenVM = linkoraViewModel()
    val platform = LocalPlatform.current
    val historyLinkTagsPairs = searchScreenVM.linkTagsPairs.collectAsStateWithLifecycle()
    val searchQueryLinkResults = searchScreenVM.linkQueryResults.collectAsStateWithLifecycle()
    val searchQueryFolderResults = searchScreenVM.folderQueryResults.collectAsStateWithLifecycle()
    val searchQueryTagResults = searchScreenVM.tagQueryResults.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val localUriHandler = LocalUriHandler.current
    val navController = LocalNavController.current
    val searchBarPadding =
        animateDpAsState(if (searchScreenVM.isSearchActive.value.not()) 15.dp else 0.dp)
    val availableFolderFilters by searchScreenVM.availableFolderFilters.collectAsStateWithLifecycle()
    val availableLinkFilters by searchScreenVM.availableLinkFilters.collectAsStateWithLifecycle()
    Column(modifier = Modifier.fillMaxSize()) {
        ProvideTextStyle(MaterialTheme.typography.titleSmall) {
            SearchBar(
                query = searchScreenVM.searchQuery.value,
                onQueryChange = {
                    searchScreenVM.updateSearchQuery(it)
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                placeholder = {
                    Text(
                        text = Localization.Key.SearchTitlesToFindLinksAndFolders.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.basicMarquee(),
                        maxLines = 1
                    )
                },
                modifier = Modifier.animateContentSize().padding(searchBarPadding.value)
                    .fillMaxWidth().wrapContentHeight(),
                trailingIcon = {
                    Row {
                        if (searchScreenVM.isSearchActive.value) {
                            SortingIconButton()
                            IconButton(modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand).pressScaleEffect(), onClick = {
                                if (searchScreenVM.searchQuery.value == "") {
                                    searchScreenVM.isSearchActive.value = false
                                } else {
                                    searchScreenVM.updateSearchQuery("")
                                }
                            }) {
                                Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                            }
                        }
                    }
                },
                onSearch = {

                },
                active = searchScreenVM.isSearchActive.value,
                onActiveChange = {
                    searchScreenVM.updateSearchActiveState(it)
                }) {
                if (searchScreenVM.searchQuery.value.isBlank()) {
                    DataEmptyScreen(text = Localization.Key.SearchInLinkora.rememberLocalizedString())
                } else {
                    Column {
                        Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                            availableFolderFilters.forEach {
                                FilterChip(
                                    text = it.asLocalizedString(),
                                    isSelected = searchScreenVM.appliedFolderFilters.contains(
                                        it
                                    ),
                                    onClick = {
                                        searchScreenVM.toggleFolderFilter(it)
                                    })
                            }
                            availableLinkFilters.forEach {
                                FilterChip(
                                    text = it.asLocalizedString(),
                                    isSelected = searchScreenVM.appliedLinkFilters.contains(
                                        it
                                    ),
                                    onClick = {
                                        searchScreenVM.toggleLinkFilter(it)
                                    })
                            }
                            if (searchScreenVM.tagsAvailableForFiltering) {
                                FilterChip(
                                    text = "Tags",
                                    isSelected = searchScreenVM.appliedTagFiltering,
                                    onClick = {
                                        searchScreenVM.toggleTagFilter()
                                    })
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                        }
                        CollectionLayoutManager(
                            emptyDataText = Localization.Key.NoSearchResults.rememberLocalizedString(),
                            folders = searchQueryFolderResults.value,
                            linksTagsPairs = searchQueryLinkResults.value,
                            paddingValues = PaddingValues(0.dp),
                            folderMoreIconClick = {
                                coroutineScope.pushUIEvent(
                                    UIEvent.Type.ShowMenuBtmSheet(
                                        menuBtmSheetFor = if (it.isArchived) MenuBtmSheetType.Folder.ArchiveFolder else MenuBtmSheetType.Folder.RegularFolder,
                                        selectedLinkForMenuBtmSheet = null,
                                        selectedFolderForMenuBtmSheet = it
                                    )
                                )
                            },
                            onFolderClick = { folder ->
                                val collectionDetailPaneInfo = CollectionDetailPaneInfo(
                                    currentFolder = folder,
                                    currentTag = null,
                                    collectionType = CollectionType.FOLDER
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    key = Constants.COLLECTION_INFO_SAVED_STATE_HANDLE_KEY,
                                    value = Json.encodeToString(collectionDetailPaneInfo)
                                )
                                navController.navigate(Navigation.Collection.CollectionDetailPane)
                            },
                            linkMoreIconClick = {
                                coroutineScope.pushUIEvent(
                                    UIEvent.Type.ShowMenuBtmSheet(
                                        menuBtmSheetFor = it.link.linkType.asMenuBtmSheetType(),
                                        selectedLinkForMenuBtmSheet = it,
                                        selectedFolderForMenuBtmSheet = null
                                    )
                                )
                            },
                            onLinkClick = {
                                localUriHandler.openUri(it.link.url)
                                searchScreenVM.addANewLinkToHistory(
                                    link = it.link.copy(
                                        linkType = LinkType.HISTORY_LINK, localId = 0
                                    ), tagIds = it.tags.map { it.localId })
                            },
                            isCurrentlyInDetailsView = {
                                false
                            },
                            nestedScrollConnection = null,
                            onAttachedTagClick = {
                                val collectionDetailPaneInfo = CollectionDetailPaneInfo(
                                    currentFolder = null,
                                    currentTag = it,
                                    collectionType = CollectionType.TAG,
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    key = Constants.COLLECTION_INFO_SAVED_STATE_HANDLE_KEY,
                                    value = Json.encodeToString(
                                        collectionDetailPaneInfo
                                    )
                                )
                                navController.navigate(
                                    Navigation.Collection.CollectionDetailPane
                                )
                            },
                            tags = searchQueryTagResults.value,
                            onTagClick = {
                                val collectionDetailPaneInfo = CollectionDetailPaneInfo(
                                    currentFolder = null,
                                    currentTag = it,
                                    collectionType = CollectionType.TAG,
                                )
                                navController.currentBackStackEntry?.savedStateHandle?.set(
                                    key = Constants.COLLECTION_INFO_SAVED_STATE_HANDLE_KEY,
                                    value = Json.encodeToString(
                                        collectionDetailPaneInfo
                                    )
                                )
                                navController.navigate(
                                    Navigation.Collection.CollectionDetailPane
                                )
                            },
                            tagMoreIconClick = {
                                coroutineScope.pushUIEvent(
                                    UIEvent.Type.ShowTagMenuBtmSheet(
                                        selectedTag = it
                                    )
                                )
                            })
                    }
                }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = Localization.rememberLocalizedString(Localization.Key.History),
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium,
                fontSize = 20.sp,
                modifier = Modifier.padding(start = 15.dp)
            )
            Row {
                SortingIconButton()
                Spacer(modifier = Modifier.width(5.dp))
            }
        }
        CollectionLayoutManager(
            emptyDataText = Localization.Key.NoHistoryFound.rememberLocalizedString(),
            folders = emptyList(),
            linksTagsPairs = historyLinkTagsPairs.value,
            paddingValues = PaddingValues(0.dp),
            folderMoreIconClick = {},
            onFolderClick = {},
            linkMoreIconClick = {
                coroutineScope.pushUIEvent(
                    UIEvent.Type.ShowMenuBtmSheet(
                        menuBtmSheetFor = MenuBtmSheetType.Link.HistoryLink,
                        selectedLinkForMenuBtmSheet = it,
                        selectedFolderForMenuBtmSheet = null
                    )
                )
            },
            onLinkClick = {
                localUriHandler.openUri(it.link.url)
                searchScreenVM.addANewLinkToHistory(
                    link = it.link.copy(linkType = LinkType.HISTORY_LINK, localId = 0),
                    tagIds = it.tags.map { it.localId })
            },
            isCurrentlyInDetailsView = {
                false
            },
            nestedScrollConnection = null,
            onAttachedTagClick = {
                val collectionDetailPaneInfo = CollectionDetailPaneInfo(
                    currentFolder = null,
                    currentTag = it,
                    collectionType = CollectionType.TAG,
                )
                navController.currentBackStackEntry?.savedStateHandle?.set(
                    key = Constants.COLLECTION_INFO_SAVED_STATE_HANDLE_KEY,
                    value = Json.encodeToString(
                        collectionDetailPaneInfo
                    )
                )
                navController.navigate(
                    Navigation.Collection.CollectionDetailPane
                )
            },
            tags = emptyList(),
            tagMoreIconClick = {},
            onTagClick = {})
    }
}

@Composable
fun FilterChip(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Row(
        modifier = Modifier.animateContentSize()
    ) {
        Spacer(modifier = Modifier.width(10.dp))
        androidx.compose.material3.FilterChip(modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand), selected = isSelected, onClick = onClick, label = {
            Text(
                text = text, style = MaterialTheme.typography.titleSmall
            )
        }, leadingIcon = {
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check, contentDescription = null
                )
            }
        })
    }
}