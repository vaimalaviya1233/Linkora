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
import com.sakethh.linkora.domain.asMenuBtmSheetType
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.components.folder.FolderComponent
import com.sakethh.linkora.ui.components.link.LinkListItemComposable
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetVM
import com.sakethh.linkora.ui.domain.Layout
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.domain.model.FolderComponentParam
import com.sakethh.linkora.ui.domain.model.LinkUIComponentParam
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailPane(
    currentlyInFolder: Folder,
    collectionsScreenVM: CollectionsScreenVM,
    btmModalSheetState: SheetState,
    selectedFolderForMenuBtmSheet: MutableState<Folder>,
    shouldMenuBtmModalSheetBeVisible: MutableState<Boolean>,
    menuBtmSheetFor: MutableState<MenuBtmSheetType>,
    selectedLinkForMenuBtmSheet: MutableState<Link>,
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
                        collectionsScreenVM.updateCollectionDetailPaneInfo(
                            CollectionDetailPaneInfo(
                                currentFolder = collectionsScreenVM.getFolder(currentlyInFolder.parentFolderId),
                                isAnyCollectionSelected = true
                            )
                        )
                    } else {
                        collectionsScreenVM.updateCollectionDetailPaneInfo(
                            CollectionDetailPaneInfo(
                                currentFolder = null,
                                isAnyCollectionSelected = false
                            )
                        )
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
                            collectionsScreenVM.updateCollectionDetailPaneInfo(
                                CollectionDetailPaneInfo(
                                    currentFolder = childFolder,
                                    isAnyCollectionSelected = true
                                )
                            )
                        },
                        onLongClick = { -> },
                        onMoreIconClick = { ->
                            menuBtmSheetFor.value = MenuBtmSheetType.Folder.RegularFolder
                            selectedFolderForMenuBtmSheet.value = childFolder
                            shouldMenuBtmModalSheetBeVisible.value = true
                            coroutineScope.launch {
                                btmModalSheetState.show()
                            }
                        },
                        isCurrentlyInDetailsView = remember(collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId) {
                            mutableStateOf(collectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId == childFolder.localId)
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
                            menuBtmSheetFor.value = it.linkType.asMenuBtmSheetType()
                            selectedLinkForMenuBtmSheet.value = it
                            menuBtmSheetVM.updateImpLinkInfo(selectedLinkForMenuBtmSheet.value.url)
                            menuBtmSheetVM.updateArchiveLinkInfo(selectedLinkForMenuBtmSheet.value.url)
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