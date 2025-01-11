package com.sakethh.linkora.ui.components

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
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.common.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.ui.utils.pulsateEffect

data class DeleteAShelfDialogBoxParam(
    val isDialogBoxVisible: MutableState<Boolean>, val onDeleteClick: () -> Unit,
    val panelName: String
)

@Composable
fun DeleteAShelfPanelDialogBox(deleteAShelfDialogBoxParam: DeleteAShelfDialogBoxParam) {
    if (deleteAShelfDialogBoxParam.isDialogBoxVisible.value) {
        AlertDialog(confirmButton = {
            Button(
                modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                    deleteAShelfDialogBoxParam.onDeleteClick()
                    deleteAShelfDialogBoxParam.isDialogBoxVisible.value = false
                }) {
                Text(
                    text = Localization.Key.PermanentlyDeleteThePanel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
        }, dismissButton = {
            OutlinedButton(
                modifier = Modifier.fillMaxWidth().pulsateEffect(), onClick = {
                    deleteAShelfDialogBoxParam.isDialogBoxVisible.value = false
                }) {
                Text(
                    text = Localization.Key.Cancel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
        }, title = {
            Text(
                text = Localization.Key.AreYouSureWantToDeleteThePanel.rememberLocalizedString()
                    .replaceFirstPlaceHolderWith(deleteAShelfDialogBoxParam.panelName),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp,
                lineHeight = 28.sp,
                textAlign = TextAlign.Start
            )
        }, text = {
            Text(
                text = Localization.Key.OnceDeletedThisPanelCannotBeRestored.rememberLocalizedString(),
                style = MaterialTheme.typography.titleSmall,
                fontSize = 14.sp,
                lineHeight = 18.sp,
                textAlign = TextAlign.Start
            )
        }, onDismissRequest = {
            deleteAShelfDialogBoxParam.isDialogBoxVisible.value = false
        })
    }
}