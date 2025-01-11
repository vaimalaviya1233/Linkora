package com.sakethh.linkora.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.ui.utils.pulsateEffect


data class AddANewShelfParam(
    val isDialogBoxVisible: MutableState<Boolean>,
    val onCreateClick: (shelfName: String) -> Unit,
)

@Composable
fun AddANewPanelDialogBox(addANewShelfParam: AddANewShelfParam) {
    if (addANewShelfParam.isDialogBoxVisible.value) {
        val customShelfName = rememberSaveable {
            mutableStateOf("")
        }

        AlertDialog(title = {
            Text(
                text = Localization.Key.AddANewPanel.rememberLocalizedString(),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 22.sp,
                lineHeight = 28.sp
            )
        }, onDismissRequest = {
            addANewShelfParam.isDialogBoxVisible.value = false
        }, text = {
            Column {
                OutlinedTextField(
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 1,
                    label = {
                        Text(
                            text = Localization.Key.PanelName.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 12.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.titleSmall,
                    singleLine = true,
                    value = customShelfName.value,
                    onValueChange = {
                        customShelfName.value = it
                    })
            }
        }, confirmButton = {
            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .pulsateEffect(), onClick = {
                    addANewShelfParam.onCreateClick(
                        customShelfName.value,
                    )
                    addANewShelfParam.isDialogBoxVisible.value = false
                }) {
                Text(
                    text = Localization.Key.AddANewPanel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
        }, dismissButton = {
            androidx.compose.material3.OutlinedButton(
                modifier = Modifier
                    .fillMaxWidth()
                    .pulsateEffect(),
                onClick = {
                    addANewShelfParam.isDialogBoxVisible.value = false
                }) {
                Text(
                    text = Localization.Key.Cancel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
        })
    }
}