package com.sakethh.linkora

import android.app.Activity
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.ifTrue
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.App
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.components.NotificationPermissionDialogBox
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.utils.AndroidUIEvent
import com.sakethh.linkora.utils.AndroidUIEvent.pushUIEvent
import com.sakethh.linkora.utils.isTablet
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    companion object {
        var wasLaunched = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.isNavigationBarContrastEnforced = false
        }
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val storageRuntimePermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { permissionGranted ->
                    coroutineScope.pushUIEvent(
                        AndroidUIEvent.Type.StoragePermissionGrantedForAndBelowQ(
                            isGranted = permissionGranted
                        )
                    )
                })
            val notificationRuntimePermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { permissionGranted ->
                    coroutineScope.pushUIEvent(
                        AndroidUIEvent.Type.NotificationPermissionState(
                            isGranted = permissionGranted
                        )
                    )
                })
            val isNotificationPermissionDialogVisible = rememberSaveable {
                mutableStateOf(false)
            }
            val activityResultLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    coroutineScope.pushUIEvent(
                        AndroidUIEvent.Type.UriOfTheFileForImporting(
                            uri
                        )
                    )
                }
            val localContext = LocalContext.current as Activity
            LaunchedEffect(Unit) {
                launch {
                    UIEvent.uiEvents.collectLatest {
                        if (it is UIEvent.Type.MinimizeTheApp) {
                            localContext.moveTaskToBack(true)
                        }
                    }
                }

                launch {
                    AndroidUIEvent.androidUIEventChannel.collectLatest {
                        when (it) {
                            is AndroidUIEvent.Type.ShowRuntimePermissionForStorage -> {
                                pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.StoragePermissionIsRequired.getLocalizedString()))
                                storageRuntimePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            }

                            is AndroidUIEvent.Type.StoragePermissionGrantedForAndBelowQ -> {
                                it.isGranted.ifNot {
                                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.StoragePermissionIsRequired.getLocalizedString()))
                                }.ifTrue {
                                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.PermissionGranted.getLocalizedString()))
                                }
                            }

                            is AndroidUIEvent.Type.ImportAFile -> {
                                activityResultLauncher.launch(it.fileType)
                            }

                            is AndroidUIEvent.Type.ShowRuntimePermissionForNotifications -> {
                                if (Build.VERSION.SDK_INT > 32) {
                                    isNotificationPermissionDialogVisible.value = true
                                }
                            }

                            is AndroidUIEvent.Type.NotificationPermissionState -> {
                                it.isGranted.ifNot {
                                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.NotificationPermissionIsRequired.getLocalizedString()))
                                }
                            }

                            else -> {}
                        }
                    }
                }
            }
            val localConfiguration = LocalConfiguration.current
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalPlatform provides if (isTablet(localConfiguration)) Platform.Android.Tablet else Platform.Android.Mobile
            ) {
                val context = LocalContext.current
                val darkColors = DarkColors.copy(
                    background = if (AppPreferences.shouldUseAmoledTheme.value) Color(0xFF000000) else DarkColors.background,
                    surface = if (AppPreferences.shouldUseAmoledTheme.value) Color(0xFF000000) else DarkColors.surface
                )
                val colors = when {
                    AppPreferences.shouldUseDynamicTheming.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        if (AppPreferences.shouldFollowSystemTheme.value) {
                            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context).copy(
                                background = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(context).background,
                                surface = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(
                                    context
                                ).surface
                            ) else dynamicLightColorScheme(
                                context
                            )
                        } else {
                            if (AppPreferences.shouldUseForceDarkTheme.value) dynamicDarkColorScheme(
                                context
                            ).copy(
                                background = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(context).background,
                                surface = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(
                                    context
                                ).surface
                            ) else dynamicLightColorScheme(context)
                        }
                    }

                    else -> if (AppPreferences.shouldFollowSystemTheme.value) {
                        if (isSystemInDarkTheme()) darkColors else LightColors
                    } else {
                        if (AppPreferences.shouldUseForceDarkTheme.value) darkColors else LightColors
                    }
                }
                LinkoraTheme(
                    typography = AndroidTypography, colorScheme = colors
                ) {
                    Surface {
                        App()
                    }
                    NotificationPermissionDialogBox(
                        isVisible = isNotificationPermissionDialogVisible,
                        notificationRuntimePermission = notificationRuntimePermission
                    )
                }
            }
        }
        wasLaunched = true
    }
}