package com.sakethh.linkora.customComposables

import android.util.Log
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.ui.theme.LinkoraTheme

data class DeleteAShelfDialogBoxDTO(
    val isDialogBoxVisible: MutableState<Boolean>,
    val onDeleteClick: () -> Unit,
    val selectedShelfName: String,
    val selectedShelfIcon: ImageVector,
)

@Composable
fun DeleteAShelfDialogBox(deleteAShelfDialogBoxDTO: DeleteAShelfDialogBoxDTO) {
    if (deleteAShelfDialogBoxDTO.isDialogBoxVisible.value) {
        val title = rememberSaveable(deleteAShelfDialogBoxDTO.selectedShelfName) {
            mutableStateOf("Are you sure you want to delete ${deleteAShelfDialogBoxDTO.selectedShelfName}?")
        }
        LinkoraTheme {
            AlertDialog(modifier = Modifier.clickable {
                Log.d("LINKORA_TAG", deleteAShelfDialogBoxDTO.selectedShelfName)
            }, confirmButton = {
                Button(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pulsateEffect(),
                    onClick = {
                        deleteAShelfDialogBoxDTO.onDeleteClick()
                        deleteAShelfDialogBoxDTO.isDialogBoxVisible.value = false
                    }) {
                    Text(
                        text = "Delete it",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp
                    )
                }
            }, dismissButton = {
                OutlinedButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .pulsateEffect(),
                    onClick = {
                        deleteAShelfDialogBoxDTO.isDialogBoxVisible.value = false
                    }) {
                    Text(
                        text = "Cancel",
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp
                    )
                }
            }, title = {
                Text(
                    text = title.value,
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 22.sp,
                    lineHeight = 27.sp,
                    textAlign = TextAlign.Start
                )
            }, text = {
                Text(
                    text = "This deletion can't be undone.",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.sp,
                    lineHeight = 18.sp,
                    textAlign = TextAlign.Start
                )
            },
                onDismissRequest = {
                    deleteAShelfDialogBoxDTO.isDialogBoxVisible.value = false
                })
        }
    }
    Column {
    }
}