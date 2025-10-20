package com.sakethh.linkora.ui.screens.collections.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.Localization
import com.sakethh.linkora.ui.utils.pressScaleEffect
import com.sakethh.linkora.utils.getLocalizedString

@Composable
fun TagDeletionConfirmation(
    showConfirmation: Boolean, onHide: () -> Unit, onDelete: () -> Unit
) {
    if (showConfirmation) {
        AlertDialog(onDismissRequest = onHide, confirmButton = {
            Button(onClick = onDelete, modifier = Modifier.pressScaleEffect().pointerHoverIcon(icon = PointerIcon.Hand).fillMaxWidth()) {
                Text(
                    text = Localization.Key.Delete.getLocalizedString(),
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }, dismissButton = {
            OutlinedButton(onClick = onHide, modifier = Modifier.pressScaleEffect().pointerHoverIcon(icon = PointerIcon.Hand).fillMaxWidth()) {
                Text(
                    text = Localization.Key.Cancel.getLocalizedString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }, icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                modifier = Modifier.size(48.dp),
                contentDescription = null
            )
        }, title = {
            Text(
                text = "Delete for sure?",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 24.sp
            )
        })
    }
}