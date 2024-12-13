package com.sakethh.linkora

import android.content.res.Configuration
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
import androidx.navigation.compose.rememberNavController
import com.sakethh.linkora.core.preferences.AppPreferences
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.utils.isTablet

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val localConfiguration = LocalConfiguration.current
            val navController = rememberNavController()
            val context = LocalContext.current
            val darkColors = DarkColors.copy(
                background = if (AppPreferences.shouldFollowAmoledTheme.value) Color(0xFF000000) else DarkColors.background,
                surface = if (AppPreferences.shouldFollowAmoledTheme.value) Color(0xFF000000) else DarkColors.surface
            )
            val colors = when {
                AppPreferences.shouldFollowDynamicTheming.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                    if (AppPreferences.shouldFollowSystemTheme.value) {
                        if (isSystemInDarkTheme()) dynamicDarkColorScheme(context).copy(
                            background = if (AppPreferences.shouldFollowAmoledTheme.value) Color(
                                0xFF000000
                            ) else dynamicDarkColorScheme(context).background,
                            surface = if (AppPreferences.shouldFollowAmoledTheme.value) Color(
                                0xFF000000
                            ) else dynamicDarkColorScheme(
                                context
                            ).surface
                        ) else dynamicLightColorScheme(
                            context
                        )
                    } else {
                        if (AppPreferences.shouldDarkThemeBeEnabled.value) dynamicDarkColorScheme(
                            context
                        ).copy(
                            background = if (AppPreferences.shouldFollowAmoledTheme.value) Color(
                                0xFF000000
                            ) else dynamicDarkColorScheme(context).background,
                            surface = if (AppPreferences.shouldFollowAmoledTheme.value) Color(
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
                    if (AppPreferences.shouldDarkThemeBeEnabled.value) darkColors else LightColors
                }
            }
            LinkoraTheme(
                typography = AndroidTypography, colorScheme = colors
            ) {
                Surface {
                    App(
                        platform = if (isTablet(localConfiguration) || localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.Android.Tablet else Platform.Android.Mobile,
                        navController = navController,
                        shouldFollowSystemThemeComposableBeVisible = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                    )
                }
            }
        }
    }
}