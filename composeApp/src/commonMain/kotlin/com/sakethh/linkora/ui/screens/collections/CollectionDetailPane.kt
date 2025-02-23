package com.sakethh.linkora.ui.screens.collections

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.primaryContentColor
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.PlatformSpecificBackHandler
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.isNotNull
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.asHistoryLinkWithoutId
import com.sakethh.linkora.domain.asLocalizedString
import com.sakethh.linkora.domain.asMenuBtmSheetType
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.CollectionLayoutManager
import com.sakethh.linkora.ui.components.SortingIconButton
import com.sakethh.linkora.ui.components.folder.FolderComponent
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.domain.model.FolderComponentParam
import com.sakethh.linkora.ui.screens.search.FilterChip
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.platform
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailPane(
    collectionsScreenVM: CollectionsScreenVM = viewModel(factory = genericViewModelFactory {
        CollectionsScreenVM(
            localFoldersRepo = DependencyContainer.localFoldersRepo.value,
            localLinksRepo = DependencyContainer.localLinksRepo.value,
            loadRootFoldersOnInit = false,
            collectionDetailPaneInfo = CollectionsScreenVM.collectionDetailPaneInfo.value
        )
    }),
) {
    val links = collectionsScreenVM.links.collectAsStateWithLifecycle()
    val childFolders = collectionsScreenVM.childFolders.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val rootArchiveFolders = collectionsScreenVM.rootArchiveFolders.collectAsStateWithLifecycle()
    val currentlyInFolder = CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder
    val platform = platform()
    val navController = LocalNavController.current
    val localUriHandler = LocalUriHandler.current
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        Column {
            TopAppBar(actions = {
                SortingIconButton()
            }, navigationIcon = {
                IconButton(onClick = {
                    if (CollectionsScreenVM.collectionDetailPaneInfo.value.searchNavigated.navigatedFromSearchScreen && currentlyInFolder?.localId == CollectionsScreenVM.collectionDetailPaneInfo.value.searchNavigated.navigatedWithFolderId) {
                        navController.navigateUp()
                        return@IconButton
                    }
                    if (currentlyInFolder?.parentFolderId.isNotNull()) {
                        currentlyInFolder?.parentFolderId as Long
                        collectionsScreenVM.updateCollectionDetailPaneInfoAndCollectData(
                            CollectionDetailPaneInfo(
                                currentFolder = collectionsScreenVM.getFolder(currentlyInFolder.parentFolderId),
                                isAnyCollectionSelected = true
                            )
                        )
                    } else {
                        if (platform is Platform.Android.Mobile) {
                            navController.navigateUp()
                            return@IconButton
                        }
                        collectionsScreenVM.updateCollectionDetailPaneInfoAndCollectData(
                            CollectionDetailPaneInfo(
                                currentFolder = null, isAnyCollectionSelected = false
                            )
                        )
                    }
                }) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null
                    )
                }
            }, title = {
                Text(
                    text = currentlyInFolder?.name ?: "",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp
                )
            })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.25f))
        }
    }) { paddingValues ->

        if (CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.ARCHIVE_ID) {
            Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
                TabRow(selectedTabIndex = pagerState.currentPage) {
                    listOf(
                        Localization.Key.Links.rememberLocalizedString(),
                        Localization.Key.Folders.rememberLocalizedString()
                    ).forEachIndexed { index, screenName ->
                        Tab(selected = pagerState.currentPage == index, onClick = {
                            coroutineScope.launch {
                                pagerState.animateScrollToPage(index)
                            }.start()
                        }) {
                            Text(
                                text = screenName,
                                style = MaterialTheme.typography.titleLarge,
                                fontSize = 18.sp,
                                modifier = Modifier.padding(15.dp),
                                color = if (pagerState.currentPage == index) primaryContentColor else MaterialTheme.colorScheme.onSurface.copy(
                                    0.70f
                                )
                            )
                        }
                    }
                }
                HorizontalPager(state = pagerState) { pageIndex ->
                    when (pageIndex) {
                        0 -> {
                            CollectionLayoutManager(
                                folders = emptyList(),
                                links = links.value,
                                paddingValues = PaddingValues(0.dp),
                                linkMoreIconClick = {
                                    coroutineScope.pushUIEvent(
                                        UIEvent.Type.ShowMenuBtmSheetUI(
                                            menuBtmSheetFor = it.linkType.asMenuBtmSheetType(),
                                            selectedLinkForMenuBtmSheet = it,
                                            selectedFolderForMenuBtmSheet = null
                                        )
                                    )
                                },
                                folderMoreIconClick = {},
                                onFolderClick = {},
                                onLinkClick = {
                                    collectionsScreenVM.addANewLink(
                                        link = it.asHistoryLinkWithoutId(),
                                        linkSaveConfig = LinkSaveConfig(
                                            forceAutoDetectTitle = false,
                                            forceSaveWithoutRetrievingData = true
                                        ),
                                        onCompletion = {},
                                        pushSnackbarOnSuccess = false
                                    )
                                    localUriHandler.openUri(it.url)
                                },
                                isCurrentlyInDetailsView = {
                                    CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == it.localId
                                })
                        }

                        1 -> {
                            LazyColumn(Modifier.fillMaxSize()) {
                                items(rootArchiveFolders.value) { rootArchiveFolder ->
                                    FolderComponent(
                                        FolderComponentParam(
                                            folder = rootArchiveFolder,
                                            onClick = { ->
                                                if (CollectionsScreenVM.selectedFoldersViaLongClick.contains(
                                                        rootArchiveFolder
                                                    )
                                                ) {
                                                    return@FolderComponentParam
                                                }
                                                collectionsScreenVM.updateCollectionDetailPaneInfoAndCollectData(
                                                    CollectionDetailPaneInfo(
                                                        currentFolder = rootArchiveFolder,
                                                        isAnyCollectionSelected = true
                                                    )
                                                )
                                            },
                                            onLongClick = { ->
                                                if (CollectionsScreenVM.isSelectionEnabled.value.not()) {
                                                    CollectionsScreenVM.isSelectionEnabled.value =
                                                        true
                                                    CollectionsScreenVM.selectedFoldersViaLongClick.add(
                                                        rootArchiveFolder
                                                    )
                                                }
                                            },
                                            onMoreIconClick = { ->
                                                coroutineScope.pushUIEvent(
                                                    UIEvent.Type.ShowMenuBtmSheetUI(
                                                        menuBtmSheetFor = MenuBtmSheetType.Folder.RegularFolder,
                                                        selectedLinkForMenuBtmSheet = null,
                                                        selectedFolderForMenuBtmSheet = rootArchiveFolder
                                                    )
                                                )
                                            },
                                            isCurrentlyInDetailsView = remember(CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId) {
                                                mutableStateOf(CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == rootArchiveFolder.localId)
                                            },
                                            showMoreIcon = rememberSaveable {
                                                mutableStateOf(true)
                                            },
                                            isSelectedForSelection = rememberSaveable(
                                                CollectionsScreenVM.isSelectionEnabled.value,
                                                CollectionsScreenVM.selectedFoldersViaLongClick
                                            ) {
                                                mutableStateOf(
                                                    CollectionsScreenVM.isSelectionEnabled.value && CollectionsScreenVM.selectedFoldersViaLongClick.contains(
                                                        rootArchiveFolder
                                                    )
                                                )
                                            },
                                            showCheckBox = CollectionsScreenVM.isSelectionEnabled,
                                            onCheckBoxChanged = { bool ->
                                                if (bool) {
                                                    CollectionsScreenVM.selectedFoldersViaLongClick.add(
                                                        rootArchiveFolder
                                                    )
                                                } else {
                                                    CollectionsScreenVM.selectedFoldersViaLongClick.remove(
                                                        rootArchiveFolder
                                                    )
                                                }
                                            })
                                    )
                                }
                            }
                        }
                    }
                }
            }
            return@Scaffold
        }

        Column(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            if (CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.ALL_LINKS_ID) {
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState())) {
                    collectionsScreenVM.availableFiltersForAllLinks.forEach {
                        FilterChip(
                            text = it.asLocalizedString(),
                            isSelected = collectionsScreenVM.appliedFiltersForAllLinks.contains(
                                it
                            ),
                            onClick = {
                                collectionsScreenVM.toggleAllLinksFilter(it)
                            })
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                }
            }
            CollectionLayoutManager(
                folders = childFolders.value,
                links = links.value,
                paddingValues = PaddingValues(0.dp),
                linkMoreIconClick = {
                    coroutineScope.pushUIEvent(
                        UIEvent.Type.ShowMenuBtmSheetUI(
                            menuBtmSheetFor = it.linkType.asMenuBtmSheetType(),
                            selectedLinkForMenuBtmSheet = it,
                            selectedFolderForMenuBtmSheet = null
                        )
                    )
                },
                folderMoreIconClick = {
                    coroutineScope.pushUIEvent(
                        UIEvent.Type.ShowMenuBtmSheetUI(
                            menuBtmSheetFor = MenuBtmSheetType.Folder.RegularFolder,
                            selectedLinkForMenuBtmSheet = null,
                            selectedFolderForMenuBtmSheet = it
                        )
                    )
                },
                onFolderClick = {
                    collectionsScreenVM.updateCollectionDetailPaneInfoAndCollectData(
                        CollectionDetailPaneInfo(
                            currentFolder = it, isAnyCollectionSelected = true
                        )
                    )
                },
                onLinkClick = {
                    collectionsScreenVM.addANewLink(
                        link = it.asHistoryLinkWithoutId(), linkSaveConfig = LinkSaveConfig(
                            forceAutoDetectTitle = false, forceSaveWithoutRetrievingData = true
                        ), onCompletion = {}, pushSnackbarOnSuccess = false
                    )
                    localUriHandler.openUri(it.url)
                },
                isCurrentlyInDetailsView = {
                    CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == it.localId
                })
        }
    }
    PlatformSpecificBackHandler {
        if (CollectionsScreenVM.collectionDetailPaneInfo.value.searchNavigated.navigatedFromSearchScreen && currentlyInFolder?.localId == CollectionsScreenVM.collectionDetailPaneInfo.value.searchNavigated.navigatedWithFolderId) {
            navController.navigateUp()
            return@PlatformSpecificBackHandler
        }
        if (currentlyInFolder?.parentFolderId.isNotNull()) {
            currentlyInFolder?.parentFolderId as Long
            collectionsScreenVM.updateCollectionDetailPaneInfoAndCollectData(
                CollectionDetailPaneInfo(
                    currentFolder = collectionsScreenVM.getFolder(currentlyInFolder.parentFolderId),
                    isAnyCollectionSelected = true
                )
            )
        } else {
            if (platform is Platform.Android.Mobile) {
                navController.navigateUp()
                return@PlatformSpecificBackHandler
            }
            collectionsScreenVM.updateCollectionDetailPaneInfoAndCollectData(
                CollectionDetailPaneInfo(
                    currentFolder = null, isAnyCollectionSelected = false
                )
            )
        }
    }
}