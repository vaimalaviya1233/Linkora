package com.sakethh.linkora.screens.search

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SearchBar
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.sakethh.linkora.btmSheet.OptionsBtmSheetType
import com.sakethh.linkora.btmSheet.OptionsBtmSheetUI
import com.sakethh.linkora.btmSheet.OptionsBtmSheetUIParam
import com.sakethh.linkora.btmSheet.OptionsBtmSheetVM
import com.sakethh.linkora.btmSheet.SortingBottomSheetUI
import com.sakethh.linkora.customComposables.DataDialogBoxType
import com.sakethh.linkora.customComposables.DeleteDialogBox
import com.sakethh.linkora.customComposables.DeleteDialogBoxParam
import com.sakethh.linkora.customComposables.LinkUIComponent
import com.sakethh.linkora.customComposables.LinkUIComponentParam
import com.sakethh.linkora.customComposables.RenameDialogBox
import com.sakethh.linkora.customComposables.RenameDialogBoxParam
import com.sakethh.linkora.localDB.dto.RecentlyVisited
import com.sakethh.linkora.navigation.NavigationRoutes
import com.sakethh.linkora.screens.DataEmptyScreen
import com.sakethh.linkora.screens.home.HomeScreenVM
import com.sakethh.linkora.screens.search.SearchScreenVM.Companion.selectedFolderID
import com.sakethh.linkora.screens.settings.SettingsScreenVM
import com.sakethh.linkora.ui.theme.LinkoraTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun SearchScreen(navController: NavController) {
    val searchScreenVM: SearchScreenVM = viewModel()
    val recentlyVisitedLinksData = searchScreenVM.historyLinksData.collectAsState().value
    val impLinksData = searchScreenVM.impLinksQueriedData.collectAsState().value
    val linksTableData = searchScreenVM.linksTableData.collectAsState().value
    val archiveLinksTableData = searchScreenVM.archiveLinksQueriedData.collectAsState().value
    val context = LocalContext.current
    val uriHandler = LocalUriHandler.current
    val shouldSortingBottomSheetAppear = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldOptionsBtmModalSheetBeVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val selectedWebURL = rememberSaveable {
        mutableStateOf("")
    }
    val selectedURLNote = rememberSaveable {
        mutableStateOf("")
    }
    val interactionSource = remember {
        MutableInteractionSource()
    }
    val coroutineScope = rememberCoroutineScope()
    val optionsBtmSheetVM = viewModel<OptionsBtmSheetVM>()
    val query = rememberSaveable {
        mutableStateOf("")
    }
    val sortingBtmSheetState = rememberModalBottomSheetState()
    val optionsBtmSheetState = rememberModalBottomSheetState()
    val shouldRenameDialogBoxAppear = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldDeleteDialogBoxAppear = rememberSaveable {
        mutableStateOf(false)
    }
    val selectedLinkTitle = rememberSaveable {
        mutableStateOf("")
    }
    if (!SearchScreenVM.isSearchEnabled.value) {
        query.value = ""
    }
    LinkoraTheme {
        Column {
            SearchBar(interactionSource = interactionSource,
                trailingIcon = {
                    if (SearchScreenVM.isSearchEnabled.value) {
                        IconButton(onClick = {
                            if (query.value == "") {
                                SearchScreenVM.focusRequester.freeFocus()
                                SearchScreenVM.isSearchEnabled.value = false
                            } else {
                                query.value = ""
                            }
                        }) {
                            Icon(imageVector = Icons.Default.Clear, contentDescription = null)
                        }
                    }
                },
                modifier = Modifier
                    .animateContentSize()
                    .padding(
                        top = if (!SearchScreenVM.isSearchEnabled.value) 10.dp else 0.dp,
                        start = if (!SearchScreenVM.isSearchEnabled.value) 10.dp else 0.dp,
                        end = if (!SearchScreenVM.isSearchEnabled.value) 10.dp else 0.dp
                    )
                    .fillMaxWidth()
                    .focusRequester(SearchScreenVM.focusRequester),
                query = query.value,
                onQueryChange = {
                    query.value = it
                    searchScreenVM.retrieveSearchQueryData(query = it)
                },
                onSearch = {
                    query.value = it
                    searchScreenVM.retrieveSearchQueryData(query = it)
                },
                active = SearchScreenVM.isSearchEnabled.value,
                onActiveChange = {
                    SearchScreenVM.isSearchEnabled.value = !SearchScreenVM.isSearchEnabled.value
                },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                placeholder = {
                    Text(
                        text = "Search titles to find links",
                        style = MaterialTheme.typography.titleSmall
                    )
                },
                content = {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        when {
                            query.value.isEmpty() -> {
                                item {
                                    DataEmptyScreen(text = "Search Linkora: Retrieve all the links you saved.")
                                }
                            }

                            query.value.isNotEmpty() && (linksTableData.isEmpty() && impLinksData.isEmpty() && archiveLinksTableData.isEmpty()) -> {
                                item {
                                    DataEmptyScreen(text = "No Matching Links Found. Try a Different Search.")
                                }
                            }

                            else -> {
                                items(impLinksData) {
                                    LinkUIComponent(
                                        LinkUIComponentParam(title = it.title,
                                            webBaseURL = it.webURL,
                                            imgURL = it.imgURL,
                                            onMoreIconCLick = {
                                                selectedLinkTitle.value = it.title
                                                SearchScreenVM.selectedLinkType =
                                                    SearchScreenVM.SelectedLinkType.IMP_LINKS
                                                HomeScreenVM.tempImpLinkData.webURL = it.webURL
                                                HomeScreenVM.tempImpLinkData.baseURL = it.baseURL
                                                HomeScreenVM.tempImpLinkData.imgURL = it.imgURL
                                                HomeScreenVM.tempImpLinkData.title = it.title
                                                HomeScreenVM.tempImpLinkData.infoForSaving =
                                                    it.infoForSaving
                                                selectedURLNote.value = it.infoForSaving
                                                selectedWebURL.value = it.webURL
                                                shouldOptionsBtmModalSheetBeVisible.value = true
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.awaitAll(async {
                                                        optionsBtmSheetVM.updateArchiveLinkCardData(
                                                            url = it.webURL
                                                        )
                                                    }, async {
                                                        optionsBtmSheetVM.updateImportantCardData(
                                                            url = it.webURL
                                                        )
                                                    })
                                                }
                                            },
                                            onLinkClick = {
                                                coroutineScope.launch {
                                                    com.sakethh.linkora.customWebTab.openInWeb(
                                                        recentlyVisitedData = RecentlyVisited(
                                                            title = it.title,
                                                            webURL = it.webURL,
                                                            baseURL = it.baseURL,
                                                            imgURL = it.imgURL,
                                                            infoForSaving = it.infoForSaving
                                                        ),
                                                        context = context,
                                                        uriHandler = uriHandler,
                                                        forceOpenInExternalBrowser = false
                                                    )
                                                }
                                            },
                                            webURL = it.webURL,
                                            onForceOpenInExternalBrowserClicked = {
                                                searchScreenVM.onLinkClick(
                                                    RecentlyVisited(
                                                        title = it.title,
                                                        webURL = it.webURL,
                                                        baseURL = it.baseURL,
                                                        imgURL = it.imgURL,
                                                        infoForSaving = it.infoForSaving
                                                    ),
                                                    context = context,
                                                    uriHandler = uriHandler,
                                                    onTaskCompleted = {},
                                                    forceOpenInExternalBrowser = true
                                                )
                                            })
                                    )
                                }
                                items(linksTableData) {
                                    LinkUIComponent(
                                        LinkUIComponentParam(title = it.title,
                                            webBaseURL = it.webURL,
                                            imgURL = it.imgURL,
                                            onMoreIconCLick = {
                                                selectedLinkTitle.value = it.title
                                                when {
                                                    it.isLinkedWithArchivedFolder -> {
                                                        SearchScreenVM.selectedLinkType =
                                                            SearchScreenVM.SelectedLinkType.ARCHIVE_FOLDER_BASED_LINKS
                                                        SearchScreenVM.selectedFolderID =
                                                            it.keyOfArchiveLinkedFolderV10
                                                    }

                                                    it.isLinkedWithFolders -> {
                                                        SearchScreenVM.selectedLinkType =
                                                            SearchScreenVM.SelectedLinkType.FOLDER_BASED_LINKS
                                                        SearchScreenVM.selectedFolderID =
                                                            it.keyOfLinkedFolderV10
                                                    }

                                                    it.isLinkedWithSavedLinks -> {
                                                        SearchScreenVM.selectedLinkType =
                                                            SearchScreenVM.SelectedLinkType.SAVED_LINKS
                                                    }
                                                }
                                                HomeScreenVM.tempImpLinkData.webURL = it.webURL
                                                HomeScreenVM.tempImpLinkData.baseURL = it.baseURL
                                                HomeScreenVM.tempImpLinkData.imgURL = it.imgURL
                                                HomeScreenVM.tempImpLinkData.title = it.title
                                                HomeScreenVM.tempImpLinkData.infoForSaving =
                                                    it.infoForSaving
                                                selectedURLNote.value = it.infoForSaving
                                                selectedWebURL.value = it.webURL
                                                shouldOptionsBtmModalSheetBeVisible.value = true
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.awaitAll(async {
                                                        optionsBtmSheetVM.updateArchiveLinkCardData(
                                                            url = it.webURL
                                                        )
                                                    }, async {
                                                        optionsBtmSheetVM.updateImportantCardData(
                                                            url = it.webURL
                                                        )
                                                    })
                                                }
                                            },
                                            onLinkClick = {
                                                coroutineScope.launch {
                                                    com.sakethh.linkora.customWebTab.openInWeb(
                                                        recentlyVisitedData = RecentlyVisited(
                                                            title = it.title,
                                                            webURL = it.webURL,
                                                            baseURL = it.baseURL,
                                                            imgURL = it.imgURL,
                                                            infoForSaving = it.infoForSaving
                                                        ),
                                                        context = context,
                                                        uriHandler = uriHandler,
                                                        forceOpenInExternalBrowser = false
                                                    )
                                                }
                                            },
                                            webURL = it.webURL,
                                            onForceOpenInExternalBrowserClicked = {
                                                searchScreenVM.onLinkClick(
                                                    RecentlyVisited(
                                                        title = it.title,
                                                        webURL = it.webURL,
                                                        baseURL = it.baseURL,
                                                        imgURL = it.imgURL,
                                                        infoForSaving = it.infoForSaving
                                                    ),
                                                    context = context,
                                                    uriHandler = uriHandler,
                                                    onTaskCompleted = {},
                                                    forceOpenInExternalBrowser = true
                                                )
                                            })
                                    )
                                }
                                items(archiveLinksTableData) {
                                    LinkUIComponent(
                                        LinkUIComponentParam(title = it.title,
                                            webBaseURL = it.webURL,
                                            imgURL = it.imgURL,
                                            onMoreIconCLick = {
                                                selectedLinkTitle.value = it.title
                                                SearchScreenVM.selectedLinkType =
                                                    SearchScreenVM.SelectedLinkType.ARCHIVE_LINKS
                                                HomeScreenVM.tempImpLinkData.webURL = it.webURL
                                                HomeScreenVM.tempImpLinkData.baseURL = it.baseURL
                                                HomeScreenVM.tempImpLinkData.imgURL = it.imgURL
                                                HomeScreenVM.tempImpLinkData.title = it.title
                                                HomeScreenVM.tempImpLinkData.infoForSaving =
                                                    it.infoForSaving
                                                selectedURLNote.value = it.infoForSaving
                                                selectedWebURL.value = it.webURL
                                                shouldOptionsBtmModalSheetBeVisible.value = true
                                                coroutineScope.launch {
                                                    kotlinx.coroutines.awaitAll(async {
                                                        optionsBtmSheetVM.updateArchiveLinkCardData(
                                                            url = it.webURL
                                                        )
                                                    }, async {
                                                        optionsBtmSheetVM.updateImportantCardData(
                                                            url = it.webURL
                                                        )
                                                    })
                                                }
                                            },
                                            onLinkClick = {
                                                coroutineScope.launch {
                                                    com.sakethh.linkora.customWebTab.openInWeb(
                                                        recentlyVisitedData = RecentlyVisited(
                                                            title = it.title,
                                                            webURL = it.webURL,
                                                            baseURL = it.baseURL,
                                                            imgURL = it.imgURL,
                                                            infoForSaving = it.infoForSaving
                                                        ),
                                                        context = context,
                                                        uriHandler = uriHandler,
                                                        forceOpenInExternalBrowser = false
                                                    )
                                                }
                                            },
                                            webURL = it.webURL,
                                            onForceOpenInExternalBrowserClicked = {
                                                searchScreenVM.onLinkClick(
                                                    RecentlyVisited(
                                                        title = it.title,
                                                        webURL = it.webURL,
                                                        baseURL = it.baseURL,
                                                        imgURL = it.imgURL,
                                                        infoForSaving = it.infoForSaving
                                                    ),
                                                    context = context,
                                                    uriHandler = uriHandler,
                                                    onTaskCompleted = {},
                                                    forceOpenInExternalBrowser = true
                                                )
                                            })
                                    )
                                }
                                item {
                                    Spacer(modifier = Modifier.height(225.dp))
                                }
                            }
                        }
                    }
                })
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.background)
            ) {
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                item {
                    androidx.compose.foundation.layout.Row(modifier = Modifier
                        .clickable {
                            if (recentlyVisitedLinksData.isNotEmpty()) {
                                shouldSortingBottomSheetAppear.value = true
                            }
                        }
                        .fillMaxWidth()
                        .wrapContentHeight(),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        Text(
                            text = "History",
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(
                                start = 15.dp,
                                top = if (recentlyVisitedLinksData.isNotEmpty()) 0.dp else 11.dp
                            )
                        )
                        if (recentlyVisitedLinksData.isNotEmpty()) {
                            IconButton(onClick = {
                                shouldSortingBottomSheetAppear.value = true
                            }) {
                                Icon(
                                    imageVector = Icons.Outlined.Sort, contentDescription = null
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(5.dp))
                }
                if (recentlyVisitedLinksData.isNotEmpty()) {
                    items(recentlyVisitedLinksData) {
                        LinkUIComponent(
                            LinkUIComponentParam(title = it.title,
                                webBaseURL = it.baseURL,
                                imgURL = it.imgURL,
                                onMoreIconCLick = {
                                    selectedLinkTitle.value = it.title
                                    SearchScreenVM.selectedLinkType =
                                        SearchScreenVM.SelectedLinkType.HISTORY_LINKS
                                    HomeScreenVM.tempImpLinkData.webURL = it.webURL
                                    HomeScreenVM.tempImpLinkData.baseURL = it.baseURL
                                    HomeScreenVM.tempImpLinkData.imgURL = it.imgURL
                                    HomeScreenVM.tempImpLinkData.title = it.title
                                    HomeScreenVM.tempImpLinkData.infoForSaving = it.infoForSaving
                                    selectedURLNote.value = it.infoForSaving
                                    selectedWebURL.value = it.webURL
                                    shouldOptionsBtmModalSheetBeVisible.value = true
                                    coroutineScope.launch {
                                        kotlinx.coroutines.awaitAll(async {
                                            optionsBtmSheetVM.updateArchiveLinkCardData(url = it.webURL)
                                        }, async {
                                            optionsBtmSheetVM.updateImportantCardData(url = it.webURL)
                                        })
                                    }
                                },
                                onLinkClick = {
                                    coroutineScope.launch {
                                        com.sakethh.linkora.customWebTab.openInWeb(
                                            recentlyVisitedData = RecentlyVisited(
                                                title = it.title,
                                                webURL = it.webURL,
                                                baseURL = it.baseURL,
                                                imgURL = it.imgURL,
                                                infoForSaving = it.infoForSaving
                                            ),
                                            context = context,
                                            uriHandler = uriHandler,
                                            forceOpenInExternalBrowser = false
                                        )
                                    }
                                },
                                webURL = it.webURL,
                                onForceOpenInExternalBrowserClicked = {
                                    searchScreenVM.onLinkClick(
                                        RecentlyVisited(
                                            title = it.title,
                                            webURL = it.webURL,
                                            baseURL = it.baseURL,
                                            imgURL = it.imgURL,
                                            infoForSaving = it.infoForSaving
                                        ),
                                        context = context,
                                        uriHandler = uriHandler,
                                        onTaskCompleted = {},
                                        forceOpenInExternalBrowser = true
                                    )
                                })
                        )
                    }
                } else {
                    item {
                        DataEmptyScreen(text = "No Links were found in History.")
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(225.dp))
                }
            }
        }
        SortingBottomSheetUI(
            shouldBottomSheetVisible = shouldSortingBottomSheetAppear, onSelectedAComponent = {
                searchScreenVM.changeHistoryRetrievedData(sortingPreferences = it)
            }, bottomModalSheetState = sortingBtmSheetState
        )
        OptionsBtmSheetUI(
            OptionsBtmSheetUIParam(
                btmModalSheetState = optionsBtmSheetState,
                shouldBtmModalSheetBeVisible = shouldOptionsBtmModalSheetBeVisible,
                btmSheetFor = OptionsBtmSheetType.LINK,
                onDeleteCardClick = {
                    shouldDeleteDialogBoxAppear.value = true
                },
                onNoteDeleteCardClick = {
                    searchScreenVM.onNoteDeleteCardClick(
                        context = context,
                        selectedWebURL = selectedWebURL.value,
                        selectedLinkType = SearchScreenVM.selectedLinkType,
                        folderID = selectedFolderID
                    )
                },
                onRenameClick = {
                    shouldRenameDialogBoxAppear.value = true
                },
                onArchiveClick = {
                    searchScreenVM.onArchiveClick(
                        context,
                        selectedLinkType = SearchScreenVM.selectedLinkType,
                        folderID = selectedFolderID
                    )
                },
                importantLinks = HomeScreenVM.tempImpLinkData,
                noteForSaving = selectedURLNote.value,
                folderName = "",
                linkTitle = selectedLinkTitle.value
            )
        )
        RenameDialogBox(
            RenameDialogBoxParam(
                shouldDialogBoxAppear = shouldRenameDialogBoxAppear,
                existingFolderName = "",
                onNoteChangeClickForLinks = { newNote ->
                    searchScreenVM.onNoteChangeClickForLinks(
                        HomeScreenVM.tempImpLinkData.webURL,
                        newNote,
                        selectedLinkType = SearchScreenVM.selectedLinkType,
                        folderID = selectedFolderID
                    )
                },
                renameDialogBoxFor = OptionsBtmSheetType.LINK,
                onTitleChangeClickForLinks = { newTitle ->
                    searchScreenVM.onTitleChangeClickForLinks(
                        HomeScreenVM.tempImpLinkData.webURL,
                        newTitle,
                        selectedLinkType = SearchScreenVM.selectedLinkType,
                        folderID = selectedFolderID
                    )
                },
                currentFolderID = selectedFolderID,
                parentFolderID = null
            )
        )
        DeleteDialogBox(
            DeleteDialogBoxParam(shouldDialogBoxAppear = shouldDeleteDialogBoxAppear,
                deleteDialogBoxType = DataDialogBoxType.LINK,
                onDeleteClick = {
                    searchScreenVM.onDeleteClick(
                        context = context,
                        selectedWebURL = selectedWebURL.value,
                        shouldDeleteBoxAppear = shouldDeleteDialogBoxAppear,
                        selectedLinkType = SearchScreenVM.selectedLinkType,
                        folderID = selectedFolderID
                    )
                })
        )
    }
    val activity = LocalContext.current as? Activity
    BackHandler {
        when {
            SearchScreenVM.isSearchEnabled.value -> {
                SearchScreenVM.isSearchEnabled.value = false
            }

            else -> if (SettingsScreenVM.Settings.isHomeScreenEnabled.value) {
                navController.navigate(NavigationRoutes.HOME_SCREEN.name) {
                    popUpTo(0)
                }
            } else {
                activity?.finish()
            }
        }
    }
}