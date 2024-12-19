package com.sakethh.linkora

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.data.local.LinkoraDataStoreName
import com.sakethh.linkora.data.local.createDataStore
import com.sakethh.linkora.data.local.repository.PreferencesImpl
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.genericViewModelFactory

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