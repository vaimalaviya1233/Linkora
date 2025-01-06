package com.sakethh.linkora.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.common.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.ui.components.menu.MenuItemType
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.platform

data class RenameDialogBoxParam(
    val shouldDialogBoxAppear: MutableState<Boolean>,
    val renameDialogBoxFor: MenuItemType = MenuItemType.FOLDER,
    val onNoteChangeClick: ((newNote: String) -> Unit),
    val onBothTitleAndNoteChangeClick: ((newTitle: String, newNote: String) -> Unit),
    val existingFolderName: String?,
    val existingTitle: String,
    val existingNote: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialogBox(
    renameDialogBoxParam: RenameDialogBoxParam
) {
    if (renameDialogBoxParam.shouldDialogBoxAppear.value) {
        val newFolderOrTitleName = rememberSaveable(renameDialogBoxParam.existingTitle) {
            mutableStateOf(renameDialogBoxParam.existingTitle)
        }
        val newNote = rememberSaveable(renameDialogBoxParam.existingNote) {
            mutableStateOf(renameDialogBoxParam.existingNote)
        }
        BasicAlertDialog(
            modifier = Modifier.then(
                if (platform() == Platform.Android.Mobile) Modifier.fillMaxSize() else Modifier.wrapContentSize()
            ).clip(RoundedCornerShape(10.dp)).background(AlertDialogDefaults.containerColor),
            properties = DialogProperties(usePlatformDefaultWidth = false),
            onDismissRequest = { renameDialogBoxParam.shouldDialogBoxAppear.value = false }) {
            LazyColumn(
                Modifier.then(
                    if (platform() == Platform.Android.Mobile) Modifier.fillMaxSize() else Modifier.wrapContentSize()
                ).padding(15.dp), verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                item {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (renameDialogBoxParam.renameDialogBoxFor == MenuItemType.FOLDER && renameDialogBoxParam.existingFolderName?.isNotBlank() == true) Localization.Key.RenameFolder.rememberLocalizedString()
                                .replaceFirstPlaceHolderWith(renameDialogBoxParam.existingFolderName) else Localization.Key.ChangeLinkData.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 22.sp,
                            lineHeight = 27.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxWidth(if (platform() != Platform.Android.Mobile) 0.85f else 1f)
                        )

                        if (platform() != Platform.Android.Mobile) {
                            IconButton(
                                onClick = {
                                    renameDialogBoxParam.shouldDialogBoxAppear.value = false
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Close, contentDescription = null
                                )
                            }
                        }
                    }
                }
                item {
                    OutlinedTextField(
                        label = {
                            Text(
                                text = if (renameDialogBoxParam.renameDialogBoxFor == MenuItemType.FOLDER) Localization.Key.NewName.rememberLocalizedString()
                                else Localization.Key.NewTitle.rememberLocalizedString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.titleSmall,
                        value = newFolderOrTitleName.value,
                        onValueChange = {
                            newFolderOrTitleName.value = it
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                item {
                    OutlinedTextField(
                        label = {
                            Text(
                                text = Localization.Key.NewNote.rememberLocalizedString(),
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.titleSmall,
                        value = newNote.value,
                        onValueChange = {
                            newNote.value = it
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                item {
                    Button(
                        modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                            renameDialogBoxParam.onNoteChangeClick(newNote.value)
                        }) {
                        Text(
                            text = Localization.Key.ChangeNoteOnly.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Button(
                        modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                            renameDialogBoxParam.onBothTitleAndNoteChangeClick(
                                newFolderOrTitleName.value,
                                newNote.value
                            )
                        }) {
                        Text(
                            text = Localization.rememberLocalizedString(Localization.Key.ChangeBothNameAndNote),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                            renameDialogBoxParam.shouldDialogBoxAppear.value = false
                        }) {
                        Text(
                            text = Localization.Key.Cancel.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}