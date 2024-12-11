package com.sakethh.linkora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.sakethh.linkora.theme.LinkoraTheme

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
                TopDecorator(windowState)
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = {
                    windowState.isMinimized = true
                }) {
                    Icon(imageVector = Icons.Default.Minimize, contentDescription = null)
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