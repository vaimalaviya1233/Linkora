package com.sakethh.linkora.customComposables

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.btmSheet.OptionsBtmSheetType
import com.sakethh.linkora.localDB.CustomFunctionsForLocalDB
import com.sakethh.linkora.ui.theme.LinkoraTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RenameDialogBox(
    shouldDialogBoxAppear: MutableState<Boolean>,
    coroutineScope: CoroutineScope, existingFolderName: String?,
    webURLForTitle: String? = null,
    renameDialogBoxFor: OptionsBtmSheetType = OptionsBtmSheetType.FOLDER,
    onNoteChangeClickForLinks: ((webURL: String, newNote: String) -> Unit?)?,
    onTitleChangeClickForLinks: ((webURL: String, newTitle: String) -> Unit?)?,
    inChildArchiveFolderScreen: MutableState<Boolean> = mutableStateOf(false),
    onTitleRenamed: () -> Unit = {},
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var doesFolderNameAlreadyExists = false
    val localContext = LocalContext.current
    val customFunctionsForLocalDB: CustomFunctionsForLocalDB = viewModel()
    if (shouldDialogBoxAppear.value) {
        val newFolderOrTitleName = rememberSaveable {
            mutableStateOf("")
        }
        val newNote = rememberSaveable {
            mutableStateOf("")
        }
        LinkoraTheme {
            AlertDialog(modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
                .background(AlertDialogDefaults.containerColor),
                onDismissRequest = { shouldDialogBoxAppear.value = false }) {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    Text(
                        text = if (renameDialogBoxFor != OptionsBtmSheetType.LINK) "Rename \"$existingFolderName\" folder:" else "Change Link's Data:",
                        color = AlertDialogDefaults.titleContentColor,
                        style = MaterialTheme.typography.titleMedium,
                        fontSize = 22.sp,
                        modifier = Modifier.padding(start = 20.dp, top = 30.dp, end = 25.dp),
                        lineHeight = 27.sp,
                        textAlign = TextAlign.Start
                    )
                    OutlinedTextField(maxLines = 1,
                        modifier = Modifier.padding(
                            start = 20.dp, end = 20.dp, top = 30.dp
                        ),
                        label = {
                            Text(
                                text = if (renameDialogBoxFor == OptionsBtmSheetType.FOLDER) "New Name" else "New title",
                                color = AlertDialogDefaults.textContentColor,
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.titleSmall,
                        singleLine = true,
                        shape = RoundedCornerShape(5.dp),
                        value = newFolderOrTitleName.value,
                        onValueChange = {
                            newFolderOrTitleName.value = it
                        })
                    OutlinedTextField(maxLines = 1,
                        modifier = Modifier.padding(
                            start = 20.dp, end = 20.dp, top = 15.dp
                        ),
                        label = {
                            Text(
                                text = "New note",
                                color = AlertDialogDefaults.textContentColor,
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.titleSmall,
                        singleLine = true,
                        shape = RoundedCornerShape(5.dp),
                        value = newNote.value,
                        onValueChange = {
                            newNote.value = it
                        })
                    Text(
                        text = "Leave above field empty, if you don't want to change the note.",
                        color = AlertDialogDefaults.textContentColor,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(start = 20.dp, top = 10.dp, end = 20.dp),
                        lineHeight = 16.sp
                    )
                    Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .padding(
                                end = 20.dp,
                                top = 20.dp,
                            )
                            .align(Alignment.End),
                        onClick = {
                            if (newFolderOrTitleName.value.isEmpty()) {
                                Toast.makeText(
                                    localContext,
                                    if (renameDialogBoxFor == OptionsBtmSheetType.FOLDER) "Folder name can't be empty" else "title can't be empty",
                                    Toast.LENGTH_SHORT
                                ).show()
                            } else {
                                if (renameDialogBoxFor == OptionsBtmSheetType.LINK || inChildArchiveFolderScreen.value) {
                                    if (onTitleChangeClickForLinks != null && webURLForTitle != null) {
                                        onTitleChangeClickForLinks(
                                            webURLForTitle, newFolderOrTitleName.value
                                        )
                                    }
                                    if (onNoteChangeClickForLinks != null && webURLForTitle != null) {
                                        if (newNote.value.isNotEmpty()) {
                                            onNoteChangeClickForLinks(
                                                if (inChildArchiveFolderScreen.value) newFolderOrTitleName.value else webURLForTitle,
                                                newNote.value
                                            )
                                        }
                                    }
                                    Toast.makeText(
                                        context,
                                        "renamed link's data successfully",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    onTitleRenamed()
                                    shouldDialogBoxAppear.value = false
                                } else {
                                    if (newFolderOrTitleName.value.isEmpty()) {
                                        Toast.makeText(
                                            localContext,
                                            if (renameDialogBoxFor == OptionsBtmSheetType.FOLDER) "Folder name can't be empty" else "title can't be empty",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        if (renameDialogBoxFor == OptionsBtmSheetType.FOLDER) {
                                            coroutineScope.launch {
                                                doesFolderNameAlreadyExists =
                                                    CustomFunctionsForLocalDB.localDB.crudDao()
                                                        .doesThisFolderExists(
                                                            newFolderOrTitleName.value
                                                        )
                                            }.invokeOnCompletion {
                                                if (doesFolderNameAlreadyExists) {
                                                    Toast.makeText(
                                                        localContext,
                                                        "Folder name already exists",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                } else {
                                                    if (existingFolderName != null) {
                                                        customFunctionsForLocalDB.updateFoldersDetails(
                                                            existingFolderName = existingFolderName,
                                                            newFolderName = newFolderOrTitleName.value,
                                                            infoForFolder = newNote.value,
                                                            context = localContext,
                                                            onTaskCompleted = {
                                                                onTitleRenamed()
                                                                shouldDialogBoxAppear.value = false
                                                            }
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }) {
                        Text(
                            text = if (renameDialogBoxFor == OptionsBtmSheetType.FOLDER) "Change folder data" else "Change title data",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                    Button(colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                        modifier = Modifier
                            .padding(
                                end = 20.dp,
                                top = 10.dp,
                            )
                            .align(Alignment.End),
                        onClick = {
                            if (renameDialogBoxFor == OptionsBtmSheetType.LINK || inChildArchiveFolderScreen.value) {
                                if (onNoteChangeClickForLinks != null && webURLForTitle != null) {
                                    onNoteChangeClickForLinks(
                                        webURLForTitle, newNote.value
                                    )
                                }
                                shouldDialogBoxAppear.value = false
                            } else {
                                if (newNote.value.isEmpty()) {
                                    Toast.makeText(
                                        localContext, "note can't be empty", Toast.LENGTH_SHORT
                                    ).show()
                                } else {
                                    coroutineScope.launch {
                                        if (existingFolderName != null) {
                                            CustomFunctionsForLocalDB.localDB.crudDao()
                                                .renameAFolderNote(
                                                    folderName = existingFolderName,
                                                    newNote = newNote.value
                                                )
                                        }
                                    }
                                    shouldDialogBoxAppear.value = false
                                }
                            }
                        }) {
                        Text(
                            text = "Change note only",
                            color = MaterialTheme.colorScheme.onPrimary,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                    OutlinedButton(colors = ButtonDefaults.outlinedButtonColors(),
                        border = BorderStroke(
                            width = 1.dp, color = MaterialTheme.colorScheme.secondary
                        ),
                        modifier = Modifier
                            .padding(
                                end = 20.dp, top = 10.dp, bottom = 30.dp
                            )
                            .align(Alignment.End),
                        onClick = {
                            shouldDialogBoxAppear.value = false
                        }) {
                        Text(
                            text = "Cancel",
                            color = MaterialTheme.colorScheme.secondary,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    }
}