package com.sakethh.linkora

import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.ifTrue
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.utils.AndroidUIEvent
import com.sakethh.linkora.utils.AndroidUIEvent.pushUIEvent
import com.sakethh.platform
import kotlinx.coroutines.flow.collectLatest

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            val coroutineScope = rememberCoroutineScope()
            val runtimePermission = rememberLauncherForActivityResult(
                contract = ActivityResultContracts.RequestPermission(),
                onResult = { permissionGranted ->
                    coroutineScope.pushUIEvent(
                        AndroidUIEvent.Type.PermissionGrantedForAndBelowQ(
                            isGranted = permissionGranted
                        )
                    )
                })
            val activityResultLauncher =
                rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                    coroutineScope.pushUIEvent(
                        AndroidUIEvent.Type.UriOfTheFileForImporting(
                            uri
                        )
                    )
                }
            LaunchedEffect(Unit) {
                AndroidUIEvent.androidUIEventChannel.collectLatest {
                    when (it) {
                        is AndroidUIEvent.Type.ShowRuntimePermissionForStorage -> {
                            pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.StoragePermissionIsRequired.getLocalizedString()))
                            runtimePermission.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        }

                        is AndroidUIEvent.Type.PermissionGrantedForAndBelowQ -> {
                            it.isGranted.ifNot {
                                pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.StoragePermissionIsRequired.getLocalizedString()))
                            }.ifTrue {
                                pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.PermissionGranted.getLocalizedString()))
                            }
                        }

                        is AndroidUIEvent.Type.ImportAFile -> {
                            activityResultLauncher.launch(it.fileType)
                        }

                        else -> {}
                    }
                }
            }
            CompositionLocalProvider(
                LocalNavController provides navController
            ) {
                val localConfiguration = LocalConfiguration.current
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
                    val systemUIController = rememberSystemUiController()
                    val platform = platform()
                    val mainRoutes = remember {
                        listOf(
                            Navigation.Root.HomeScreen,
                            Navigation.Root.SearchScreen,
                            Navigation.Root.CollectionsScreen,
                            Navigation.Root.SettingsScreen
                        )
                    }
                    val currentBackStackEntry = navController.currentBackStackEntryAsState()
                    val navigationBarElevation = NavigationBarDefaults.Elevation
                    val rootRoutesColor = colorScheme.surfaceColorAtElevation(
                        navigationBarElevation
                    )
                    LaunchedEffect(currentBackStackEntry.value) {
                        systemUIController.setSystemBarsColor(colors.surface)
                        if (platform == Platform.Android.Mobile) {
                            systemUIController.setNavigationBarColor(
                                color = if (mainRoutes.any {
                                        currentBackStackEntry.value?.destination?.hasRoute(it::class) == true
                                    }) rootRoutesColor else colors.surface
                            )
                        } else {
                            systemUIController.setNavigationBarColor(
                                color = colors.surface
                            )
                        }
                    }
                    Surface {
                        App()
                    }
                }
            }
        }
    }
}