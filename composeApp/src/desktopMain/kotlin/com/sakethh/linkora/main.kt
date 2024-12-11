package com.sakethh.linkora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sakethh.linkora.ui.theme.LinkoraTheme

fun main() = application {
    val windowState = rememberWindowState()
    Window(
        state = windowState,
        onCloseRequest = ::exitApplication,
        title = "Linkora",
        undecorated = true
    ) {
        LinkoraTheme {
            Scaffold(topBar = {
                WindowDraggableArea {
                    TopDecorator(windowState)
                }
            }) {
                App(modifier = Modifier.padding(it))
            }
        }
    }
}

@Composable
private fun ApplicationScope.TopDecorator(windowState: WindowState) {
    Column {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Text(
                text = "Linkora",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 15.dp).align(
                    Alignment.CenterStart
                ),
            )
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    windowState.isMinimized = true
                }) {
                    Icon(imageVector = Icons.Default.Minimize, contentDescription = null)
                }
                IconButton(onClick = {
                    if (windowState.placement == WindowPlacement.Fullscreen) {
                        windowState.placement = WindowPlacement.Floating
                    } else {
                        windowState.placement = WindowPlacement.Fullscreen
                    }
                }) {
                    Icon(
                        imageVector = if (windowState.placement != WindowPlacement.Fullscreen) Icons.Default.Maximize else Icons.Outlined.Window,
                        contentDescription = null
                    )
                }
                IconButton(onClick = {
                    this@TopDecorator.exitApplication()
                }) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = null)
                }
            }
        }
        HorizontalDivider()
    }
}