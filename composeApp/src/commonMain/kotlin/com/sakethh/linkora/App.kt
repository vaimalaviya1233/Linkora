package com.sakethh.linkora

import androidx.compose.animation.core.Animatable
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.material3.rememberStandardBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.inRootScreen
import com.sakethh.linkora.common.utils.isNotNull
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.components.AddANewFolderDialogBox
import com.sakethh.linkora.ui.components.AddANewLinkDialogBox
import com.sakethh.linkora.ui.components.AddItemFABParam
import com.sakethh.linkora.ui.components.AddItemFab
import com.sakethh.linkora.ui.components.DeleteDialogBox
import com.sakethh.linkora.ui.components.DeleteDialogBoxParam
import com.sakethh.linkora.ui.components.DeleteDialogBoxType
import com.sakethh.linkora.ui.components.RenameDialogBox
import com.sakethh.linkora.ui.components.RenameDialogBoxParam
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetParam
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetUI
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetVM
import com.sakethh.linkora.ui.components.menu.menuBtmSheetFolderEntries
import com.sakethh.linkora.ui.components.sorting.SortingBottomSheetParam
import com.sakethh.linkora.ui.components.sorting.SortingBottomSheetUI
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.domain.SortingBtmSheetType
import com.sakethh.linkora.ui.domain.model.AddNewFolderDialogBoxParam
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.CollectionDetailPane
import com.sakethh.linkora.ui.screens.collections.CollectionsScreen
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.home.HomeScreen
import com.sakethh.linkora.ui.screens.home.panels.PanelsManagerScreen
import com.sakethh.linkora.ui.screens.home.panels.SpecificPanelManagerScreen
import com.sakethh.linkora.ui.screens.search.SearchScreen
import com.sakethh.linkora.ui.screens.settings.SettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.AcknowledgementSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.AdvancedSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.GeneralSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.LanguageSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.LayoutSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.ThemeSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.about.AboutSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerSetupScreen
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.linkora.ui.utils.rememberDeserializableMutableObject
import com.sakethh.linkora.ui.utils.rememberDeserializableObject
import com.sakethh.platform
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    modifier: Modifier = Modifier
) {
    viewModel<AppVM>(factory = genericViewModelFactory {
        AppVM(localFoldersRepo = DependencyContainer.localFoldersRepo.value)
    })
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val shouldRenameDialogBoxBeVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldDeleteDialogBoxBeVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val menuBtmModalSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val selectedFolderForMenuBtmSheet = rememberDeserializableMutableObject {
        mutableStateOf(
            Folder(
                name = "", note = "", parentFolderId = null, localId = 0L, isArchived = false
            )
        )
    }
    val selectedLinkForMenuBtmSheet = rememberDeserializableMutableObject {
        mutableStateOf(
            Link(
                linkType = LinkType.SAVED_LINK,
                localId = 0L,
                title = "",
                url = "",
                baseURL = "",
                imgURL = "",
                note = "",
                lastModified = "",
                idOfLinkedFolder = null,
                userAgent = null
            )
        )
    }
    val shouldMenuBtmModalSheetBeVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldShowAddLinkDialog = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldShowNewFolderDialog = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldSortingBottomSheetAppear = rememberSaveable {
        mutableStateOf(false)
    }
    val sortingBtmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val menuBtmSheetFor: MutableState<MenuBtmSheetType> = rememberDeserializableMutableObject {
        mutableStateOf(MenuBtmSheetType.Folder.RegularFolder)
    }

    val menuBtmSheetVM: MenuBtmSheetVM = viewModel(factory = genericViewModelFactory {
        MenuBtmSheetVM(DependencyContainer.localLinksRepo.value)
    })

    LaunchedEffect(Unit) {
        UIEvent.uiEventsReadOnlyChannel.collectLatest { eventType ->
            when (eventType) {
                is UIEvent.Type.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = eventType.message)
                }

                is UIEvent.Type.ShowAddANewFolderDialogBox -> shouldShowNewFolderDialog.value = true
                is UIEvent.Type.ShowAddANewLinkDialogBox -> shouldShowAddLinkDialog.value = true
                is UIEvent.Type.ShowDeleteDialogBox -> shouldDeleteDialogBoxBeVisible.value = true

                is UIEvent.Type.ShowMenuBtmSheetUI -> {
                    menuBtmSheetFor.value = eventType.menuBtmSheetFor
                    if (eventType.selectedFolderForMenuBtmSheet.isNotNull()) {
                        menuBtmSheetVM.updateArchiveFolderCardData(eventType.selectedFolderForMenuBtmSheet!!.isArchived)
                        selectedFolderForMenuBtmSheet.value =
                            eventType.selectedFolderForMenuBtmSheet
                    }
                    if (eventType.selectedLinkForMenuBtmSheet.isNotNull()) {
                        menuBtmSheetVM.updateImpLinkInfo(eventType.selectedLinkForMenuBtmSheet!!.url)
                        menuBtmSheetVM.updateArchiveLinkInfo(eventType.selectedLinkForMenuBtmSheet.url)
                        selectedLinkForMenuBtmSheet.value = eventType.selectedLinkForMenuBtmSheet
                    }
                    shouldMenuBtmModalSheetBeVisible.value = true
                    this.launch {
                        menuBtmModalSheetState.show()
                    }
                }

                is UIEvent.Type.ShowRenameDialogBox -> shouldRenameDialogBoxBeVisible.value = true

                is UIEvent.Type.ShowSortingBtmSheetUI -> {
                    shouldSortingBottomSheetAppear.value = true
                    this.launch {
                        sortingBtmSheetState.show()
                    }
                }
            }
        }
    }
    val collectionsScreenVM = viewModel<CollectionsScreenVM>(factory = genericViewModelFactory {
        CollectionsScreenVM(
            DependencyContainer.localFoldersRepo.value, DependencyContainer.localLinksRepo.value
        )
    })
    val rootRouteList = rememberDeserializableObject {
        listOf(
            Navigation.Root.HomeScreen,
            Navigation.Root.SearchScreen,
            Navigation.Root.CollectionsScreen,
            Navigation.Root.SettingsScreen,
        )
    }
    val localNavController = LocalNavController.current
    val inRootScreen = localNavController.inRootScreen(includeSettingsScreen = true)
    val currentBackStackEntryState = localNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntryState.value?.destination
    val standardBottomSheet =
        rememberStandardBottomSheetState(skipHiddenState = false, initialValue = SheetValue.Hidden)
    val scaffoldSheetState =
        rememberBottomSheetScaffoldState(bottomSheetState = standardBottomSheet)
    val btmModalSheetStateForSavingLinks = rememberModalBottomSheetState()

    val isMainFabRotated = rememberSaveable {
        mutableStateOf(false)
    }
    val rotationAnimation = remember {
        Animatable(0f)
    }
    val shouldScreenTransparencyDecreasedBoxVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldBtmSheetForNewLinkAdditionBeEnabled = rememberSaveable {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val showAddingLinkOrFoldersFAB = rememberSaveable {
        mutableStateOf(false)
    }
    val platform = LocalPlatform.current
    LaunchedEffect(
        key1 = currentBackStackEntryState.value,
        key2 = CollectionsScreenVM.collectionDetailPaneInfo.value
    ) {
        launch {
            if (rootRouteList.any {
                    currentBackStackEntryState.value?.destination?.hasRoute(it::class) == true
                }) {
                scaffoldSheetState.bottomSheetState.expand()
            } else {
                scaffoldSheetState.bottomSheetState.hide()
            }
        }
        launch {
            if (platform is Platform.Android.Mobile && currentBackStackEntryState.value?.destination?.hasRoute(
                    Navigation.Root.CollectionsScreen::class
                ) == true
            ) {
                CollectionsScreenVM.resetCollectionDetailPaneInfo()
            }
            if (currentBackStackEntryState.value?.destination?.hasRoute(Navigation.Root.CollectionsScreen::class) == true && platform !is Platform.Android.Mobile && CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.ARCHIVE_ID) {
                showAddingLinkOrFoldersFAB.value = false
            }
            showAddingLinkOrFoldersFAB.value = listOf(
                Navigation.Root.HomeScreen,
                Navigation.Root.SearchScreen,
                Navigation.Root.CollectionsScreen,
                Navigation.Collection.CollectionDetailPane
            ).any {
                currentBackStackEntryState.value?.destination?.hasRoute(it::class) == true
            } && CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId != Constants.ARCHIVE_ID
        }
    }
    Row(modifier = Modifier.fillMaxSize().then(modifier)) {
        if (platform() == Platform.Desktop || platform() == Platform.Android.Tablet) {
            Row {
                Box(modifier = Modifier.fillMaxHeight()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        rootRouteList.forEach { navRouteItem ->
                            val isSelected =
                                currentRoute?.hasRoute(navRouteItem::class) == true
                            NavigationRailItem(
                                modifier = Modifier.padding(
                                    start = 15.dp, end = 15.dp, top = 15.dp
                                ), selected = isSelected, onClick = {
                                    if (currentRoute?.hasRoute(navRouteItem::class) == false) {
                                        CollectionsScreenVM.resetCollectionDetailPaneInfo()
                                        localNavController.navigate(navRouteItem)
                                    }
                                }, icon = {
                                    Icon(
                                        imageVector = if (isSelected) {
                                            when (navRouteItem) {
                                                Navigation.Root.HomeScreen -> Icons.Filled.Home
                                                Navigation.Root.SearchScreen -> Icons.Filled.Search
                                                Navigation.Root.CollectionsScreen -> Icons.Filled.Folder
                                                Navigation.Root.SettingsScreen -> Icons.Filled.Settings
                                                else -> return@NavigationRailItem
                                            }
                                        } else {
                                            when (navRouteItem) {
                                                Navigation.Root.HomeScreen -> Icons.Outlined.Home
                                                Navigation.Root.SearchScreen -> Icons.Outlined.Search
                                                Navigation.Root.CollectionsScreen -> Icons.Outlined.Folder
                                                Navigation.Root.SettingsScreen -> Icons.Outlined.Settings
                                                else -> return@NavigationRailItem
                                            }
                                        }, contentDescription = null
                                    )
                                }, label = {
                                    Text(
                                        text = navRouteItem.toString(),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                })
                        }
                    }
                }
                VerticalDivider()
            }
        }
        Scaffold(
            floatingActionButton = {
                if (showAddingLinkOrFoldersFAB.value) {
                    if (CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId in listOf(
                            Constants.SAVED_LINKS_ID,
                            Constants.IMPORTANT_LINKS_ID,
                            Constants.ALL_LINKS_ID
                        )
                    ) {
                        FloatingActionButton(
                            modifier = Modifier.padding(
                                bottom = if (platform() == Platform.Android.Mobile && rootRouteList.any {
                                        currentRoute?.hasRoute(it::class) == true
                                    }) 82.dp else 0.dp
                            ), onClick = {
                                shouldShowAddLinkDialog.value = true
                            }) {
                            Icon(
                                imageVector = Icons.Default.AddLink, contentDescription = null
                            )
                        }
                        return@Scaffold
                    }
                    AddItemFab(
                        AddItemFABParam(
                            newLinkBottomModalSheetState = btmModalSheetStateForSavingLinks,
                            shouldBtmSheetForNewLinkAdditionBeEnabled = shouldBtmSheetForNewLinkAdditionBeEnabled,
                            shouldScreenTransparencyDecreasedBoxVisible = shouldScreenTransparencyDecreasedBoxVisible,
                            shouldDialogForNewFolderAppear = shouldShowNewFolderDialog,
                            shouldDialogForNewLinkAppear = shouldShowAddLinkDialog,
                            isMainFabRotated = isMainFabRotated,
                            rotationAnimation = rotationAnimation,
                            inASpecificScreen = false
                        )
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(snackbarHostState, snackbar = {
                    Snackbar(
                        it,
                        containerColor = MaterialTheme.colorScheme.surfaceContainerHighest,
                        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                })
            }, modifier = Modifier.fillMaxSize()
        ) {
            BottomSheetScaffold(
                sheetDragHandle = {},
                sheetPeekHeight = 0.dp,
                scaffoldState = scaffoldSheetState,
                sheetSwipeEnabled = false,
                sheetShape = RectangleShape,
                sheetContent = {
                    if (platform() == Platform.Android.Mobile) {
                        NavigationBar {
                            rootRouteList.forEach { navRouteItem ->
                                val isSelected = currentRoute?.hasRoute(navRouteItem::class) == true
                                NavigationBarItem(selected = isSelected, onClick = {
                                    isSelected.ifNot {
                                        CollectionsScreenVM.resetCollectionDetailPaneInfo()
                                        localNavController.navigate(navRouteItem)
                                    }
                                }, icon = {
                                    Icon(
                                        imageVector = if (isSelected) {
                                            when (navRouteItem) {
                                                Navigation.Root.HomeScreen -> Icons.Filled.Home
                                                Navigation.Root.SearchScreen -> Icons.Filled.Search
                                                Navigation.Root.CollectionsScreen -> Icons.Filled.Folder
                                                Navigation.Root.SettingsScreen -> Icons.Filled.Settings
                                                else -> return@NavigationBarItem
                                            }
                                        } else {
                                            when (navRouteItem) {
                                                Navigation.Root.HomeScreen -> Icons.Outlined.Home
                                                Navigation.Root.SearchScreen -> Icons.Outlined.Search
                                                Navigation.Root.CollectionsScreen -> Icons.Outlined.Folder
                                                Navigation.Root.SettingsScreen -> Icons.Outlined.Settings
                                                else -> return@NavigationBarItem
                                            }
                                        }, contentDescription = null
                                    )
                                }, label = {
                                    Text(
                                        text = navRouteItem.toString(),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                })
                            }
                        }
                    }
                }) {
                NavHost(
                    navController = localNavController,
                    startDestination = Navigation.Root.HomeScreen
                ) {
                    composable<Navigation.Root.HomeScreen> {
                        HomeScreen()
                    }
                    composable<Navigation.Root.SearchScreen> {
                        SearchScreen()
                    }
                    composable<Navigation.Root.CollectionsScreen> {
                        CollectionsScreen(
                            collectionsScreenVM = collectionsScreenVM
                        )
                    }
                    composable<Navigation.Root.SettingsScreen> {
                        SettingsScreen()
                    }
                    composable<Navigation.Settings.ThemeSettingsScreen> {
                        ThemeSettingsScreen()
                    }
                    composable<Navigation.Settings.GeneralSettingsScreen> {
                        GeneralSettingsScreen()
                    }
                    composable<Navigation.Settings.LayoutSettingsScreen> {
                        LayoutSettingsScreen()
                    }
                    composable<Navigation.Settings.DataSettingsScreen> {
                        DataSettingsScreen()
                    }
                    composable<Navigation.Settings.Data.ServerSetupScreen> {
                        ServerSetupScreen()
                    }
                    composable<Navigation.Settings.LanguageSettingsScreen> {
                        LanguageSettingsScreen()
                    }
                    composable<Navigation.Collection.CollectionDetailPane> {
                        CollectionDetailPane()
                    }
                    composable<Navigation.Home.PanelsManagerScreen> {
                        PanelsManagerScreen()
                    }
                    composable<Navigation.Home.SpecificPanelManagerScreen> {
                        SpecificPanelManagerScreen()
                    }
                    composable<Navigation.Settings.AboutSettingsScreen> {
                        AboutSettingsScreen()
                    }
                    composable<Navigation.Settings.AcknowledgementSettingsScreen> {
                        AcknowledgementSettingsScreen()
                    }
                    composable<Navigation.Settings.AdvancedSettingsScreen> {
                        AdvancedSettingsScreen()
                    }
                }
            }

            if (shouldScreenTransparencyDecreasedBoxVisible.value) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(0.95f)).clickable {
                            shouldScreenTransparencyDecreasedBoxVisible.value = false
                            coroutineScope.launch {
                                kotlinx.coroutines.awaitAll(async {
                                    rotationAnimation.animateTo(
                                        -360f,
                                        animationSpec = androidx.compose.animation.core.tween(300)
                                    )
                                }, async { isMainFabRotated.value = false })
                            }.invokeOnCompletion {
                                coroutineScope.launch {
                                    rotationAnimation.snapTo(0f)
                                }
                            }
                        })
            }
            AddANewLinkDialogBox(
                shouldBeVisible = shouldShowAddLinkDialog,
                screenType = ScreenType.ROOT_SCREEN,
                currentFolder = if ((inRootScreen == true && platform is Platform.Android.Mobile) || CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.ALL_LINKS_ID) null else CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder,
                collectionsScreenVM
            )

            AddANewFolderDialogBox(
                AddNewFolderDialogBoxParam(
                    shouldBeVisible = shouldShowNewFolderDialog,
                    inAChildFolderScreen = CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId != null && CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId!! > 0,
                    onFolderCreateClick = { folderName, folderNote, onCompletion ->
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.insertANewFolder(
                                folder = Folder(
                                    name = folderName,
                                    note = folderNote,
                                    parentFolderId = if ((CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId
                                            ?: 0) > 0
                                    ) CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId else null
                                ), ignoreFolderAlreadyExistsThrowable = false, onCompletion = {
                                    collectionsScreenVM.triggerFoldersSorting()
                                    onCompletion()
                                })
                        }
                    },
                    thisFolder = CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder
                )
            )
            val localUriHandler = LocalUriHandler.current
            MenuBtmSheetUI(
                menuBtmSheetParam = MenuBtmSheetParam(
                    btmModalSheetState = menuBtmModalSheetState,
                    shouldBtmModalSheetBeVisible = shouldMenuBtmModalSheetBeVisible,
                    menuBtmSheetFor = menuBtmSheetFor.value,
                    onDelete = {
                        shouldDeleteDialogBoxBeVisible.value = true
                    },
                    onRename = {
                        shouldRenameDialogBoxBeVisible.value = true
                    },
                    onArchive = {
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.archiveAFolder(
                                selectedFolderForMenuBtmSheet.value, onCompletion = {
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        shouldMenuBtmModalSheetBeVisible.value = false
                                    }
                                    collectionsScreenVM.triggerFoldersSorting()
                                })
                        } else {
                            collectionsScreenVM.archiveALink(
                                selectedLinkForMenuBtmSheet.value, onCompletion = {
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        shouldMenuBtmModalSheetBeVisible.value = false
                                    }
                                    collectionsScreenVM.triggerLinksSorting()
                                })
                        }
                    },
                    onDeleteNote = {
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.deleteTheNote(selectedFolderForMenuBtmSheet.value)
                        } else {
                            collectionsScreenVM.deleteTheNote(selectedLinkForMenuBtmSheet.value)
                        }
                        coroutineScope.launch {
                            menuBtmModalSheetState.hide()
                        }.invokeOnCompletion {
                            shouldMenuBtmModalSheetBeVisible.value = false
                        }
                    },
                    onRefreshClick = {
                        collectionsScreenVM.refreshLinkMetadata(selectedLinkForMenuBtmSheet.value)
                        coroutineScope.launch {
                            menuBtmModalSheetState.hide()
                        }.invokeOnCompletion {
                            shouldMenuBtmModalSheetBeVisible.value = false
                        }
                    },
                    onForceLaunchInAnExternalBrowser = {
                        localUriHandler.openUri(selectedLinkForMenuBtmSheet.value.url)
                    },
                    showQuickActions = rememberSaveable { mutableStateOf(false) },
                    shouldTransferringOptionShouldBeVisible = true,
                    link = selectedLinkForMenuBtmSheet,
                    folder = selectedFolderForMenuBtmSheet,
                    onAddToImportantLinks = {
                        collectionsScreenVM.markALinkAsImp(selectedLinkForMenuBtmSheet.value)
                        coroutineScope.launch {
                            menuBtmModalSheetState.hide()
                        }.invokeOnCompletion {
                            shouldMenuBtmModalSheetBeVisible.value = false
                        }
                        Unit
                    },
                    shouldShowArchiveOption = {
                        menuBtmSheetVM.shouldShowArchiveOption(selectedLinkForMenuBtmSheet.value.url)
                    })
            )
            DeleteDialogBox(
                DeleteDialogBoxParam(
                    shouldDeleteDialogBoxBeVisible,
                    if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                        DeleteDialogBoxType.FOLDER
                    } else DeleteDialogBoxType.LINK,
                    onDeleteClick = { onCompletion ->
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.deleteAFolder(
                                selectedFolderForMenuBtmSheet.value, onCompletion = {
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        shouldMenuBtmModalSheetBeVisible.value = false
                                    }
                                    collectionsScreenVM.triggerFoldersSorting()
                                    onCompletion()
                                })
                        } else {
                            collectionsScreenVM.deleteALink(
                                selectedLinkForMenuBtmSheet.value,
                                onCompletion = {
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        shouldMenuBtmModalSheetBeVisible.value = false
                                    }
                                collectionsScreenVM.triggerLinksSorting()
                                onCompletion()
                            })
                        }
                    })
            )
            RenameDialogBox(
                RenameDialogBoxParam(
                    onNoteChangeClick = {
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.updateFolderNote(
                                selectedFolderForMenuBtmSheet.value.localId,
                                newNote = it,
                                onCompletion = {})
                        } else {
                            collectionsScreenVM.updateLinkNote(
                                selectedLinkForMenuBtmSheet.value.localId,
                                newNote = it,
                                onCompletion = {})
                        }
                        shouldRenameDialogBoxBeVisible.value = false
                    },
                    shouldDialogBoxAppear = shouldRenameDialogBoxBeVisible,
                    existingFolderName = selectedFolderForMenuBtmSheet.value.name,
                    onBothTitleAndNoteChangeClick = { title, note ->
                        if (menuBtmSheetFor.value in menuBtmSheetFolderEntries()) {
                            collectionsScreenVM.updateFolderNote(
                                selectedFolderForMenuBtmSheet.value.localId,
                                newNote = note,
                                pushSnackbarOnSuccess = false,
                                onCompletion = {})
                            collectionsScreenVM.updateFolderName(
                                folder = selectedFolderForMenuBtmSheet.value,
                                newName = title,
                                ignoreFolderAlreadyExistsThrowable = true,
                                onCompletion = {
                                    collectionsScreenVM.triggerFoldersSorting()
                                })
                        } else {
                            collectionsScreenVM.updateLinkNote(
                                linkId = selectedLinkForMenuBtmSheet.value.localId,
                                newNote = note,
                                pushSnackbarOnSuccess = false,
                                onCompletion = {})
                            collectionsScreenVM.updateLinkTitle(
                                linkId = selectedLinkForMenuBtmSheet.value.localId,
                                newTitle = title,
                                onCompletion = {
                                    collectionsScreenVM.triggerLinksSorting()
                                })
                        }
                        shouldRenameDialogBoxBeVisible.value = false
                        coroutineScope.launch {
                            menuBtmModalSheetState.hide()
                        }.invokeOnCompletion {
                            shouldMenuBtmModalSheetBeVisible.value = false
                        }
                    },
                    existingTitle = if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) selectedFolderForMenuBtmSheet.value.name else selectedLinkForMenuBtmSheet.value.title,
                    existingNote = if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) selectedFolderForMenuBtmSheet.value.note else selectedLinkForMenuBtmSheet.value.note
                )
            )

            SortingBottomSheetUI(
                SortingBottomSheetParam(
                    shouldBottomSheetBeVisible = shouldSortingBottomSheetAppear,
                    onSelected = { sortingPreferences, _, _ -> },
                    bottomModalSheetState = sortingBtmSheetState,
                    sortingBtmSheetType = SortingBtmSheetType.COLLECTIONS_SCREEN,
                    shouldFoldersSelectionBeVisible = mutableStateOf(false),
                    shouldLinksSelectionBeVisible = mutableStateOf(false)
                )
            )
        }
    }
}