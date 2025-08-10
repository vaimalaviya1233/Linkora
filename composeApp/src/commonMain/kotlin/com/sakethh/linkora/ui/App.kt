package com.sakethh.linkora.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakethh.linkora.preferences.AppPreferences.serverBaseUrl
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.currentSavedServerConfig
import com.sakethh.linkora.utils.inRootScreen
import com.sakethh.linkora.utils.initializeIfServerConfigured
import com.sakethh.linkora.di.CollectionScreenVMAssistedFactory
import com.sakethh.linkora.di.linkoraViewModel
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.components.AddANewFolderDialogBox
import com.sakethh.linkora.ui.components.AddANewLinkDialogBox
import com.sakethh.linkora.ui.components.AddItemFABParam
import com.sakethh.linkora.ui.components.AddItemFab
import com.sakethh.linkora.ui.components.BottomNavOnSelection
import com.sakethh.linkora.ui.components.DeleteDialogBox
import com.sakethh.linkora.ui.components.DeleteDialogBoxParam
import com.sakethh.linkora.ui.components.DeleteDialogBoxType
import com.sakethh.linkora.ui.components.DesktopNavigationRail
import com.sakethh.linkora.ui.components.MobileBottomNavBar
import com.sakethh.linkora.ui.components.RenameDialogBox
import com.sakethh.linkora.ui.components.RenameDialogBoxParam
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetParam
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetUI
import com.sakethh.linkora.ui.components.menu.menuBtmSheetFolderEntries
import com.sakethh.linkora.ui.components.sorting.SortingBottomSheetParam
import com.sakethh.linkora.ui.components.sorting.SortingBottomSheetUI
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.domain.SortingBtmSheetType
import com.sakethh.linkora.ui.domain.TransferActionType
import com.sakethh.linkora.ui.domain.model.AddNewFolderDialogBoxParam
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.CollectionDetailPane
import com.sakethh.linkora.ui.screens.collections.CollectionsScreen
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.home.HomeScreen
import com.sakethh.linkora.ui.screens.home.panels.PanelsManagerScreen
import com.sakethh.linkora.ui.screens.home.panels.SpecificPanelManagerScreen
import com.sakethh.linkora.ui.screens.onboarding.OnboardingSlidesScreen
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
import com.sakethh.linkora.ui.screens.settings.section.data.snapshots.SnapshotsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerSetupScreen
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.rememberDeserializableMutableObject
import com.sakethh.linkora.ui.utils.rememberDeserializableObject
import com.sakethh.linkora.platform.platform
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Suppress("UnusedMaterial3ScaffoldPaddingParameter")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App(
    modifier: Modifier = Modifier
) {
    val appVM: AppVM = linkoraViewModel()
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val showRenameDialogBox = rememberSaveable {
        mutableStateOf(false)
    }
    val showDeleteDialogBox = rememberSaveable {
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
                idOfLinkedFolder = null,
                userAgent = null
            )
        )
    }
    val menuBtmModalSheetVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldShowAddLinkDialog = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldShowNewFolderDialog = rememberSaveable {
        mutableStateOf(false)
    }
    val sortingBottomSheetVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val sortingBtmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val menuBtmSheetFor: MutableState<MenuBtmSheetType> = rememberDeserializableMutableObject {
        mutableStateOf(MenuBtmSheetType.Folder.RegularFolder)
    }

    LaunchedEffect(Unit) {
        UIEvent.uiEvents.collectLatest { eventType ->
            when (eventType) {
                is UIEvent.Type.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = eventType.message)
                }

                is UIEvent.Type.ShowAddANewFolderDialogBox -> shouldShowNewFolderDialog.value = true
                is UIEvent.Type.ShowAddANewLinkDialogBox -> shouldShowAddLinkDialog.value = true
                is UIEvent.Type.ShowDeleteDialogBox -> showDeleteDialogBox.value = true

                is UIEvent.Type.ShowMenuBtmSheetUI -> {
                    menuBtmSheetFor.value = eventType.menuBtmSheetFor
                    if (eventType.selectedFolderForMenuBtmSheet != null) {
                        selectedFolderForMenuBtmSheet.value =
                            eventType.selectedFolderForMenuBtmSheet
                    }
                    if (eventType.selectedLinkForMenuBtmSheet != null) {
                        selectedLinkForMenuBtmSheet.value = eventType.selectedLinkForMenuBtmSheet
                    }
                    menuBtmModalSheetVisible.value = true
                    this.launch {
                        menuBtmModalSheetState.show()
                    }
                }

                is UIEvent.Type.ShowRenameDialogBox -> showRenameDialogBox.value = true

                is UIEvent.Type.ShowSortingBtmSheetUI -> {
                    sortingBottomSheetVisible.value = true
                    this.launch {
                        sortingBtmSheetState.show()
                    }
                }

                else -> Unit
            }
        }
    }
    val collectionsScreenVM: CollectionsScreenVM =
        linkoraViewModel(factory = CollectionScreenVMAssistedFactory.createForApp())
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
    val currentBackStackEntryState by localNavController.currentBackStackEntryAsState()
    val currentRoute = currentBackStackEntryState?.destination

    val rotationAnimation = remember {
        Animatable(0f)
    }
    val isReducedTransparencyBoxVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val showBtmSheetForNewLinkAddition = rememberSaveable {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val showAddingLinkOrFoldersFAB = rememberSaveable {
        mutableStateOf(false)
    }
    val platform = LocalPlatform.current
    LaunchedEffect(
        key1 = currentBackStackEntryState,
        key2 = CollectionsScreenVM.collectionDetailPaneInfo.value,
        key3 = CollectionsScreenVM.isSelectionEnabled.value
    ) {
        launch {
            if (platform is Platform.Android.Mobile && currentBackStackEntryState?.destination?.hasRoute(
                    Navigation.Root.CollectionsScreen::class
                ) == true
            ) {
                CollectionsScreenVM.resetCollectionDetailPaneInfo()
            }
            if (currentBackStackEntryState?.destination?.hasRoute(Navigation.Root.CollectionsScreen::class) == true && platform !is Platform.Android.Mobile && CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.ARCHIVE_ID) {
                showAddingLinkOrFoldersFAB.value = false
            }
            showAddingLinkOrFoldersFAB.value = listOf(
                Navigation.Root.HomeScreen,
                Navigation.Root.SearchScreen,
                Navigation.Root.CollectionsScreen,
                Navigation.Collection.CollectionDetailPane
            ).any {
                currentBackStackEntryState?.destination?.hasRoute(it::class) == true
            } && CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId != Constants.ARCHIVE_ID
        }
    }

    val isDataSyncingFromPullRefresh = rememberSaveable {
        mutableStateOf(false)
    }
    val pullToRefreshState = rememberPullToRefreshState()

    LaunchedEffect(AppVM.isMainFabRotated.value) {
        if (!AppVM.isMainFabRotated.value) {
            isReducedTransparencyBoxVisible.value = false
            rotationAnimation.animateTo(
                -180f, animationSpec = tween(500)
            )
        }
    }
    Row(modifier = Modifier.fillMaxSize().then(modifier)) {
        if (appVM.onBoardingCompleted.value && (platform() == Platform.Desktop || platform() == Platform.Android.Tablet)) {
            DesktopNavigationRail(
                rootRouteList = rootRouteList,
                appVM = appVM,
                currentRoute = currentRoute,
                isDataSyncingFromPullRefresh = isDataSyncingFromPullRefresh
            )
        }
        val showLoadingProgressBarOnTransferAction = rememberSaveable {
            mutableStateOf(false)
        }
        val selectedAndInRoot = rememberSaveable(inRootScreen, appVM.transferActionType.value) {
            mutableStateOf((inRootScreen == true) && (appVM.transferActionType.value != TransferActionType.NONE))
        }

        Scaffold(
            bottomBar = {
                Box(modifier = Modifier.animateContentSize()) {
                    if (CollectionsScreenVM.isSelectionEnabled.value) {
                        BottomNavOnSelection(
                            showLoadingProgressBarOnTransferAction = showLoadingProgressBarOnTransferAction,
                            appVM = appVM,
                            selectedAndInRoot = selectedAndInRoot,
                            currentRoute = currentRoute
                        )
                    }
                }
                MobileBottomNavBar(
                    rootRouteList = rootRouteList,
                    appVM = appVM,
                    platform = platform,
                    inRootScreen = inRootScreen,
                    currentRoute = currentRoute
                )
            },
            floatingActionButton = {
                AnimatedVisibility(
                    enter = fadeIn(),
                    exit = fadeOut(),
                    visible = showAddingLinkOrFoldersFAB.value && !CollectionsScreenVM.isSelectionEnabled.value
                ) {
                    if (CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId in listOf(
                            Constants.SAVED_LINKS_ID,
                            Constants.IMPORTANT_LINKS_ID,
                            Constants.ALL_LINKS_ID
                        )
                    ) {
                        FloatingActionButton(onClick = {
                            shouldShowAddLinkDialog.value = true
                        }) {
                            Icon(
                                imageVector = Icons.Default.AddLink, contentDescription = null
                            )
                        }
                        return@AnimatedVisibility
                    }
                    AddItemFab(
                        AddItemFABParam(
                            showBtmSheetForNewLinkAddition = showBtmSheetForNewLinkAddition,
                            isReducedTransparencyBoxVisible = isReducedTransparencyBoxVisible,
                            showDialogForNewFolder = shouldShowNewFolderDialog,
                            shouldShowAddLinkDialog = shouldShowAddLinkDialog,
                            isMainFabRotated = AppVM.isMainFabRotated,
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
            },
            modifier = Modifier.fillMaxSize(),
        ) {
            Box(
                modifier = Modifier.pullToRefresh(
                    isRefreshing = isDataSyncingFromPullRefresh.value,
                    state = pullToRefreshState,
                    enabled = rememberSaveable(serverBaseUrl.value) {
                        serverBaseUrl.value.isNotBlank() && platform == Platform.Android.Mobile
                    },
                    onRefresh = {
                        appVM.saveServerConnectionAndSync(
                            serverConnection = currentSavedServerConfig(),
                            timeStampAfter = {
                                appVM.getLastSyncedTime()
                            },
                            onSyncStart = {
                                isDataSyncingFromPullRefresh.value = true
                            },
                            onCompletion = {
                                isDataSyncingFromPullRefresh.value = false
                            })
                    })
            ) {
                NavHost(
                    navController = localNavController,
                    startDestination = appVM.startDestination.value
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
                    composable<Navigation.Settings.Data.SnapshotsScreen> {
                        SnapshotsScreen()
                    }
                    composable<Navigation.Root.OnboardingSlidesScreen> {
                        OnboardingSlidesScreen(onOnboardingComplete = {
                            appVM.markOnboardingComplete()
                        })
                    }
                }
                Indicator(
                    state = pullToRefreshState,
                    isRefreshing = isDataSyncingFromPullRefresh.value,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
            AnimatedVisibility(
                visible = isReducedTransparencyBoxVisible.value, enter = fadeIn(), exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier.fillMaxSize()
                        .background(MaterialTheme.colorScheme.background.copy(0.95f)).clickable {
                            AppVM.isMainFabRotated.value = false
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

                                    onCompletion()
                                })
                        }
                    },
                    thisFolder = CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder
                )
            )
            val localUriHandler = LocalUriHandler.current
            val showProgressBarDuringRemoteSave = rememberSaveable {
                mutableStateOf(false)
            }
            MenuBtmSheetUI(
                menuBtmSheetParam = MenuBtmSheetParam(
                    btmModalSheetState = menuBtmModalSheetState,
                    shouldBtmModalSheetBeVisible = menuBtmModalSheetVisible,
                    menuBtmSheetFor = menuBtmSheetFor.value,
                    onDelete = {
                        showDeleteDialogBox.value = true
                    },
                    onRename = {
                        showRenameDialogBox.value = true
                    },
                    onArchive = {
                        initializeIfServerConfigured {
                            showProgressBarDuringRemoteSave.value = true
                        }
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.archiveAFolder(
                                selectedFolderForMenuBtmSheet.value, onCompletion = {
                                    showProgressBarDuringRemoteSave.value = false
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        menuBtmModalSheetVisible.value = false
                                    }

                                })
                        } else {
                            collectionsScreenVM.archiveALink(
                                selectedLinkForMenuBtmSheet.value, onCompletion = {
                                    showProgressBarDuringRemoteSave.value = false
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        menuBtmModalSheetVisible.value = false
                                    }

                                })
                        }
                    },
                    onDeleteNote = {
                        initializeIfServerConfigured {
                            showProgressBarDuringRemoteSave.value = true
                        }
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.deleteTheNote(
                                selectedFolderForMenuBtmSheet.value, onCompletion = {
                                    showProgressBarDuringRemoteSave.value = false
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        menuBtmModalSheetVisible.value = false
                                    }
                                })
                        } else {
                            collectionsScreenVM.deleteTheNote(
                                selectedLinkForMenuBtmSheet.value, onCompletion = {
                                    showProgressBarDuringRemoteSave.value = false
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        menuBtmModalSheetVisible.value = false
                                    }
                                })
                        }
                    },
                    onRefreshClick = {
                        initializeIfServerConfigured {
                            showProgressBarDuringRemoteSave.value = true
                        }
                        collectionsScreenVM.refreshLinkMetadata(
                            selectedLinkForMenuBtmSheet.value, onCompletion = {
                                showProgressBarDuringRemoteSave.value = false
                                coroutineScope.launch {
                                    menuBtmModalSheetState.hide()
                                }.invokeOnCompletion {
                                    menuBtmModalSheetVisible.value = false
                                }
                            })
                    },
                    onForceLaunchInAnExternalBrowser = {
                        localUriHandler.openUri(selectedLinkForMenuBtmSheet.value.url)
                    },
                    showQuickActions = rememberSaveable { mutableStateOf(false) },
                    shouldTransferringOptionShouldBeVisible = true,
                    link = selectedLinkForMenuBtmSheet,
                    folder = selectedFolderForMenuBtmSheet,
                    onAddToImportantLinks = {
                        initializeIfServerConfigured {
                            showProgressBarDuringRemoteSave.value = true
                        }
                        collectionsScreenVM.markALinkAsImp(
                            selectedLinkForMenuBtmSheet.value, onCompletion = {
                                showProgressBarDuringRemoteSave.value = false
                                coroutineScope.launch {
                                    menuBtmModalSheetState.hide()
                                }.invokeOnCompletion {
                                    menuBtmModalSheetVisible.value = false
                                }
                            })
                    },
                    shouldShowArchiveOption = {
                        selectedLinkForMenuBtmSheet.value.linkType == LinkType.ARCHIVE_LINK
                    },
                    showProgressBarDuringRemoteSave = showProgressBarDuringRemoteSave
                )
            )
            DeleteDialogBox(
                DeleteDialogBoxParam(
                    showDeleteDialogBox,
                    if (CollectionsScreenVM.isSelectionEnabled.value) DeleteDialogBoxType.SELECTED_DATA else if (menuBtmSheetFolderEntries().contains(
                            menuBtmSheetFor.value
                        )
                    ) {
                        DeleteDialogBoxType.FOLDER
                    } else DeleteDialogBoxType.LINK,
                    onDeleteClick = { onCompletion, _ ->
                        if (CollectionsScreenVM.isSelectionEnabled.value) {
                            appVM.deleteSelectedItems(onStart = {}, onCompletion)
                            return@DeleteDialogBoxParam
                        }
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.deleteAFolder(
                                selectedFolderForMenuBtmSheet.value, onCompletion = {
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        menuBtmModalSheetVisible.value = false
                                    }

                                    onCompletion()
                                })
                        } else {
                            collectionsScreenVM.deleteALink(
                                selectedLinkForMenuBtmSheet.value, onCompletion = {
                                    coroutineScope.launch {
                                        menuBtmModalSheetState.hide()
                                    }.invokeOnCompletion {
                                        menuBtmModalSheetVisible.value = false
                                    }

                                    onCompletion()
                                })
                        }
                    })
            )
            RenameDialogBox(
                RenameDialogBoxParam(
                    onNoteChangeClick = { newNote, onCompletion ->
                        if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) {
                            collectionsScreenVM.updateFolderNote(
                                selectedFolderForMenuBtmSheet.value.localId,
                                newNote = newNote,
                                onCompletion = {
                                    onCompletion()
                                    showRenameDialogBox.value = false
                                })
                        } else {
                            collectionsScreenVM.updateLinkNote(
                                selectedLinkForMenuBtmSheet.value.localId,
                                newNote = newNote,
                                onCompletion = {
                                    onCompletion()
                                    showRenameDialogBox.value = false
                                })
                        }
                    },
                    shouldDialogBoxAppear = showRenameDialogBox,
                    existingFolderName = selectedFolderForMenuBtmSheet.value.name,
                    onBothTitleAndNoteChangeClick = { title, note, onCompletion ->
                        if (menuBtmSheetFor.value in menuBtmSheetFolderEntries()) {
                            collectionsScreenVM.updateFolderNote(
                                selectedFolderForMenuBtmSheet.value.localId,
                                newNote = note,
                                pushSnackbarOnSuccess = false,
                                onCompletion = {
                                    onCompletion()
                                    showRenameDialogBox.value = false
                                })
                            collectionsScreenVM.updateFolderName(
                                folder = selectedFolderForMenuBtmSheet.value,
                                newName = title,
                                ignoreFolderAlreadyExistsThrowable = true,
                                onCompletion = {
                                    onCompletion()

                                    showRenameDialogBox.value = false
                                })
                        } else {
                            collectionsScreenVM.updateLinkNote(
                                linkId = selectedLinkForMenuBtmSheet.value.localId,
                                newNote = note,
                                pushSnackbarOnSuccess = false,
                                onCompletion = {
                                    onCompletion()
                                    showRenameDialogBox.value = false
                                })
                            collectionsScreenVM.updateLinkTitle(
                                linkId = selectedLinkForMenuBtmSheet.value.localId,
                                newTitle = title,
                                onCompletion = {
                                    onCompletion()

                                    showRenameDialogBox.value = false
                                })
                        }
                        coroutineScope.launch {
                            menuBtmModalSheetState.hide()
                        }.invokeOnCompletion {
                            menuBtmModalSheetVisible.value = false
                        }
                    },
                    existingTitle = if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) selectedFolderForMenuBtmSheet.value.name else selectedLinkForMenuBtmSheet.value.title,
                    existingNote = if (menuBtmSheetFolderEntries().contains(menuBtmSheetFor.value)) selectedFolderForMenuBtmSheet.value.note else selectedLinkForMenuBtmSheet.value.note
                )
            )

            SortingBottomSheetUI(
                SortingBottomSheetParam(
                    shouldBottomSheetBeVisible = sortingBottomSheetVisible,
                    onSelected = { sortingPreferences, _, _ -> },
                    bottomModalSheetState = sortingBtmSheetState,
                    sortingBtmSheetType = SortingBtmSheetType.COLLECTIONS_SCREEN,
                    shouldFoldersSelectionBeVisible = rememberSaveable {
                        mutableStateOf(false)
                    },
                    shouldLinksSelectionBeVisible = rememberSaveable {
                        mutableStateOf(false)
                    })
            )
        }
    }
}