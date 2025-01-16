package com.sakethh.linkora.ui.components.menu

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderDelete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.OpenInBrowser
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.unit.dp
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.bottomNavPaddingAcrossPlatforms
import com.sakethh.linkora.common.utils.fillMaxWidthWithPadding
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.ComposableContent
import com.sakethh.linkora.ui.domain.Layout
import com.sakethh.onShare
import com.sakethh.platform
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MenuBtmSheetUI(
    menuBtmSheetParam: MenuBtmSheetParam
) {
    val coroutineScope = rememberCoroutineScope()
    if (menuBtmSheetParam.shouldBtmModalSheetBeVisible.value) {
        val isNoteBtnSelected = rememberSaveable {
            mutableStateOf(false)
        }
        val platform = platform()
        val localUriHandler = LocalUriHandler.current
        val localClipboard = LocalClipboardManager.current
        val commonContent: ComposableContent = {
            Column {
                if (platform is Platform.Android.Mobile) {
                    IndividualMenuComponent(
                        onOptionClick = {
                            coroutineScope.launch {
                                if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                    menuBtmSheetParam.btmModalSheetState.hide()
                                }
                            }.invokeOnCompletion {
                                isNoteBtnSelected.value = true
                                coroutineScope.launch {
                                    menuBtmSheetParam.btmModalSheetState.show()
                                }
                            }
                        },
                        elementName = Localization.Key.ViewNote.rememberLocalizedString(),
                        elementImageVector = Icons.AutoMirrored.Outlined.TextSnippet
                    )
                }
                // TODO  if (TransferActions.currentTransferActionType.value != TransferActionType.NOTHING) return@Column
                if (platform is Platform.Android.Mobile || menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetFolderEntries()) {
                    IndividualMenuComponent(
                        onOptionClick = {
                            coroutineScope.launch {
                                if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                    menuBtmSheetParam.btmModalSheetState.hide()
                                }
                            }.invokeOnCompletion {
                                menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                            }
                            menuBtmSheetParam.onRename()
                        },
                        elementName = Localization.Key.Rename.getLocalizedString(),
                        elementImageVector = Icons.Outlined.DriveFileRenameOutline
                    )

                    if (menuBtmSheetLinkEntries().contains(menuBtmSheetParam.menuBtmSheetFor)) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onRefreshClick()
                                coroutineScope.launch {
                                    if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                        menuBtmSheetParam.btmModalSheetState.hide()
                                    }
                                }.invokeOnCompletion {
                                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                                }
                            },
                            elementName = Localization.Key.RefreshImageAndTitle.rememberLocalizedString(),
                            elementImageVector = Icons.Outlined.Refresh
                        )
                    }
                }

                if (menuBtmSheetLinkEntries().contains(menuBtmSheetParam.menuBtmSheetFor)) {
                    IndividualMenuComponent(
                        onOptionClick = {
                            menuBtmSheetParam.onAddToImportantLinks?.let { it() }
                            coroutineScope.launch {
                                if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                    menuBtmSheetParam.btmModalSheetState.hide()
                                }
                                menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                            }
                        },
                        elementName = MenuBtmSheetVM.importantOptionText.value,
                        elementImageVector = MenuBtmSheetVM.importantOptionIcon.value
                    )
                }

                IndividualMenuComponent(
                    onOptionClick = {
                        menuBtmSheetParam.onArchive()
                        coroutineScope.launch {
                            if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                menuBtmSheetParam.btmModalSheetState.hide()
                            }
                        }.invokeOnCompletion {
                            menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                        }
                    },
                    elementName = MenuBtmSheetVM.archiveOptionText.value,
                    elementImageVector = MenuBtmSheetVM.archiveOptionIcon.value
                )

                if (menuBtmSheetLinkEntries().contains(menuBtmSheetParam.menuBtmSheetFor) && menuBtmSheetParam.link!!.value.note.isNotBlank() || menuBtmSheetFolderEntries().contains(
                        menuBtmSheetParam.menuBtmSheetFor
                    ) && menuBtmSheetParam.folder!!.value.note.isNotBlank()
                ) {
                    IndividualMenuComponent(
                        onOptionClick = {
                            menuBtmSheetParam.onDeleteNote()
                            coroutineScope.launch {
                                if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                    menuBtmSheetParam.btmModalSheetState.hide()
                                }
                            }.invokeOnCompletion {
                                menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                            }
                        },
                        elementName = Localization.Key.DeleteTheNote.rememberLocalizedString(),
                        elementImageVector = Icons.Outlined.Delete
                    )
                }/*if (menuBtmSheetParam.forAChildFolder.value && menuBtmSheetParam.shouldTransferringOptionShouldBeVisible && menuBtmSheetParam.menuBtmSheetFor != MenuBtmSheetType.LINK) {
                            IndividualMenuComponent(
                                onOptionClick = {
                                    menuBtmSheetParam.onMoveToRootFoldersClick()
                                },
                                elementName = if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.RegularFolder) Localization.Key.MoveToRootFolders.rememberLocalizedString() else Localization.Key.DeleteTheLink.rememberLocalizedString(),
                                elementImageVector = if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.RegularFolder) Icons.Outlined.VerticalAlignTop else Icons.Outlined.DeleteForever
                            )
                        }*//*if (*//*TransferActions.currentTransferActionType.value == TransferActionType.NOTHING &&*//* menuBtmSheetParam.shouldTransferringOptionShouldBeVisible) {
                            IndividualMenuComponent(
                                onOptionClick = {
                                    menuBtmSheetParam.onCopy()
                                },
                                elementName = if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.RegularFolder) Localization.Key.CopyFolder.rememberLocalizedString() else Localization.Key.CopyLink.rememberLocalizedString(),
                                elementImageVector = if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.RegularFolder) Icons.Outlined.FolderCopy else Icons.Outlined.CopyAll
                            )

                            IndividualMenuComponent(
                                onOptionClick = {
                                    menuBtmSheetParam.onMove()
                                },
                                elementName = if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.RegularFolder) Localization.Key.MoveToOtherFolder.rememberLocalizedString() else Localization.Key.MoveLink.rememberLocalizedString(),
                                elementImageVector = Icons.AutoMirrored.Outlined.DriveFileMove
                            )
                        }*/
                if (menuBtmSheetParam.menuBtmSheetFor != MenuBtmSheetType.Link.ImportantLink) {
                    IndividualMenuComponent(
                        onOptionClick = {
                            menuBtmSheetParam.onDelete()
                            coroutineScope.launch {
                                if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                    menuBtmSheetParam.btmModalSheetState.hide()
                                }
                            }.invokeOnCompletion {
                                menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                            }
                        },
                        elementName = if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.Folder.RegularFolder) Localization.Key.DeleteTheFolder.rememberLocalizedString() else Localization.Key.DeleteTheLink.rememberLocalizedString(),
                        elementImageVector = if (menuBtmSheetParam.menuBtmSheetFor == MenuBtmSheetType.Folder.RegularFolder) Icons.Outlined.FolderDelete else Icons.Outlined.DeleteForever
                    )
                }
                if (platform !is Platform.Android.Mobile && menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, top = 0.dp, bottom = 5.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.25f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NavigationBarItem(selected = true, onClick = {
                            if (menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()) {
                                localClipboard.setText(
                                    AnnotatedString(
                                        text = menuBtmSheetParam.link?.value?.url ?: ""
                                    )
                                )
                                coroutineScope.launch {
                                    menuBtmSheetParam.btmModalSheetState.hide()
                                }.invokeOnCompletion {
                                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                                }
                            } else {
                                menuBtmSheetParam.onArchive()
                            }
                        }, icon = {
                            Icon(
                                imageVector = if (menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()) Icons.Default.ContentCopy else MenuBtmSheetVM.archiveOptionIcon.value,
                                contentDescription = null
                            )
                        }, label = {
                            Text(
                                text = if (menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()) {
                                    Localization.Key.CopyLink.rememberLocalizedString()
                                } else {
                                    MenuBtmSheetVM.archiveOptionText.value
                                }, style = MaterialTheme.typography.titleSmall
                            )
                        })
                        NavigationBarItem(selected = true, onClick = {
                            menuBtmSheetParam.onRename()
                        }, icon = {
                            Icon(
                                imageVector = Icons.Outlined.DriveFileRenameOutline,
                                contentDescription = null
                            )
                        }, label = {
                            Text(
                                text = Localization.Key.Rename.rememberLocalizedString(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        })
                        NavigationBarItem(selected = true, onClick = {
                            if (menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()) {
                                menuBtmSheetParam.onRefreshClick()
                            } else {
                                menuBtmSheetParam.onDelete()
                            }
                        }, icon = {
                            Icon(
                                imageVector = if (menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()) Icons.Outlined.Refresh else Icons.Default.FolderDelete,
                                contentDescription = null
                            )
                        }, label = {
                            Text(
                                text = if (menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()) Localization.Key.Refresh.rememberLocalizedString() else Localization.Key.Delete.rememberLocalizedString(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        })
                    }
                    if (platform is Platform.Android && AppPreferences.currentlySelectedLinkLayout.value in listOf(
                            Layout.STAGGERED_VIEW.name, Layout.GRID_VIEW.name
                        ) && menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()
                    ) {
                        FilledTonalButton(
                            onClick = {
                                onShare(url = menuBtmSheetParam.link!!.value.url)
                            },
                            modifier = Modifier.fillMaxWidthWithPadding()
                                .bottomNavPaddingAcrossPlatforms()
                        ) {
                            Icon(imageVector = Icons.Default.Share, contentDescription = null)
                            Spacer(Modifier.width(5.dp))
                            Text(
                                text = Localization.Key.Share.rememberLocalizedString(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                } else if (platform == Platform.Android.Mobile && AppPreferences.currentlySelectedLinkLayout.value in listOf(
                        Layout.STAGGERED_VIEW.name, Layout.GRID_VIEW.name
                    ) && menuBtmSheetParam.menuBtmSheetFor in menuBtmSheetLinkEntries()
                ) {
                    HorizontalDivider(
                        modifier = Modifier.fillMaxWidth()
                            .padding(start = 15.dp, end = 15.dp, top = 0.dp, bottom = 5.dp),
                        color = MaterialTheme.colorScheme.outline.copy(0.25f)
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        NavigationBarItem(selected = true, onClick = {
                            localClipboard.setText(
                                AnnotatedString(
                                    text = menuBtmSheetParam.link?.value?.url ?: ""
                                )
                            )
                            coroutineScope.launch {
                                menuBtmSheetParam.btmModalSheetState.hide()
                            }.invokeOnCompletion {
                                menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                            }
                        }, icon = {
                            Icon(
                                imageVector = Icons.Default.ContentCopy, contentDescription = null
                            )
                        })
                        NavigationBarItem(selected = true, onClick = {
                            menuBtmSheetParam.onForceLaunchInAnExternalBrowser()
                        }, icon = {
                            Icon(
                                imageVector = Icons.Outlined.OpenInBrowser,
                                contentDescription = null
                            )
                        })
                        NavigationBarItem(selected = true, onClick = {
                            onShare(url = menuBtmSheetParam.link!!.value.url)
                        }, icon = {
                            Icon(
                                imageVector = Icons.Default.Share, contentDescription = null
                            )
                        })
                    }
                }
            }
        }
        ModalBottomSheet(
            onDismissRequest = {
                coroutineScope.launch {
                    menuBtmSheetParam.btmModalSheetState.hide()
                }.invokeOnCompletion {
                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                }
            }, dragHandle = {
                if (platform !is Platform.Android.Mobile) {
                    BottomSheetDefaults.DragHandle()
                }
            }, sheetState = menuBtmSheetParam.btmModalSheetState
        ) {
            if (platform is Platform.Android.Mobile) {
                MobileMenu(menuBtmSheetParam, isNoteBtnSelected, commonContent)
            } else {
                NonMobileMenu(menuBtmSheetParam, commonContent)
            }
        }
    }
}



