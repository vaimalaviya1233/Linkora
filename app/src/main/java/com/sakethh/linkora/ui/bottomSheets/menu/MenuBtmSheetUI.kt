package com.sakethh.linkora.ui.bottomSheets.menu

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.TextSnippet
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DriveFileRenameOutline
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.DriveFileRenameOutline
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.FolderDelete
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.sakethh.linkora.ui.commonComposables.pulsateEffect
import com.sakethh.linkora.ui.commonComposables.viewmodels.commonBtmSheets.OptionsBtmSheetType
import com.sakethh.linkora.ui.commonComposables.viewmodels.commonBtmSheets.OptionsBtmSheetVM
import com.sakethh.linkora.ui.screens.collections.FolderIndividualComponent
import kotlinx.coroutines.launch


@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun MenuBtmSheetUI(
    menuBtmSheetParam: MenuBtmSheetParam
) {
    val context = LocalContext.current
    val mutableStateNote =
        rememberSaveable(inputs = arrayOf(menuBtmSheetParam.noteForSaving)) {
            mutableStateOf(menuBtmSheetParam.noteForSaving)
        }
    val isNoteBtnSelected = rememberSaveable {
        mutableStateOf(false)
    }
    val coroutineScope = rememberCoroutineScope()
    val optionsBtmSheetVM: OptionsBtmSheetVM = hiltViewModel()
    val localClipBoardManager = LocalClipboardManager.current
    if (menuBtmSheetParam.shouldBtmModalSheetBeVisible.value) {
        ModalBottomSheet(
            dragHandle = {},
            sheetState = menuBtmSheetParam.btmModalSheetState,
            onDismissRequest = {
                coroutineScope.launch {
                    if (menuBtmSheetParam.btmModalSheetState.isVisible) {
                        menuBtmSheetParam.btmModalSheetState.hide()
                    }
                }.invokeOnCompletion {
                    menuBtmSheetParam.shouldBtmModalSheetBeVisible.value = false
                    isNoteBtnSelected.value = false
                }
            }) {
            Column(
                modifier = Modifier
                    .navigationBarsPadding()
                    .verticalScroll(rememberScrollState())
            ) {
                FolderIndividualComponent(
                    folderName = if (menuBtmSheetParam.btmSheetFor == OptionsBtmSheetType.FOLDER) menuBtmSheetParam.folderName else menuBtmSheetParam.linkTitle,
                    folderNote = "",
                    onMoreIconClick = {
                        localClipBoardManager.setText(AnnotatedString(if (menuBtmSheetParam.btmSheetFor == OptionsBtmSheetType.FOLDER) menuBtmSheetParam.folderName else menuBtmSheetParam.linkTitle))
                        Toast.makeText(context, "Title copied to clipboard", Toast.LENGTH_SHORT)
                            .show()
                    },
                    onFolderClick = { },
                    maxLines = 2,
                    showMoreIcon = false,
                    folderIcon = if (menuBtmSheetParam.btmSheetFor == OptionsBtmSheetType.FOLDER) Icons.Outlined.Folder else Icons.Outlined.Link
                )
                Spacer(modifier = Modifier.height(5.dp))
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
                        elementName = "View Note",
                        elementImageVector = Icons.AutoMirrored.Outlined.TextSnippet
                    )
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
                        elementName = "Rename",
                        elementImageVector = Icons.Outlined.DriveFileRenameOutline
                    )

                    if ((menuBtmSheetParam.btmSheetFor == OptionsBtmSheetType.LINK || menuBtmSheetParam.btmSheetFor == OptionsBtmSheetType.IMPORTANT_LINKS_SCREEN) && !menuBtmSheetParam.inArchiveScreen.value) {
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
                            elementName = optionsBtmSheetVM.importantCardText.value,
                            elementImageVector = optionsBtmSheetVM.importantCardIcon.value
                        )
                    }
                    if (!menuBtmSheetParam.inSpecificArchiveScreen.value && optionsBtmSheetVM.archiveCardIcon.value != Icons.Outlined.Unarchive && !menuBtmSheetParam.inArchiveScreen.value) {
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
                            elementName = optionsBtmSheetVM.archiveCardText.value,
                            elementImageVector = optionsBtmSheetVM.archiveCardIcon.value
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
                            elementName = "Unarchive",
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
                            elementName = "Delete the note",
                            elementImageVector = Icons.Outlined.Delete
                        )
                    }
                    if (menuBtmSheetParam.inSpecificArchiveScreen.value || menuBtmSheetParam.btmSheetFor != OptionsBtmSheetType.IMPORTANT_LINKS_SCREEN) {
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
                            elementName = if (menuBtmSheetParam.btmSheetFor == OptionsBtmSheetType.FOLDER) "Delete Folder" else "Delete Link",
                            elementImageVector = if (menuBtmSheetParam.btmSheetFor == OptionsBtmSheetType.FOLDER) Icons.Outlined.FolderDelete else Icons.Outlined.DeleteForever
                        )
                    }
                } else {
                    if (mutableStateNote.value.isNotEmpty()) {
                        Text(
                            text = "Saved note :",
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
                                    Toast
                                        .makeText(
                                            context,
                                            "Note copied to clipboard",
                                            Toast.LENGTH_SHORT
                                        )
                                        .show()
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
                                text = "You didn't add a note for this.",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 15.sp,
                                textAlign = TextAlign.Start,
                                lineHeight = 24.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(20.dp))
                    }
                }
            }
        }
        BackHandler {
            coroutineScope.launch {
                menuBtmSheetParam.btmModalSheetState.hide()
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun IndividualMenuComponent(
    onOptionClick: () -> Unit,
    elementName: String,
    elementImageVector: ImageVector,
    inShelfUI: Boolean = false,
    onDeleteIconClick: () -> Unit = {},
    onTuneIconClick: () -> Unit = {},
    onRenameIconClick: () -> Unit = {},
) {
    Row(
        modifier = Modifier
            .combinedClickable(interactionSource = remember {
                MutableInteractionSource()
            }, indication = null,
                onClick = {
                    onOptionClick()
                },
                onLongClick = {

                })
            .pulsateEffect()
            .padding(end = 10.dp)
            .wrapContentHeight()
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                modifier = Modifier.padding(10.dp), onClick = { onOptionClick() },
                colors = IconButtonDefaults.filledTonalIconButtonColors()
            ) {
                Icon(imageVector = elementImageVector, contentDescription = null)
            }
            Text(
                text = elementName,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 16.sp,
                modifier = Modifier.fillMaxWidth(if (inShelfUI) 0.4f else 1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        if (inShelfUI) {
            Row {
                IconButton(onClick = { onRenameIconClick() }
                ) {
                    Icon(
                        imageVector = Icons.Default.DriveFileRenameOutline,
                        contentDescription = null
                    )
                }
                IconButton(onClick = { onTuneIconClick() }
                ) {
                    Icon(imageVector = Icons.Default.Tune, contentDescription = null)
                }
                IconButton(onClick = { onDeleteIconClick() }
                ) {
                    Icon(imageVector = Icons.Default.Delete, contentDescription = null)
                }
            }
        }
    }
}