package com.sakethh.linkora.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.ui.utils.pulsateEffect

enum class DeleteDialogBoxType {
    LINK, FOLDER, REMOVE_ENTIRE_DATA, SELECTED_DATA
}

data class DeleteDialogBoxParam(
    val shouldDialogBoxAppear: MutableState<Boolean>,
    val deleteDialogBoxType: DeleteDialogBoxType,
    val onDeleteClick: (onCompletion: () -> Unit) -> Unit,
    val areFoldersSelectable: Boolean = false
)

@Composable
fun DeleteDialogBox(
    deleteDialogBoxParam: DeleteDialogBoxParam
) {
    val isDeletionInProgress: MutableState<Boolean> = rememberSaveable {
        mutableStateOf(false)
    }
    Column {
        if (deleteDialogBoxParam.shouldDialogBoxAppear.value) {
            AlertDialog(modifier = Modifier.animateContentSize(), confirmButton = {
                if (isDeletionInProgress.value.not()) {
                    Button(
                        modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                            isDeletionInProgress.value = true
                            deleteDialogBoxParam.onDeleteClick({
                                isDeletionInProgress.value = false
                                deleteDialogBoxParam.shouldDialogBoxAppear.value = false
                            })
                        }) {
                        Text(
                            text = Localization.rememberLocalizedString(Localization.Key.Delete),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                }
            }, dismissButton = {
                if (isDeletionInProgress.value.not()) {
                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                            deleteDialogBoxParam.shouldDialogBoxAppear.value = false
                        }) {
                        Text(
                            text = Localization.rememberLocalizedString(Localization.Key.Cancel),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                }
            }, title = {
                Text(
                    text = if (isDeletionInProgress.value) Localization.Key.DeletionInProgress.rememberLocalizedString() else deleteDialogBoxParam.deleteDialogBoxType.getTitle(
                        areFoldersSelectable = deleteDialogBoxParam.areFoldersSelectable
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    lineHeight = 27.sp,
                    textAlign = TextAlign.Start
                )
            }, text = {
                if (isDeletionInProgress.value.not() && deleteDialogBoxParam.deleteDialogBoxType == DeleteDialogBoxType.FOLDER) {
                    Text(
                        text = Localization.Key.FolderDeletionLabel.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 14.sp,
                        lineHeight = 18.sp,
                        textAlign = TextAlign.Start,
                        overflow = TextOverflow.Ellipsis
                    )
                } else if (isDeletionInProgress.value) {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
            }, onDismissRequest = {
                if (isDeletionInProgress.value.not()) {
                    deleteDialogBoxParam.shouldDialogBoxAppear.value = false
                }
            })
        }
    }
}

private fun DeleteDialogBoxType.getTitle(areFoldersSelectable: Boolean): String {
    return if (this == DeleteDialogBoxType.LINK && areFoldersSelectable) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteSelectedLinks
    )
    else if (this == DeleteDialogBoxType.LINK) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteLink
    )
    else if (this == DeleteDialogBoxType.FOLDER && areFoldersSelectable) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteSelectedFolders
    )
    else if (this == DeleteDialogBoxType.FOLDER) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteFolder
    )
    else if (this == DeleteDialogBoxType.SELECTED_DATA) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteSelectedItems
    )
    else Localization.getLocalizedString(Localization.Key.AreYouSureDeleteEverything)
}