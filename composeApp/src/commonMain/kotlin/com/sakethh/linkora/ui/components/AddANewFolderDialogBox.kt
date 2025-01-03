package com.sakethh.linkora.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.isNotNull
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.ui.domain.model.AddNewFolderDialogBoxParam
import com.sakethh.linkora.ui.utils.pulsateEffect

@Composable
fun AddANewFolderDialogBox(addNewFolderDialogBoxParam: AddNewFolderDialogBoxParam) {
    val scrollState = rememberScrollState()
    val isFolderCreationInProgress = rememberSaveable {
        mutableStateOf(false)
    }
    val folderName: MutableState<String?> =
        rememberSaveable(addNewFolderDialogBoxParam.thisFolder?.name) {
            mutableStateOf(addNewFolderDialogBoxParam.thisFolder?.name)
        }
    if (addNewFolderDialogBoxParam.shouldBeVisible.value) {
        val folderNameTextFieldValue = rememberSaveable {
            mutableStateOf("")
        }
        val noteTextFieldValue = rememberSaveable {
            mutableStateOf("")
        }
        AlertDialog(
            dismissButton = {
                if (!isFolderCreationInProgress.value) {
                    androidx.compose.material3.OutlinedButton(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pulsateEffect(),
                        onClick = {
                            addNewFolderDialogBoxParam.shouldBeVisible.value = false
                        }) {
                        Text(
                            text = Localization.rememberLocalizedString(Localization.Key.Cancel),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            confirmButton = {
                if (!isFolderCreationInProgress.value) {
                    Button(
                        modifier = Modifier
                            .fillMaxWidth()
                            .pulsateEffect(), onClick = {
                            isFolderCreationInProgress.value = true
                            addNewFolderDialogBoxParam.onFolderCreateClick(
                                folderNameTextFieldValue.value, noteTextFieldValue.value, {
                                    addNewFolderDialogBoxParam.shouldBeVisible.value = false
                                    isFolderCreationInProgress.value = false
                                })
                        }) {
                        Text(
                            text = Localization.rememberLocalizedString(Localization.Key.Create),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                }
            },
            modifier = Modifier
                .animateContentSize()
                .wrapContentHeight(),
            onDismissRequest = {
                if (!isFolderCreationInProgress.value) {
                    addNewFolderDialogBoxParam.shouldBeVisible.value = false
                }
            },
            text = {
                Column(modifier = Modifier.verticalScroll(scrollState)) {
                    OutlinedTextField(
                        readOnly = isFolderCreationInProgress.value,
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 1,
                        label = {
                            Text(
                                text = Localization.rememberLocalizedString(Localization.Key.FolderName),
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.titleSmall,
                        singleLine = true,
                        value = folderNameTextFieldValue.value,
                        onValueChange = {
                            folderNameTextFieldValue.value = it
                        })
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        readOnly = isFolderCreationInProgress.value,
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = Localization.rememberLocalizedString(Localization.Key.NoteForCreatingTheFolder),
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 12.sp
                            )
                        },
                        textStyle = MaterialTheme.typography.titleSmall,
                        value = noteTextFieldValue.value,
                        onValueChange = {
                            noteTextFieldValue.value = it
                        })
                    if (isFolderCreationInProgress.value) {
                        Spacer(modifier = Modifier.height(40.dp))
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                }
            },
            title = {
                Text(
                    text = if (addNewFolderDialogBoxParam.inAChildFolderScreen && addNewFolderDialogBoxParam.thisFolder.isNotNull())
                        Localization.rememberLocalizedString(Localization.Key.CreateANewFolderIn)
                            .replace(
                                LinkoraPlaceHolder.First.value,
                                "\"${addNewFolderDialogBoxParam.thisFolder!!.name}\""
                            )
                    else
                        Localization.rememberLocalizedString(Localization.Key.CreateANewFolder),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    lineHeight = 28.sp
                )
            })
    }
}