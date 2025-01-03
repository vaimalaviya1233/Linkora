package com.sakethh.linkora.ui.components.menu

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.CopyAll
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderCopy
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material.icons.outlined.VerticalAlignTop
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.ui.components.CoilImage
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.fadedEdges
import com.sakethh.linkora.ui.utils.pulsateEffect
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MenuBtmSheetUI(
    menuBtmSheetParam: MenuBtmSheetParam
) {
    val coroutineScope = rememberCoroutineScope()
    val mutableStateNote =
        rememberSaveable(inputs = arrayOf(menuBtmSheetParam.noteForSaving)) {
            mutableStateOf(menuBtmSheetParam.noteForSaving)
        }
    val isNoteBtnSelected = rememberSaveable {
        mutableStateOf(false)
    }
    val localClipBoardManager = LocalClipboardManager.current
    val isImageAssociatedWithTheLinkIsExpanded = rememberSaveable {
        mutableStateOf(false)
    }
    if (menuBtmSheetParam.shouldBtmModalSheetBeVisible.value) {
        ModalBottomSheet(onDismissRequest = {
            coroutineScope.launch {
                menuBtmSheetParam.btmModalSheetState.hide()
            }.invokeOnCompletion {
                menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
            }
        }, sheetState = menuBtmSheetParam.btmModalSheetState) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {

                if (menuBtmSheetParam.imgLink.isNotEmpty() && AppPreferences.showAssociatedImagesInLinkMenu.value && (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.IMPORTANT_LINKS_SCREEN || menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.LINK)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 15.dp)
                            .wrapContentHeight()
                    ) {
                        CoilImage(
                            modifier = Modifier
                                .animateContentSize()
                                .fillMaxWidth()
                                .then(
                                    if (isImageAssociatedWithTheLinkIsExpanded.value) Modifier.wrapContentHeight() else Modifier.heightIn(
                                        max = 150.dp
                                    )
                                )
                                .fadedEdges(MaterialTheme.colorScheme),
                            imgURL = menuBtmSheetParam.imgLink,
                            userAgent = menuBtmSheetParam.imgUserAgent
                        )
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .align(Alignment.BottomStart)
                                .padding(end = 15.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilledTonalIconButton(
                                onClick = {
                                    isImageAssociatedWithTheLinkIsExpanded.value =
                                        !isImageAssociatedWithTheLinkIsExpanded.value
                                }, modifier = Modifier
                                    .alpha(0.75f)
                                    .padding(5.dp)
                            ) {
                                Icon(
                                    imageVector = if (!isImageAssociatedWithTheLinkIsExpanded.value) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                    contentDescription = ""
                                )
                            }
                            Text(
                                text = menuBtmSheetParam.linkTitle,
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 16.sp,
                                maxLines = 2,
                                lineHeight = 20.sp,
                                textAlign = TextAlign.Start,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 25.dp, end = 25.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(0.25f)
                    )
                } else {
                    Row(
                        modifier = Modifier
                            .combinedClickable(
                                interactionSource = remember {
                                    MutableInteractionSource()
                                }, indication = null,
                                onClick = {
                                    localClipBoardManager.setText(AnnotatedString(if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) menuBtmSheetParam.folderName else menuBtmSheetParam.linkTitle))
                                    coroutineScope.pushUIEvent(
                                        UIEvent.Type.ShowSnackbar(
                                            Localization.Key.CopiedTitleToTheClipboard.getLocalizedString()
                                        )
                                    )
                                })
                            .pulsateEffect()
                            .fillMaxWidth(), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Icons.Outlined.Folder else Icons.Outlined.Link,
                            null,
                            modifier = Modifier
                                .padding(20.dp)
                                .size(28.dp)
                        )

                        Text(
                            text = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) menuBtmSheetParam.folderName else menuBtmSheetParam.linkTitle,
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(
                                end = 20.dp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            lineHeight = 20.sp
                        )
                    }
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 25.dp, end = 25.dp),
                        thickness = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(0.25f)
                    )
                }
                if (!isNoteBtnSelected.value) {
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
                    // TODO  if (TransferActions.currentTransferActionType.value != TransferActionType.NOTHING) return@Column
                    IndividualMenuComponent(
                        onOptionClick = {
                            coroutineScope.launch {
                                if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                    menuBtmSheetParam.btmModalSheetState.hide()
                                }
                            }.invokeOnCompletion {
                                menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                            }
                            menuBtmSheetParam.onRenameClick()
                        },
                        elementName = Localization.Key.Rename.getLocalizedString(),
                        elementImageVector = Icons.Outlined.DriveFileRenameOutline
                    )
                    if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.LINK || menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.IMPORTANT_LINKS_SCREEN) {
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

                    if (menuBtmSheetParam.shouldImportantLinkOptionBeVisible.value && (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.LINK || menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.IMPORTANT_LINKS_SCREEN) && !menuBtmSheetParam.inArchiveScreen.value) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onImportantLinkClick?.let { it() }
                                coroutineScope.launch {
                                    if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                        menuBtmSheetParam.btmModalSheetState.hide()
                                    }
                                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value =
                                        false
                                }
                            },
                            elementName = "Archive",
                            elementImageVector = Icons.Default.Favorite
                        )
                    }
                    if (!menuBtmSheetParam.inSpecificArchiveScreen.value/* && optionsBtmSheetVM.archiveCardIcon.value != Icons.Outlined.Unarchive*/ && !menuBtmSheetParam.inArchiveScreen.value) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onArchiveClick()
                                coroutineScope.launch {
                                    if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                        menuBtmSheetParam.btmModalSheetState.hide()
                                    }
                                }.invokeOnCompletion {
                                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value =
                                        false
                                }
                            },
                            elementName = "Archive",
                            elementImageVector = Icons.Default.Archive
                        )
                    }
                    if (menuBtmSheetParam.inArchiveScreen.value && !menuBtmSheetParam.inSpecificArchiveScreen.value) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onUnarchiveClick()
                                coroutineScope.launch {
                                    if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                        menuBtmSheetParam.btmModalSheetState.hide()
                                    }
                                }.invokeOnCompletion {
                                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value =
                                        false
                                }
                            },
                            elementName = Localization.Key.UnArchive.rememberLocalizedString(),
                            elementImageVector = Icons.Outlined.Unarchive
                        )
                    }
                    if (mutableStateNote.value.isNotEmpty()) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onNoteDeleteCardClick()
                                coroutineScope.launch {
                                    if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                        menuBtmSheetParam.btmModalSheetState.hide()
                                    }
                                }.invokeOnCompletion {
                                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value =
                                        false
                                }
                            },
                            elementName = Localization.Key.DeleteTheNote.rememberLocalizedString(),
                            elementImageVector = Icons.Outlined.Delete
                        )
                    }
                    if (menuBtmSheetParam.forAChildFolder.value && menuBtmSheetParam.shouldTransferringOptionShouldBeVisible && menuBtmSheetParam.btmSheetFor != MenuBtmSheetType.LINK) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onMoveToRootFoldersClick()
                            },
                            elementName = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Localization.Key.MoveToRootFolders.rememberLocalizedString() else Localization.Key.DeleteTheLink.rememberLocalizedString(),
                            elementImageVector = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Icons.Outlined.VerticalAlignTop else Icons.Outlined.DeleteForever
                        )
                    }
                    if (/*TransferActions.currentTransferActionType.value == TransferActionType.NOTHING &&*/ menuBtmSheetParam.shouldTransferringOptionShouldBeVisible) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onCopyItemClick()
                            },
                            elementName = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Localization.Key.CopyFolder.rememberLocalizedString() else Localization.Key.CopyLink.rememberLocalizedString(),
                            elementImageVector = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Icons.Outlined.FolderCopy else Icons.Outlined.CopyAll
                        )

                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onMoveItemClick()
                            },
                            elementName = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Localization.Key.MoveToOtherFolder.rememberLocalizedString() else Localization.Key.MoveLink.rememberLocalizedString(),
                            elementImageVector = Icons.AutoMirrored.Outlined.DriveFileMove
                        )
                    }
                    if (menuBtmSheetParam.inSpecificArchiveScreen.value || menuBtmSheetParam.btmSheetFor != MenuBtmSheetType.IMPORTANT_LINKS_SCREEN) {
                        IndividualMenuComponent(
                            onOptionClick = {
                                menuBtmSheetParam.onDeleteCardClick()
                                coroutineScope.launch {
                                    if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                                        menuBtmSheetParam.btmModalSheetState.hide()
                                    }
                                }.invokeOnCompletion {
                                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value =
                                        false
                                }
                            },
                            elementName = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Localization.Key.DeleteTheFolder.rememberLocalizedString() else Localization.Key.DeleteTheLink.rememberLocalizedString(),
                            elementImageVector = if (menuBtmSheetParam.btmSheetFor == MenuBtmSheetType.FOLDER) Icons.Outlined.FolderDelete else Icons.Outlined.DeleteForever
                        )
                    }
                } else {
                    if (mutableStateNote.value.isNotEmpty()) {
                        Text(
                            text = Localization.Key.SavedNote.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontSize = 16.sp,
                            modifier = Modifier
                                .padding(20.dp)
                        )
                        Text(
                            text = mutableStateNote.value,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 20.sp,
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(onClick = {}, onLongClick = {
                                    localClipBoardManager.setText(AnnotatedString(mutableStateNote.value))
                                    coroutineScope.pushUIEvent(
                                        UIEvent.Type.ShowSnackbar(
                                            Localization.Key.CopiedNoteToTheClipboard.getLocalizedString()
                                        )
                                    )
                                })
                                .padding(
                                    start = 20.dp, end = 25.dp
                                ),
                            textAlign = TextAlign.Start,
                            lineHeight = 24.sp
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                    } else {
                        Spacer(modifier = Modifier.height(20.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = Localization.Key.NoNoteAdded.rememberLocalizedString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                lineHeight = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
                if (menuBtmSheetParam.showQuickActions.value) {
                    QuickActions(
                        onForceOpenInExternalBrowserClicked = menuBtmSheetParam.onForceOpenInExternalBrowserClicked,
                        webUrl = menuBtmSheetParam.webUrl
                    )
                }

            }
        }
    }
}



