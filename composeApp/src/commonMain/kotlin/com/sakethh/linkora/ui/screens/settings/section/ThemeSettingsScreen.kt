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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.platform
import com.sakethh.showDynamicThemingOption
import com.sakethh.showFollowSystemThemeOption

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeSettingsScreen() {
    val navController = LocalNavController.current
    val settingsScreenViewModel: SettingsScreenViewModel =
        viewModel(factory = genericViewModelFactory {
            SettingsScreenViewModel(DependencyContainer.preferencesRepo.value)
        })
    val platform = platform()
    val isSystemInDarkTheme = isSystemInDarkTheme()
    SettingsSectionScaffold(
        topAppBarText = Localization.Key.Theme.rememberLocalizedString(),
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
            if (showFollowSystemThemeOption && !AppPreferences.shouldUseForceDarkTheme.value) {
                item(key = Localization.Key.FollowSystemTheme.defaultValue) {
                    SettingComponent(
                        SettingComponentParam(
                            title = Localization.Key.FollowSystemTheme.rememberLocalizedString(),
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
                item(key = Localization.Key.UseDarkMode.defaultValue) {
                    SettingComponent(
                        SettingComponentParam(
                            title = Localization.Key.UseDarkMode.rememberLocalizedString(),
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
                            title = Localization.Key.UseAmoledTheme.rememberLocalizedString(),
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
            if (platform is Platform.Android && showDynamicThemingOption) {
                item(key = Localization.Key.UseDynamicTheming.defaultValue) {
                    SettingComponent(
                        SettingComponentParam(
                            title = Localization.Key.UseDynamicTheming.rememberLocalizedString(),
                            doesDescriptionExists = true,
                            description = Localization.Key.UseDynamicThemingDesc.rememberLocalizedString(),
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