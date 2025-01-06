package com.sakethh.linkora.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.ui.utils.pulsateEffect

enum class DataDialogBoxType {
    LINK, FOLDER, REMOVE_ENTIRE_DATA, SELECTED_DATA
}

data class DeleteDialogBoxParam(
    val shouldDialogBoxAppear: MutableState<Boolean>,
    val deleteDialogBoxType: DataDialogBoxType,
    val onDeleteClick: () -> Unit,
    val areFoldersSelectable: Boolean = false
)

@Composable
fun DeleteDialogBox(
    deleteDialogBoxParam: DeleteDialogBoxParam
) {
    Column {
        if (deleteDialogBoxParam.shouldDialogBoxAppear.value) {
            AlertDialog(confirmButton = {
                Button(
                    modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                        deleteDialogBoxParam.onDeleteClick()
                        deleteDialogBoxParam.shouldDialogBoxAppear.value = false
                    }) {
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.Delete),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp
                    )
                }
            }, dismissButton = {
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
            }, title = {
                Text(
                    text = deleteDialogBoxParam.deleteDialogBoxType.getTitle(areFoldersSelectable = deleteDialogBoxParam.areFoldersSelectable),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    lineHeight = 27.sp,
                    textAlign = TextAlign.Start
                )
            }, text = {
                Text(
                    text = Localization.Key.FolderDeletionLabel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start,
                    overflow = TextOverflow.Ellipsis
                )
            }, onDismissRequest = {
                deleteDialogBoxParam.shouldDialogBoxAppear.value = false
            })
        }
    }
}

private fun DataDialogBoxType.getTitle(areFoldersSelectable: Boolean): String {
    return if (this == DataDialogBoxType.LINK && areFoldersSelectable) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteSelectedLinks
    )
    else if (this == DataDialogBoxType.LINK) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteLink
    )
    else if (this == DataDialogBoxType.FOLDER && areFoldersSelectable) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteSelectedFolders
    )
    else if (this == DataDialogBoxType.FOLDER) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteFolder
    )
    else if (this == DataDialogBoxType.SELECTED_DATA) Localization.getLocalizedString(
        Localization.Key.AreYouSureDeleteSelectedItems
    )
    else Localization.getLocalizedString(Localization.Key.AreYouSureDeleteAllFoldersAndLinks)
}