package com.sakethh.linkora.ui.screens.collections

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Sort
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DatasetLinked
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FabPosition
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.AddItemFABParam
import com.sakethh.linkora.ui.components.AddItemFab
import com.sakethh.linkora.ui.components.folder.FolderComponent
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetVM
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.domain.model.FolderComponentParam
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.platform
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    collectionsScreenVM: CollectionsScreenVM, menuBtmSheetVM: MenuBtmSheetVM,
    shouldShowNewFolderDialog: MutableState<Boolean>,
    shouldShowAddLinkDialog: MutableState<Boolean>
) {
    val rootFolders = collectionsScreenVM.rootRegularFolders.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
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
    val navController = LocalNavController.current
    val platform = platform()
    Scaffold(
        floatingActionButton = {
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
        },
        floatingActionButtonPosition = FabPosition.End,
        modifier = Modifier.background(MaterialTheme.colorScheme.surface),
        topBar = {
            Column {
                TopAppBar(actions = {}, navigationIcon = {}, title = {
                    Text(
                        text = Navigation.Root.CollectionsScreen.toString(),
                        color = MaterialTheme.colorScheme.onSurface,
                        style = MaterialTheme.typography.titleLarge,
                        fontSize = 18.sp
                    )
                })
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.25f))
            }
        }) { padding ->
        Row(modifier = Modifier.padding(padding).fillMaxSize()) {
            LazyColumn(
                modifier = Modifier.fillMaxHeight()
                    .fillMaxWidth(if (platform() is Platform.Android.Mobile) 1f else 0.4f)
            ) {
                item {
                    DefaultFolderComponent(
                        name = Localization.rememberLocalizedString(Localization.Key.AllLinks),
                        icon = Icons.Outlined.DatasetLinked,
                        onClick = {
                            collectionsScreenVM.updateCollectionDetailPaneInfo(
                                CollectionDetailPaneInfo(
                                    currentFolder = Folder(
                                        name = Localization.Key.AllLinks.getLocalizedString(),
                                        note = "",
                                        parentFolderId = null,
                                        localId = Constants.ALL_LINKS_ID
                                    ),
                                    isAnyCollectionSelected = true
                                )
                            )
                            if (platform is Platform.Android.Mobile) navController.navigate(
                                Navigation.Collection.CollectionDetailPane
                            )
                        },
                        isSelected = collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.ALL_LINKS_ID
                    )
                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            top = 15.dp,
                            start = 25.dp,
                            end = if (platform() is Platform.Android.Mobile) 25.dp else 5.dp
                        ),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(0.25f)
                    )
                }
                item {
                    DefaultFolderComponent(
                        name = Localization.rememberLocalizedString(Localization.Key.SavedLinks),
                        icon = Icons.Outlined.Link,
                        onClick = { ->
                            collectionsScreenVM.updateCollectionDetailPaneInfo(
                                CollectionDetailPaneInfo(
                                    currentFolder = Folder(
                                        name = Localization.Key.SavedLinks.getLocalizedString(),
                                        note = "",
                                        parentFolderId = null,
                                        localId = Constants.SAVED_LINKS_ID
                                    ),
                                    isAnyCollectionSelected = true
                                )
                            )
                            if (platform is Platform.Android.Mobile) navController.navigate(
                                Navigation.Collection.CollectionDetailPane
                            )
                        },
                        isSelected = collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.SAVED_LINKS_ID
                    )
                }
                item {
                    DefaultFolderComponent(
                        name = Localization.rememberLocalizedString(Localization.Key.ImportantLinks),
                        icon = Icons.Outlined.StarOutline,
                        onClick = { ->
                            collectionsScreenVM.updateCollectionDetailPaneInfo(
                                CollectionDetailPaneInfo(
                                    currentFolder = Folder(
                                        name = Localization.Key.ImportantLinks.getLocalizedString(),
                                        note = "",
                                        parentFolderId = null,
                                        localId = Constants.IMPORTANT_LINKS_ID
                                    ),
                                    isAnyCollectionSelected = true
                                )
                            )
                            if (platform is Platform.Android.Mobile) navController.navigate(
                                Navigation.Collection.CollectionDetailPane
                            )
                        },
                        isSelected = collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.IMPORTANT_LINKS_ID
                    )
                }
                item {
                    DefaultFolderComponent(
                        name = Localization.rememberLocalizedString(Localization.Key.Archive),
                        icon = Icons.Outlined.Archive,
                        onClick = { ->
                            collectionsScreenVM.updateCollectionDetailPaneInfo(
                                CollectionDetailPaneInfo(
                                    currentFolder = Folder(
                                        name = Localization.Key.Archive.getLocalizedString(),
                                        note = "",
                                        parentFolderId = null,
                                        localId = Constants.ARCHIVE_ID
                                    ),
                                    isAnyCollectionSelected = true
                                )
                            )
                            if (platform is Platform.Android.Mobile) navController.navigate(
                                Navigation.Collection.CollectionDetailPane
                            )
                        },
                        isSelected = collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == Constants.ARCHIVE_ID
                    )

                }
                item {
                    HorizontalDivider(
                        modifier = Modifier.padding(
                            start = 20.dp,
                            top = 15.dp,
                            bottom = 10.dp,
                            end = if (platform() is Platform.Android.Mobile) 20.dp else 0.dp
                        ),
                        thickness = 0.5.dp,
                        color = MaterialTheme.colorScheme.outline.copy(0.25f)
                    )
                }
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = Localization.rememberLocalizedString(Localization.Key.Folders),
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(start = 15.dp)
                        )
                        IconButton(modifier = Modifier.pulsateEffect(), onClick = {
                            coroutineScope.pushUIEvent(UIEvent.Type.ShowSortingBtmSheetUI)
                        }) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Outlined.Sort,
                                contentDescription = null
                            )
                        }
                    }
                }
                items(rootFolders.value) { folder ->
                    FolderComponent(
                        FolderComponentParam(
                            folder = folder,
                            onClick = { ->
                                collectionsScreenVM.updateCollectionDetailPaneInfo(
                                    CollectionDetailPaneInfo(
                                        currentFolder = folder,
                                        isAnyCollectionSelected = true
                                    )
                                )
                                if (platform is Platform.Android.Mobile) navController.navigate(
                                    Navigation.Collection.CollectionDetailPane
                                )
                            },
                            onLongClick = { -> },
                            onMoreIconClick = { ->
                                coroutineScope.pushUIEvent(
                                    UIEvent.Type.ShowMenuBtmSheetUI(
                                        menuBtmSheetFor = MenuBtmSheetType.Folder.RegularFolder,
                                        selectedLinkForMenuBtmSheet = null,
                                        selectedFolderForMenuBtmSheet = folder
                                    )
                                )
                            },
                            isCurrentlyInDetailsView = remember(collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId) {
                                mutableStateOf(collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == folder.localId)
                            },
                            showMoreIcon = rememberSaveable {
                                mutableStateOf(true)
                            })
                    )
                }
                item {
                    Spacer(Modifier.height(150.dp))
                }
            }
            if (platform() is Platform.Android.Mobile) return@Row
            VerticalDivider(modifier = Modifier.padding(start = 20.dp))
            if (collectionsScreenVM.collectionDetailPaneInfo.value.isAnyCollectionSelected.not() || collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder.isNull()) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = Localization.Key.SelectACollection.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            } else {
                CollectionDetailPane(
                    collectionsScreenVM = collectionsScreenVM,
                    menuBtmSheetVM = menuBtmSheetVM,
                    currentlyInFolder = collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder!!,
                )
            }
        }
        if (shouldScreenTransparencyDecreasedBoxVisible.value) {
            Box(
                modifier = Modifier.fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(0.95f)).clickable {
                        shouldScreenTransparencyDecreasedBoxVisible.value = false
                        coroutineScope.launch {
                            awaitAll(async {
                                rotationAnimation.animateTo(
                                    -360f, animationSpec = tween(300)
                                )
                            }, async { isMainFabRotated.value = false })
                        }.invokeOnCompletion {
                            coroutineScope.launch {
                                rotationAnimation.snapTo(0f)
                            }
                        }
                    })
        }
    }
}


@Composable
private fun DefaultFolderComponent(
    name: String, icon: ImageVector, onClick: () -> Unit, isSelected: Boolean
) {
    Card(
        modifier = Modifier.padding(
            end = if (platform() == Platform.Android.Mobile) 20.dp else 0.dp,
            start = 20.dp,
            top = 15.dp
        ).wrapContentHeight().fillMaxWidth().clickable(interactionSource = remember {
            MutableInteractionSource()
        }, indication = null, onClick = {
            onClick()
        }).pulsateEffect().then(
            if (isSelected) Modifier.border(
                width = 2.5.dp,
                color = MaterialTheme.colorScheme.primary,
                shape = CardDefaults.shape
            ) else Modifier
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                modifier = Modifier.padding(20.dp), imageVector = icon, contentDescription = null
            )
            Text(
                text = name, style = MaterialTheme.typography.titleSmall, fontSize = 16.sp
            )
        }
    }
}