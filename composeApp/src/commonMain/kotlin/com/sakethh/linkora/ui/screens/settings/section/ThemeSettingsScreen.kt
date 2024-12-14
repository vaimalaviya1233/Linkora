package com.sakethh.linkora.ui.screens.settings.section

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.navigation.NavController
import com.sakethh.linkora.Platform
import com.sakethh.linkora.core.preferences.AppPreferenceType
import com.sakethh.linkora.core.preferences.AppPreferences
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.shouldFollowSystemThemeComposableVisible

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen(
    navController: NavController,
    platform: Platform,
    settingsScreenViewModel: SettingsScreenViewModel
) {
    val isSystemInDarkTheme = isSystemInDarkTheme()
    SettingsSectionScaffold(
        topAppBarText = "Theme",
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            if (shouldFollowSystemThemeComposableVisible && !AppPreferences.shouldUseForceDarkTheme.value) {
                item(key = "Follow System Theme") {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Follow System Theme",
                            doesDescriptionExists = false,
                            isSwitchNeeded = true,
                            description = null,
                            isSwitchEnabled = AppPreferences.shouldFollowSystemTheme,
                            onSwitchStateChange = {
                                AppPreferences.shouldFollowSystemTheme.value =
                                    AppPreferences.shouldFollowSystemTheme.value.not()
                                settingsScreenViewModel.changeSettingPreferenceValue(
                                    booleanPreferencesKey(AppPreferenceType.FOLLOW_SYSTEM_THEME.name),
                                    it
                                )
                            }, isIconNeeded = remember {
                                mutableStateOf(false)
                            }
                        )
                    )
                }
            }
            if (!AppPreferences.shouldFollowSystemTheme.value) {
                item(key = "Use Dark Mode") {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Use Dark Mode",
                            doesDescriptionExists = false,
                            description = null,
                            isSwitchNeeded = true,
                            isSwitchEnabled = AppPreferences.shouldUseForceDarkTheme,
                            onSwitchStateChange = {
                                AppPreferences.shouldUseForceDarkTheme.value =
                                    AppPreferences.shouldUseForceDarkTheme.value.not()
                                settingsScreenViewModel.changeSettingPreferenceValue(
                                    booleanPreferencesKey(AppPreferenceType.DARK_THEME.name),
                                    it
                                )
                            }, isIconNeeded = remember {
                                mutableStateOf(false)
                            })
                    )
                }
            }
            if (platform is Platform.Android && (AppPreferences.shouldUseForceDarkTheme.value || (isSystemInDarkTheme && AppPreferences.shouldFollowSystemTheme.value))) {
                item {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Use Amoled Theme",
                            doesDescriptionExists = false,
                            description = "",
                            isSwitchNeeded = true,
                            isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                            onSwitchStateChange = {
                                AppPreferences.shouldUseAmoledTheme.value =
                                    AppPreferences.shouldUseAmoledTheme.value.not()
                                settingsScreenViewModel.changeSettingPreferenceValue(
                                    booleanPreferencesKey(AppPreferenceType.AMOLED_THEME_STATE.name),
                                    it
                                )
                            }, isIconNeeded = remember {
                                mutableStateOf(false)
                            })
                    )
                }
            }
            if (platform is Platform.Android) {
                item(key = "Use dynamic theming") {
                    SettingComponent(
                        SettingComponentParam(
                            title = "Use dynamic theming",
                            doesDescriptionExists = true,
                            description = "LocalizedStrings.useDynamicThemingDesc.value",
                            isSwitchNeeded = true,
                            isSwitchEnabled = AppPreferences.shouldUseDynamicTheming,
                            onSwitchStateChange = {
                                AppPreferences.shouldUseDynamicTheming.value =
                                    AppPreferences.shouldUseDynamicTheming.value.not()
                                settingsScreenViewModel.changeSettingPreferenceValue(
                                    booleanPreferencesKey(AppPreferenceType.DYNAMIC_THEMING.name),
                                    it
                                )
                            }, isIconNeeded = remember {
                                mutableStateOf(false)
                            })
                    )
                }
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
}