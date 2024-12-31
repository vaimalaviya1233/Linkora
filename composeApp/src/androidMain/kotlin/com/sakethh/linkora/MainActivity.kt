package com.sakethh.linkora

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.data.local.LinkoraDataStoreName
import com.sakethh.linkora.data.local.createDataStore
import com.sakethh.linkora.data.local.repository.PreferencesImpl
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.platform

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val preferencesRepository = PreferencesImpl(dataStore = createDataStore {
            applicationContext.filesDir.resolve(LinkoraDataStoreName).absolutePath
        })
        AppPreferences.readAll(preferencesRepository)
        setContent {
            val settingsScreenViewModel =
                viewModel<SettingsScreenViewModel>(factory = genericViewModelFactory {
                    SettingsScreenViewModel(PreferencesImpl(dataStore = preferencesRepository.dataStore))
                })
            val localConfiguration = LocalConfiguration.current
            val navController = rememberNavController()
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
                    App(
                        navController = navController,
                        settingsScreenViewModel = settingsScreenViewModel
                    )
                }
            }
        }
    }
}