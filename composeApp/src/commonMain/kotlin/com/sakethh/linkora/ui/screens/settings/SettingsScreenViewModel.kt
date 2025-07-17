package com.sakethh.linkora.ui.screens.settings

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.PublicOff
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.rounded.Home
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.navigation.Navigation
import kotlinx.coroutines.launch

open class SettingsScreenViewModel(
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {

    fun generalSection(platform: Platform): List<SettingComponentParam> {
        return buildList {
            this.addAll(
                listOf(
                    SettingComponentParam(
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
                        },
                    ), SettingComponentParam(
                        title = "Skip saving existing links",
                        doesDescriptionExists = true,
                        description = "If enabled, a link won't be saved if it already exists in the destination. An error will be thrown instead.",
                        isSwitchNeeded = true,
                        isSwitchEnabled = AppPreferences.skipSavingExistingLink,
                        isIconNeeded = mutableStateOf(true),
                        icon = Icons.Default.Block,
                        onSwitchStateChange = {
                            viewModelScope.launch {
                                changeSettingPreferenceValue(
                                    preferenceKey = booleanPreferencesKey(
                                        AppPreferenceType.SKIP_SAVING_EXISTING_LINK.name
                                    ), newValue = it
                                )
                                AppPreferences.skipSavingExistingLink.value = it
                            }
                        },
                    )
                )
            )

            if (platform == Platform.Android.Mobile) {
                add(
                    SettingComponentParam(
                        title = Localization.getLocalizedString(Localization.Key.ShowAssociatedImageInLinkMenu),
                        doesDescriptionExists = true,
                        description = Localization.getLocalizedString(Localization.Key.ShowAssociatedImageInLinkMenuDesc),
                        isSwitchNeeded = true,
                        isSwitchEnabled = AppPreferences.showAssociatedImageInLinkMenu,
                        isIconNeeded = mutableStateOf(true),
                        icon = Icons.Default.Image,
                        onSwitchStateChange = {
                            viewModelScope.launch {
                                changeSettingPreferenceValue(
                                    preferenceKey = booleanPreferencesKey(
                                        AppPreferenceType.ASSOCIATED_IMAGES_IN_LINK_MENU_VISIBILITY.name
                                    ), newValue = it
                                )
                                AppPreferences.showAssociatedImageInLinkMenu.value = it
                            }
                        })
                )
            }
            add(
                SettingComponentParam(
                    title = "Enable Home Screen",
                    doesDescriptionExists = true,
                    description = "When disabled, Collections opens on launch if Home is set as the initial route.",
                    isSwitchNeeded = true,
                    isSwitchEnabled = AppPreferences.isHomeScreenEnabled,
                    onSwitchStateChange = {
                        AppPreferences.isHomeScreenEnabled.value = it
                        changeSettingPreferenceValue(
                            preferenceKey = booleanPreferencesKey(
                                AppPreferenceType.HOME_SCREEN_VISIBILITY.name
                            ), newValue = it
                        )
                    },
                    isIconNeeded = mutableStateOf(true),
                    icon = Icons.Rounded.Home
                )
            )
        }
    }

    fun acknowledgementSection(): List<SettingComponentParam> {
        return listOf(
            SettingComponentParam(
                title = "Kotlin",
                doesDescriptionExists = true,
                description = "Apache License (Version 2.0)",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {},
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/JetBrains/kotlin")
                },
                isIconNeeded = mutableStateOf(false),
                shouldArrowIconBeAppear = mutableStateOf(true)
            ),
            SettingComponentParam(
                title = "Android Jetpack",
                doesDescriptionExists = true,
                description = "Apache License (Version 2.0)",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {},
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/androidx/androidx")
                },
                shouldArrowIconBeAppear = mutableStateOf(true),
                isIconNeeded = mutableStateOf(false)
            ),
            SettingComponentParam(
                title = "Coil",
                doesDescriptionExists = true,
                description = "Apache License (Version 2.0)",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/coil-kt/coil")
                },
                shouldArrowIconBeAppear = mutableStateOf(true),
                isIconNeeded = mutableStateOf(false)
            ),
            SettingComponentParam(
                title = "jsoup",
                doesDescriptionExists = true,
                description = "MIT License",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/jhy/jsoup")
                },
                shouldArrowIconBeAppear = mutableStateOf(true),
                isIconNeeded = mutableStateOf(false)
            ),
            SettingComponentParam(
                title = "Material Design 3",
                doesDescriptionExists = true,
                description = "Apache License (Version 2.0)",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://m3.material.io/")
                },
                shouldArrowIconBeAppear = mutableStateOf(true),
                isIconNeeded = mutableStateOf(false)
            ),
            SettingComponentParam(
                title = "ktor-client",
                doesDescriptionExists = true,
                description = "Apache License (Version 2.0)",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/ktorio/ktor")
                },
                shouldArrowIconBeAppear = mutableStateOf(true),
                isIconNeeded = mutableStateOf(false)
            ),
            SettingComponentParam(
                title = "kotlinx.serialization",
                doesDescriptionExists = true,
                description = "Apache License (Version 2.0)",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/Kotlin/kotlinx.serialization")
                },
                shouldArrowIconBeAppear = mutableStateOf(true),
                isIconNeeded = mutableStateOf(false)
            ),
            SettingComponentParam(
                title = "Material Icons",
                doesDescriptionExists = true,
                description = "Apache License (Version 2.0)",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/google/material-design-icons")
                },
                isIconNeeded = mutableStateOf(false),
                shouldArrowIconBeAppear = mutableStateOf(true)
            ),
            SettingComponentParam(
                title = "vxTwitter",
                doesDescriptionExists = true,
                description = "WTFPL License",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://github.com/dylanpdx/BetterTwitFix")
                },
                isIconNeeded = mutableStateOf(false),
                shouldArrowIconBeAppear = mutableStateOf(true)
            ),
            SettingComponentParam(
                title = "Poppins",
                doesDescriptionExists = true,
                description = "Open Font License",
                isSwitchNeeded = false,
                isSwitchEnabled = mutableStateOf(false),
                onSwitchStateChange = {

                },
                onAcknowledgmentClick = { uriHandler ->
                    uriHandler.openUri("https://fonts.google.com/specimen/Poppins")
                },
                isIconNeeded = mutableStateOf(false),
                shouldArrowIconBeAppear = mutableStateOf(true)
            ),
        )
    }

    fun <T> changeSettingPreferenceValue(
        preferenceKey: Preferences.Key<T>, newValue: T, onCompletion: () -> Unit = {}
    ) {
        viewModelScope.launch {
            preferencesRepository.changePreferenceValue(preferenceKey, newValue)
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun currInitialRoute(init: (String) -> Unit) {
        viewModelScope.launch {
            (preferencesRepository.readPreferenceValue(stringPreferencesKey(AppPreferenceType.INITIAL_ROUTE.name))
                ?: Navigation.Root.HomeScreen.toString()).let {
                init(it)
            }
        }
    }

}