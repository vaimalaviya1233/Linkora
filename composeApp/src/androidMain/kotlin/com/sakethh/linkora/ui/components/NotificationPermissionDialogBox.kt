package com.sakethh.linkora.ui.components

import android.os.Build
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.rememberLocalizedString

@Composable
fun NotificationPermissionDialogBox(
    isVisible: MutableState<Boolean>,
    notificationRuntimePermission: ManagedActivityResultLauncher<String, Boolean>
) {
    if (isVisible.value) {
        AlertDialog(
            onDismissRequest = {},
            confirmButton = {
                Button(
                    onClick = {
                        if (Build.VERSION.SDK_INT > 32) {
                            notificationRuntimePermission.launch(android.Manifest.permission.POST_NOTIFICATIONS)
                        }
                        isVisible.value = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = Localization.Key.EnableNotifications.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            },
            title = {
                Text(
                    text = Localization.Key.NotificationPermissionRequired.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp
                )
            },
            text = {
                Text(
                    text = Localization.Key.NotificationPermissionDesc.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 15.sp
                )
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        isVisible.value = false
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = Localization.Key.Cancel.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall
                    )
                }
            }
        )
    }
}