package com.sakethh.linkora.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SystemUpdateAlt
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import kotlinx.coroutines.launch

open class SettingsScreenViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    fun generalSection(): List<SettingComponentParam> {
        return listOf(
            SettingComponentParam(
                title = Localization.getLocalizedString(Localization.Key.UseInAppBrowser),
                doesDescriptionExists = AppPreferences.showDescriptionForSettingsState.value,
                description = Localization.getLocalizedString(Localization.Key.UseInAppBrowserDesc),
                isSwitchNeeded = true,
                isSwitchEnabled = AppPreferences.isInAppWebTabEnabled,
                isIconNeeded = mutableStateOf(true),
                icon = Icons.Default.OpenInBrowser,
                onSwitchStateChange = {
                    viewModelScope.launch {
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.CUSTOM_TABS.name
                            ), newValue = it
                        )
                        AppPreferences.isInAppWebTabEnabled.value = it
                    }
                }), SettingComponentParam(
                title = Localization.getLocalizedString(Localization.Key.EnableHomeScreen),
                doesDescriptionExists = AppPreferences.showDescriptionForSettingsState.value,
                description = Localization.getLocalizedString(Localization.Key.EnableHomeScreenDesc),
                isSwitchNeeded = true,
                isIconNeeded = mutableStateOf(true),
                icon = Icons.Default.Home,
                isSwitchEnabled = AppPreferences.isHomeScreenEnabled,
                onSwitchStateChange = {
                    viewModelScope.launch {
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.HOME_SCREEN_VISIBILITY.name
                            ), newValue = it
                        )
                        AppPreferences.isHomeScreenEnabled.value = it
                    }
                }), SettingComponentParam(
                title = Localization.getLocalizedString(Localization.Key.AutoDetectTitle),
                doesDescriptionExists = true,
                description = Localization.getLocalizedString(Localization.Key.AutoDetectTitleDesc),
                isSwitchNeeded = true,
                isSwitchEnabled = AppPreferences.isAutoDetectTitleForLinksEnabled,
                isIconNeeded = mutableStateOf(true),
                icon = Icons.Default.Search,
                onSwitchStateChange = {
                    viewModelScope.launch {
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.AUTO_DETECT_TITLE_FOR_LINK.name
                            ), newValue = it
                        )
                        AppPreferences.isAutoDetectTitleForLinksEnabled.value = it

                        if (it) {
                            changeSettingPreferenceValue(
                                preferenceKey = booleanPreferencesKey(
                                    AppPreferenceType.FORCE_SAVE_WITHOUT_FETCHING_META_DATA.name
                                ), newValue = false
                            )
                            AppPreferences.forceSaveWithoutFetchingAnyMetaData.value = false
                        }
                    }
                }), SettingComponentParam(
                title = Localization.getLocalizedString(Localization.Key.ForceSaveWithoutRetrievingMetadata),
                doesDescriptionExists = true,
                description = Localization.getLocalizedString(Localization.Key.ForceSaveWithoutRetrievingMetadataDesc),
                isSwitchNeeded = true,
                isSwitchEnabled = AppPreferences.forceSaveWithoutFetchingAnyMetaData,
                isIconNeeded = mutableStateOf(true),
                icon = Icons.Default.PublicOff,
                onSwitchStateChange = {
                    viewModelScope.launch {
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.FORCE_SAVE_WITHOUT_FETCHING_META_DATA.name
                            ), newValue = it
                        )
                        AppPreferences.forceSaveWithoutFetchingAnyMetaData.value = it

                        if (it) {
                            changeSettingPreferenceValue(
                                preferenceKey = booleanPreferencesKey(
                                    AppPreferenceType.AUTO_DETECT_TITLE_FOR_LINK.name
                                ), newValue = false
                            )
                            AppPreferences.isAutoDetectTitleForLinksEnabled.value = false
                        }
                    }
                }), SettingComponentParam(
                title = Localization.getLocalizedString(Localization.Key.ShowAssociatedImageInLinkMenu),
                doesDescriptionExists = true,
                description = Localization.getLocalizedString(Localization.Key.ShowAssociatedImageInLinkMenuDesc),
                isSwitchNeeded = true,
                isSwitchEnabled = AppPreferences.showAssociatedImagesInLinkMenu,
                isIconNeeded = mutableStateOf(true),
                icon = Icons.Default.Image,
                onSwitchStateChange = {
                    viewModelScope.launch {
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.ASSOCIATED_IMAGES_IN_LINK_MENU_VISIBILITY.name
                            ), newValue = it
                        )
                        AppPreferences.showAssociatedImagesInLinkMenu.value = it
                    }
                }), SettingComponentParam(
                title = Localization.getLocalizedString(Localization.Key.AutoCheckForUpdates),
                doesDescriptionExists = AppPreferences.showDescriptionForSettingsState.value,
                description = Localization.getLocalizedString(Localization.Key.AutoCheckForUpdatesDesc),
                isIconNeeded = mutableStateOf(true),
                icon = Icons.Default.SystemUpdateAlt,
                isSwitchNeeded = true,
                isSwitchEnabled = AppPreferences.isAutoCheckUpdatesEnabled,
                onSwitchStateChange = {
                    viewModelScope.launch {
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.AUTO_CHECK_UPDATES.name
                            ), newValue = it
                        )
                        AppPreferences.isAutoCheckUpdatesEnabled.value = it
                    }
                }), SettingComponentParam(
                title = Localization.getLocalizedString(Localization.Key.ShowDescriptionForSettings),
                doesDescriptionExists = true,
                description = Localization.getLocalizedString(Localization.Key.ShowDescriptionForSettingsDesc),
                isSwitchNeeded = true,
                isIconNeeded = mutableStateOf(true),
                icon = Icons.AutoMirrored.Default.ShortText,
                isSwitchEnabled = AppPreferences.showDescriptionForSettingsState,
                onSwitchStateChange = {
                    viewModelScope.launch {
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.SETTING_COMPONENT_DESCRIPTION_STATE.name
                            ), newValue = it
                        )
                        AppPreferences.showDescriptionForSettingsState.value = it
                    }
                })
        )
    }

    fun <T> changeSettingPreferenceValue(
        preferenceKey: Preferences.Key<T>,
        newValue: T,
    ) {
        viewModelScope.launch {
            preferencesRepository.changePreferenceValue(preferenceKey, newValue)
        }
    }
}