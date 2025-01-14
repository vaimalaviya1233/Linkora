package com.sakethh.linkora

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.components.AddANewLinkDialogBox
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.utils.isTablet
import kotlinx.coroutines.flow.collectLatest

class IntentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val localConfiguration = LocalConfiguration.current
            val navController = rememberNavController()
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalPlatform provides if (isTablet(localConfiguration)) Platform.Android.Tablet else Platform.Android.Mobile
            ) {
                val context = LocalContext.current
                val darkColors = DarkColors.copy(
                    background = if (AppPreferences.shouldUseAmoledTheme.value) Color(0xFF000000) else DarkColors.background,
                    surface = if (AppPreferences.shouldUseAmoledTheme.value) Color(0xFF000000) else DarkColors.surface
                )
                val shouldUIBeVisible = rememberSaveable {
                    mutableStateOf(true)
                }
                LaunchedEffect(shouldUIBeVisible.value) {
                    shouldUIBeVisible.value.ifNot {
                        this@IntentActivity.finishAndRemoveTask()
                    }
                }
                LaunchedEffect(Unit) {
                    UIEvent.uiEventsReadOnlyChannel.collectLatest {
                        if (it is UIEvent.Type.ShowSnackbar) {
                            // Toast isn't supposed to show, but as soon as `shouldUIBeVisible` isn't true, the snackbar won't show either
                            // toast stays on the UI for a while, no matter what happens with the clearance
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
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
                    AddANewLinkDialogBox(
                        shouldBeVisible = shouldUIBeVisible,
                        screenType = ScreenType.ROOT_SCREEN,
                        currentFolder = null,
                        collectionsScreenVM = CollectionsScreenVM(
                            localFoldersRepo = DependencyContainer.localFoldersRepo.value,
                            localLinksRepo = DependencyContainer.localLinksRepo.value,
                            loadRootFoldersOnInit = true,
                            collectionDetailPaneInfo = null
                        ),
                        url = this@IntentActivity.intent?.getStringExtra(
                            Intent.EXTRA_TEXT
                        ).toString()
                    )
                }
            }
        }
    }
}