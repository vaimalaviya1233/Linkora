package com.sakethh.linkora

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Maximize
import androidx.compose.material.icons.filled.Minimize
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.outlined.Window
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ApplicationScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.WindowPlacement
import androidx.compose.ui.window.WindowState
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import androidx.navigation.compose.rememberNavController
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.data.local.LinkoraDataStoreName
import com.sakethh.linkora.data.local.createDataStore
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.DesktopTypography
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent

fun main() {
    val dataStorePref = createDataStore {
        LinkoraDataStoreName
    }
    DependencyContainer.dataStorePref = dataStorePref
    AppPreferences.readAll(DependencyContainer.preferencesRepo.value)
    Localization.loadLocalizedStrings(
        AppPreferences.preferredAppLanguageCode.value
    )
    application {
        val windowState = rememberWindowState(
            width = 1054.dp,
            height = 600.dp
        )
        val navController = rememberNavController()
        Window(
            state = windowState,
            onCloseRequest = ::exitApplication,
            title = Localization.Key.Linkora.getLocalizedString(),
            undecorated = true
        ) {
            CompositionLocalProvider(
                LocalNavController provides navController
            ) {
                LinkoraTheme(
                    typography = DesktopTypography,
                    colorScheme = if (AppPreferences.shouldFollowSystemTheme.value) {
                        if (isSystemInDarkTheme()) DarkColors else LightColors
                    } else {
                        if (AppPreferences.shouldUseForceDarkTheme.value) DarkColors else LightColors
                    }
                ) {
                    Scaffold(topBar = {
                        WindowDraggableArea {
                            TopDecorator(windowState)
                        }
                    },
                        modifier = Modifier.border(
                            0.5.dp,
                            MaterialTheme.colorScheme.outline.copy(0.25f)
                        )
                    ) {
                        App(modifier = Modifier.padding(it))
                    }
                }
            }
        }
    }
}

@Composable
private fun ApplicationScope.TopDecorator(windowState: WindowState) {
    val coroutineScope = rememberCoroutineScope()
    Column {
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
            Row(
                verticalAlignment = Alignment.CenterVertically, modifier = Modifier.align(
                    Alignment.CenterStart
                )
            ) {
                if (AppPreferences.isServerConfigured()) {
                    Spacer(Modifier.padding(start = 15.dp))
                    Icon(
                        imageVector = Icons.Default.WbCloudy,
                        contentDescription = null,
                        modifier = Modifier.clickable(onClick = {
                            coroutineScope.pushUIEvent(
                                UIEvent.Type.ShowSnackbar(
                                    Localization.Key.LinkoraIsConnectedToAServer.getLocalizedString()
                                        .replace(
                                            LinkoraPlaceHolder.First.value,
                                            "\"${AppPreferences.serverSyncType.value.asUIString()}\""
                                        )
                                )
                            )
                        }, indication = null, interactionSource = remember {
                            MutableInteractionSource()
                        })
                    )
                }
                Spacer(
                    Modifier.padding(
                        start = if (AppPreferences.isServerConfigured().not()) 15.dp else 5.dp
                    )
                )
                Text(
                    text = Localization.Key.Linkora.rememberLocalizedString(),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
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