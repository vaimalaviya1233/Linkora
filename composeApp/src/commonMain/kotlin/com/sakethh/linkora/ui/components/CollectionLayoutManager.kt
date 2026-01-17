package com.sakethh.linkora.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.lazy.staggeredgrid.items
import androidx.compose.foundation.lazy.staggeredgrid.rememberLazyStaggeredGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material3.ContainedLoadingIndicator
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.linkora.Localization
import com.sakethh.linkora.di.LinkoraSDK
import com.sakethh.linkora.domain.model.FlatChildFolderData
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.PageKey
import com.sakethh.linkora.ui.components.folder.FolderComponent
import com.sakethh.linkora.ui.components.link.GridViewLinkComponent
import com.sakethh.linkora.ui.components.link.ListViewLinkComponent
import com.sakethh.linkora.ui.domain.Layout
import com.sakethh.linkora.ui.domain.PaginationState
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.domain.model.FolderComponentParam
import com.sakethh.linkora.ui.domain.model.LinkComponentParam
import com.sakethh.linkora.ui.domain.model.LinkTagsPair
import com.sakethh.linkora.ui.screens.DataEmptyScreen
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.getLocalizedString
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged

@OptIn(ExperimentalMaterial3ExpressiveApi::class)
@Composable
fun CollectionLayoutManager(
    screenType: ScreenType,
    flatChildFolderDataState: PaginationState<Map<PageKey, List<FlatChildFolderData>>>,
    linksTagsPairsState: StateFlow<PaginationState<Map<PageKey, List<LinkTagsPair>>>>,
    tags: List<Tag>,
    paddingValues: PaddingValues,
    folderMoreIconClick: (folder: Folder) -> Unit,
    tagMoreIconClick: (tag: Tag) -> Unit,
    onFolderClick: (folder: Folder) -> Unit,
    linkMoreIconClick: (linkTagsPair: LinkTagsPair) -> Unit,
    onLinkClick: (linkTagsPair: LinkTagsPair) -> Unit,
    onTagClick: (tag: Tag) -> Unit,
    onAttachedTagClick: (tag: Tag) -> Unit,
    isCurrentlyInDetailsView: (folder: Folder) -> Boolean,
    emptyDataText: String = "",
    nestedScrollConnection: NestedScrollConnection?,
    onRetrieveNextPage: () -> Unit,
    onFirstVisibleItemIndexChange: (PageKey) -> Unit,
) {
    val linksTagsPairsState by linksTagsPairsState.collectAsStateWithLifecycle()
    val isFoldersStateEmpty =
        flatChildFolderDataState.data.isEmpty() || flatChildFolderDataState.data.values.first()
            .isEmpty()
    val isLinkTagsPairStateEmpty =
        linksTagsPairsState.data.isEmpty() || linksTagsPairsState.data.values.first().isEmpty()

    val emptyDataText =
        rememberSaveable(flatChildFolderDataState, linksTagsPairsState, emptyDataText) {
            emptyDataText.ifBlank {
                when {
                    isFoldersStateEmpty && isLinkTagsPairStateEmpty -> Localization.Key.NoFoldersOrLinksFound.getLocalizedString()
                    else -> Localization.Key.FoldersExistsButNotLinks.getLocalizedString()
                }
            }
        }
    val isDataEmpty =
        (isFoldersStateEmpty && isLinkTagsPairStateEmpty) || (!isFoldersStateEmpty && isLinkTagsPairStateEmpty)

    val listLayoutState =
        rememberLazyListState()

    val gridLayoutState = rememberLazyGridState()
    val staggeredGridLayoutState = rememberLazyStaggeredGridState()

    val linkComponentParam: (linkTagsPair: LinkTagsPair) -> LinkComponentParam = { linkTagsPair ->
        LinkComponentParam(
            link = linkTagsPair.link,
            isSelectionModeEnabled = CollectionsScreenVM.isSelectionEnabled,
            onMoreIconClick = {
                linkMoreIconClick(linkTagsPair)
            },
            onLinkClick = {
                if (!CollectionsScreenVM.isSelectionEnabled.value) {
                    onLinkClick(linkTagsPair)
                } else {
                    if (CollectionsScreenVM.selectedLinkTagPairsViaLongClick.contains(linkTagsPair)) {
                        CollectionsScreenVM.selectedLinkTagPairsViaLongClick.remove(linkTagsPair)
                    } else {
                        CollectionsScreenVM.selectedLinkTagPairsViaLongClick.add(linkTagsPair)
                    }
                }
            },
            onForceOpenInExternalBrowserClicked = {

            },
            isItemSelected = mutableStateOf(
                CollectionsScreenVM.selectedLinkTagPairsViaLongClick.contains(
                    linkTagsPair
                )
            ),
            onLongClick = {
                if (!CollectionsScreenVM.isSelectionEnabled.value) {
                    CollectionsScreenVM.isSelectionEnabled.value = true
                    CollectionsScreenVM.selectedLinkTagPairsViaLongClick.add(linkTagsPair)
                }
            },
            tags = linkTagsPair.tags,
            onTagClick = {
                onAttachedTagClick(it)
            })
    }
    val bottomSpacing = remember {
        mutableStateOf(250.dp)
    }
    val folderComponentParam: (folder: Folder) -> FolderComponentParam = {
        FolderComponentParam(
            name = it.name,
            note = it.note,
            onClick = { ->
                if (CollectionsScreenVM.selectedFoldersViaLongClick.contains(it)) {
                    return@FolderComponentParam
                }
                onFolderClick(it)
            },
            onLongClick = { ->
                if (CollectionsScreenVM.isSelectionEnabled.value.not()) {
                    CollectionsScreenVM.isSelectionEnabled.value = true
                    CollectionsScreenVM.selectedFoldersViaLongClick.add(it)
                }
            },
            onMoreIconClick = { ->
                folderMoreIconClick(it)
            },
            isCurrentlyInDetailsView = mutableStateOf(isCurrentlyInDetailsView(it)),
            showMoreIcon = mutableStateOf(true),
            isSelectedForSelection = mutableStateOf(
                CollectionsScreenVM.isSelectionEnabled.value && CollectionsScreenVM.selectedFoldersViaLongClick.contains(
                    it
                )
            ),
            showCheckBox = CollectionsScreenVM.isSelectionEnabled,
            onCheckBoxChanged = { bool ->
                if (bool) {
                    CollectionsScreenVM.selectedFoldersViaLongClick.add(it)
                } else {
                    CollectionsScreenVM.selectedFoldersViaLongClick.remove(
                        it
                    )
                }
            })
    }
    val tagComponentParam: (tag: Tag) -> FolderComponentParam = {
        FolderComponentParam(
            name = it.name,
            note = "",
            onClick = { ->
                onTagClick(it)
            },
            onLongClick = { },
            onMoreIconClick = { ->
                tagMoreIconClick(it)
            },
            isCurrentlyInDetailsView = mutableStateOf(true),
            showMoreIcon = mutableStateOf(true),
            isSelectedForSelection = mutableStateOf(false),
            showCheckBox = mutableStateOf(false),
            onCheckBoxChanged = {},
            leadingIcon = Icons.Default.Tag
        )
    }

    when (AppPreferences.selectedLinkLayout.value) {
        Layout.TITLE_ONLY_LIST_VIEW.name, Layout.REGULAR_LIST_VIEW.name -> {

            LazyColumn(
                modifier = Modifier.then(
                    if (nestedScrollConnection != null) Modifier.nestedScroll(
                        nestedScrollConnection
                    ) else Modifier
                ).padding(paddingValues).fillMaxSize(),
                state = listLayoutState
            ) {

                if (screenType == ScreenType.TAGS_FOLDERS_LINKS) {
                    items(tags) {
                        FolderComponent(folderComponentParam = tagComponentParam(it))
                    }
                }

                if (screenType == ScreenType.FOLDERS_AND_LINKS || screenType == ScreenType.TAGS_FOLDERS_LINKS) {
                    flatChildFolderDataState.data.forEach { (pageIndex, flatChildFolderData) ->
                        items(flatChildFolderData, key = {
                            "CollectionLayoutManager-LazyColumn-flatChildFolderDataState-P$pageIndex-ID${it.linkLocalId}${it.folderLocalId}"
                        }) {
                            if (it.itemType == Constants.FOLDER) {
                                FolderComponent(folderComponentParam = folderComponentParam(it.asFolder))
                            } else {
                                ListViewLinkComponent(
                                    linkComponentParam = linkComponentParam(it.asLinkTagsPair),
                                    titleOnlyView = AppPreferences.selectedLinkLayout.value == Layout.TITLE_ONLY_LIST_VIEW.name,
                                    onShare = {
                                        LinkoraSDK.getInstance().nativeUtils.onShare(it)
                                    })
                            }
                        }
                    }
                }

                if (screenType == ScreenType.LINKS_ONLY) {
                    linksTagsPairsState.data.forEach { (pageIndex, linkTagsPair) ->
                        items(items = linkTagsPair, key = {
                            "LazyColumn-linksTagsPairs-P$pageIndex-ID" + it.link.localId
                        }) {
                            ListViewLinkComponent(
                                linkComponentParam = linkComponentParam(it),
                                titleOnlyView = AppPreferences.selectedLinkLayout.value == Layout.TITLE_ONLY_LIST_VIEW.name,
                                onShare = {
                                    LinkoraSDK.getInstance().nativeUtils.onShare(it)
                                })
                        }
                    }
                }

                item {
                    if (!linksTagsPairsState.isRetrieving && isDataEmpty) {
                        DataEmptyScreen(text = emptyDataText)
                    }
                }
                item {
                    if (!linksTagsPairsState.pagesCompleted) {
                        Box(
                            modifier = Modifier.fillMaxWidth()
                                .padding(15.dp).padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ContainedLoadingIndicator()
                        }
                    }
                }
                item {
                    if (!linksTagsPairsState.isRetrieving) {
                        Spacer(Modifier.height(bottomSpacing.value))
                    }
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow {
                    listLayoutState.firstVisibleItemIndex
                }.debounce(500).distinctUntilChanged().collectLatest {
                    onFirstVisibleItemIndexChange(it.toLong())
                }
            }

            LaunchedEffect(listLayoutState.canScrollForward) {
                if (!listLayoutState.canScrollForward) {
                    onRetrieveNextPage()
                }
            }
        }

        Layout.GRID_VIEW.name -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(150.dp),
                modifier = Modifier.padding(paddingValues).fillMaxSize().then(
                    if (nestedScrollConnection != null) Modifier.nestedScroll(
                        nestedScrollConnection
                    ) else Modifier
                ),
                state = gridLayoutState
            ) {

                if (screenType == ScreenType.TAGS_FOLDERS_LINKS) {
                    items(items = tags, span = {
                        GridItemSpan(this.maxLineSpan)
                    }) {
                        FolderComponent(folderComponentParam = tagComponentParam(it))
                    }
                }

                if (screenType == ScreenType.FOLDERS_AND_LINKS || screenType == ScreenType.TAGS_FOLDERS_LINKS) {
                    flatChildFolderDataState.data.forEach { (pageIndex, folders) ->
                        items(items = folders, span = {
                            GridItemSpan(this.maxLineSpan)
                        }, key = {
                            "CollectionLayoutManager-LazyVerticalGrid-flatChildFolderDataState-P$pageIndex-ID${it.linkLocalId}${it.folderLocalId}"
                        }) {
                            if (it.itemType == Constants.FOLDER) {
                                FolderComponent(folderComponentParam = folderComponentParam(it.asFolder))
                            } else {
                                GridViewLinkComponent(
                                    linkComponentParam = linkComponentParam(it.asLinkTagsPair),
                                    forStaggeredView = AppPreferences.selectedLinkLayout.value == Layout.STAGGERED_VIEW.name
                                )
                            }
                        }
                    }
                }

                if (isFoldersStateEmpty) {
                    item(span = {
                        GridItemSpan(this.maxLineSpan)
                    }) {
                        Spacer(Modifier.height(10.dp))
                    }
                }

                if (screenType == ScreenType.LINKS_ONLY) {
                    linksTagsPairsState.data.forEach { (pageIndex, linkTagsPairs) ->
                        items(linkTagsPairs, key = {
                            "LazyVerticalGrid-linksTagsPairs-P$pageIndex-ID" + it.link.localId
                        }) {
                            GridViewLinkComponent(
                                linkComponentParam = linkComponentParam(it),
                                forStaggeredView = AppPreferences.selectedLinkLayout.value == Layout.STAGGERED_VIEW.name
                            )
                        }
                    }
                }

                if (!linksTagsPairsState.pagesCompleted) {
                    item(span = {
                        GridItemSpan(this.maxLineSpan)
                    }) {
                        Box(
                            modifier = Modifier.fillMaxWidth().padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ContainedLoadingIndicator()
                        }
                    }
                }

                if (linksTagsPairsState.pagesCompleted) {
                    item(span = {
                        GridItemSpan(this.maxLineSpan)
                    }) {
                        Spacer(Modifier.height(bottomSpacing.value))
                    }
                }
            }
            LaunchedEffect(Unit) {
                snapshotFlow {
                    gridLayoutState.firstVisibleItemIndex
                }.debounce(500).distinctUntilChanged().collectLatest {
                    onFirstVisibleItemIndexChange(it.toLong())
                }
            }

            LaunchedEffect(gridLayoutState.canScrollForward) {
                if (!gridLayoutState.canScrollForward && !linksTagsPairsState.pagesCompleted && !linksTagsPairsState.isRetrieving) {
                    onRetrieveNextPage()
                }
            }

        }

        Layout.STAGGERED_VIEW.name -> {
            LazyVerticalStaggeredGrid(
                columns = StaggeredGridCells.Adaptive(150.dp),
                modifier = Modifier.padding(paddingValues).fillMaxSize().then(
                    if (nestedScrollConnection != null) Modifier.nestedScroll(nestedScrollConnection) else Modifier
                ),
                state = staggeredGridLayoutState
            ) {
                if (screenType == ScreenType.TAGS_FOLDERS_LINKS) {
                    items(items = tags, span = { StaggeredGridItemSpan.FullLine }) {
                        FolderComponent(folderComponentParam = tagComponentParam(it))
                    }
                }
                if (tags.isNotEmpty()) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(10.dp))
                    }
                }

                if (screenType == ScreenType.FOLDERS_AND_LINKS || screenType == ScreenType.TAGS_FOLDERS_LINKS) {
                    flatChildFolderDataState.data.forEach { (pageIndex, folders) ->
                        items(items = folders, span = { StaggeredGridItemSpan.FullLine }, key = {
                            "CollectionLayoutManager-LazyVerticalStaggeredGrid-flatChildFolderDataState-P$pageIndex-ID${it.linkLocalId}${it.folderLocalId}"
                        }) {
                            if (it.itemType == Constants.FOLDER) {
                                FolderComponent(folderComponentParam = folderComponentParam(it.asFolder))
                            } else {
                                GridViewLinkComponent(
                                    linkComponentParam = linkComponentParam(it.asLinkTagsPair),
                                    forStaggeredView = AppPreferences.selectedLinkLayout.value == Layout.STAGGERED_VIEW.name
                                )
                            }
                        }
                    }
                }

                if (isFoldersStateEmpty) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(10.dp))
                    }
                }
                if (screenType == ScreenType.LINKS_ONLY) {
                    linksTagsPairsState.data.forEach { (pageIndex, linkTagsPairs) ->
                        items(linkTagsPairs, key = {
                            "LazyVerticalStaggeredGrid-linksTagsPairs-P$pageIndex-ID" + it.link.localId
                        }) {
                            GridViewLinkComponent(
                                linkComponentParam = linkComponentParam(it),
                                forStaggeredView = AppPreferences.selectedLinkLayout.value == Layout.STAGGERED_VIEW.name
                            )
                        }
                    }
                }

                if (!linksTagsPairsState.pagesCompleted) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Box(
                            modifier = Modifier.then(
                                if (!isDataEmpty) Modifier.fillMaxWidth()
                                    .padding(15.dp) else Modifier.fillMaxHeight()
                            ).padding(15.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            ContainedLoadingIndicator()
                        }
                    }
                }
                if (linksTagsPairsState.pagesCompleted) {
                    item(span = StaggeredGridItemSpan.FullLine) {
                        Spacer(Modifier.height(bottomSpacing.value))
                    }
                }
            }

            LaunchedEffect(Unit) {
                snapshotFlow {
                    staggeredGridLayoutState.firstVisibleItemIndex
                }.debounce(500).distinctUntilChanged().collectLatest {
                    onFirstVisibleItemIndexChange(it.toLong())
                }
            }

            LaunchedEffect(staggeredGridLayoutState.canScrollForward) {
                if (!staggeredGridLayoutState.canScrollForward && !linksTagsPairsState.pagesCompleted && !linksTagsPairsState.isRetrieving) {
                    onRetrieveNextPage()
                }
            }
        }
    }
}