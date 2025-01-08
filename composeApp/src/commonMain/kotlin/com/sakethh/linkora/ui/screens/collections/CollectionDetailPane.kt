package com.sakethh.linkora.ui.screens.collections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.adaptive.ExperimentalMaterial3AdaptiveApi
import androidx.compose.material3.adaptive.layout.ListDetailPaneScaffoldRole
import androidx.compose.material3.adaptive.navigation.ThreePaneScaffoldNavigator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.isNotNull
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.components.folder.FolderComponent
import com.sakethh.linkora.ui.components.link.LinkListItemComposable
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetVM
import com.sakethh.linkora.ui.components.menu.MenuItemType
import com.sakethh.linkora.ui.domain.Layout
import com.sakethh.linkora.ui.domain.model.FolderComponentParam
import com.sakethh.linkora.ui.domain.model.LinkUIComponentParam
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3AdaptiveApi::class)
@Composable
fun CollectionDetailPane(
    currentlyInFolder: Folder,
    paneNavigator: ThreePaneScaffoldNavigator<Folder>,
    collectionsScreenVM: CollectionsScreenVM,
    btmModalSheetState: SheetState,
    selectedFolder: MutableState<Folder>,
    shouldMenuBtmModalSheetBeVisible: MutableState<Boolean>,
    menuBtmSheetFor: MutableState<MenuItemType>,
    selectedLink: MutableState<Link>,
    menuBtmSheetVM: MenuBtmSheetVM
) {
    val links = collectionsScreenVM.links.collectAsStateWithLifecycle()
    val childFolders = collectionsScreenVM.childFolders.collectAsStateWithLifecycle()
    val coroutineScope = rememberCoroutineScope()
    Scaffold(modifier = Modifier.fillMaxSize(), topBar = {
        Column {
            TopAppBar(actions = {}, navigationIcon = {
                IconButton(onClick = {
                    if (currentlyInFolder.parentFolderId.isNotNull()) {
                        currentlyInFolder.parentFolderId as Long
                        paneNavigator.navigateTo(
                            ListDetailPaneScaffoldRole.Detail,
                            content = collectionsScreenVM.getFolder(currentlyInFolder.parentFolderId)
                        )
                        collectionsScreenVM.updateCollectableLinks(
                            linkType = LinkType.FOLDER_LINK,
                            folderId = currentlyInFolder.parentFolderId
                        )
                        collectionsScreenVM.updateCollectableChildFolders(
                            parentFolderId = currentlyInFolder.parentFolderId
                        )
                    } else {
                        paneNavigator.navigateBack()
                    }
                }) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                }
            }, title = {
                Text(
                    text = currentlyInFolder.name,
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp
                )
            })
            HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(0.25f))
        }
    }) { padding ->
        LazyColumn(Modifier.padding(padding).fillMaxSize()) {
            items(childFolders.value) { childFolder ->
                FolderComponent(
                    FolderComponentParam(
                        folder = childFolder,
                        onClick = { ->
                            collectionsScreenVM.updateCollectableLinks(
                                linkType = LinkType.FOLDER_LINK,
                                folderId = childFolder.id
                            )
                            collectionsScreenVM.updateCollectableChildFolders(
                                parentFolderId = childFolder.id
                            )
                            paneNavigator.navigateTo(
                                ListDetailPaneScaffoldRole.Detail, childFolder
                            )
                        },
                        onLongClick = { -> },
                        onMoreIconClick = { ->
                            menuBtmSheetFor.value = MenuItemType.FOLDER
                            selectedFolder.value = childFolder
                            shouldMenuBtmModalSheetBeVisible.value = true
                            coroutineScope.launch {
                                btmModalSheetState.show()
                            }
                        },
                        isCurrentlyInDetailsView = remember(paneNavigator.currentDestination?.content?.id) {
                            mutableStateOf(paneNavigator.currentDestination?.content?.id == childFolder.id)
                        },
                        showMoreIcon = rememberSaveable {
                            mutableStateOf(true)
                        })
                )
            }
            items(links.value) {
                LinkListItemComposable(
                    linkUIComponentParam = LinkUIComponentParam(
                        link = it,
                        isSelectionModeEnabled = mutableStateOf(false),
                        onMoreIconClick = {
                            menuBtmSheetFor.value = MenuItemType.LINK
                            selectedLink.value = it
                            menuBtmSheetVM.updateImpLinkInfo(selectedLink.value.id)
                            shouldMenuBtmModalSheetBeVisible.value = true
                            coroutineScope.launch {
                                btmModalSheetState.show()
                            }
                        },
                        onLinkClick = {

                        },
                        onForceOpenInExternalBrowserClicked = {

                        },
                        isItemSelected = mutableStateOf(false),
                        onLongClick = {

                        }
                    ),
                    forTitleOnlyView = AppPreferences.currentlySelectedLinkLayout.value == Layout.TITLE_ONLY_LIST_VIEW.name)
            }
        }
    }
}