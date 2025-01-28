package com.sakethh.linkora.common.preferences

import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.BUILD_FLAVOUR
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.domain.SyncType
import com.sakethh.linkora.domain.dto.server.Correlation
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.domain.Layout
import com.sakethh.linkora.ui.domain.SortingType
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.showFollowSystemThemeOption
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

object AppPreferences {
    val shouldUseForceDarkTheme = mutableStateOf(true)
    val shouldFollowSystemTheme = mutableStateOf(false)
    val shouldUseAmoledTheme = mutableStateOf(false)
    val shouldUseDynamicTheming = mutableStateOf(false)

    val isInAppWebTabEnabled = mutableStateOf(true)
    val isAutoDetectTitleForLinksEnabled = mutableStateOf(false)
    val showAssociatedImagesInLinkMenu = mutableStateOf(false)
    val isBtmSheetEnabledForSavingLinks = mutableStateOf(false)
    val isHomeScreenEnabled = mutableStateOf(true)
    val isSendCrashReportsEnabled = mutableStateOf(true)
    val didDataAutoDataMigratedFromV9 = mutableStateOf(false)
    val isAutoCheckUpdatesEnabled = mutableStateOf(true)
    val showDescriptionForSettingsState = mutableStateOf(true)
    val useLanguageStringsBasedOnFetchedValuesFromServer = mutableStateOf(false)
    val isOnLatestUpdate = mutableStateOf(false)
    val didServerTimeOutErrorOccurred = mutableStateOf(false)
    val selectedSortingTypeType = mutableStateOf(SortingType.NEW_TO_OLD.name)
    val primaryJsoupUserAgent =
        mutableStateOf(Constants.DEFAULT_USER_AGENT)
    val secondaryJsoupUserAgent =
        mutableStateOf("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:131.0) Gecko/20100101 Firefox/131.0")
    val localizationServerURL =
        mutableStateOf(Constants.LOCALIZATION_SERVER_URL)
    val isShelfMinimizedInHomeScreen = mutableStateOf(false)
    val lastSelectedPanelID = mutableLongStateOf(-1)
    val preferredAppLanguageName = mutableStateOf("English")
    val preferredAppLanguageCode = mutableStateOf("en")
    val totalLocalAppStrings = mutableIntStateOf(328)
    val totalRemoteStrings = mutableIntStateOf(0)
    val remoteStringsLastUpdatedOn = mutableStateOf("")
    val currentlySelectedLinkLayout = mutableStateOf(Layout.REGULAR_LIST_VIEW.name)
    val enableBorderForNonListViews = mutableStateOf(true)
    val enableTitleForNonListViews = mutableStateOf(true)
    val enableBaseURLForNonListViews = mutableStateOf(true)
    val enableFadedEdgeForNonListViews = mutableStateOf(true)
    val shouldFollowAmoledTheme = mutableStateOf(false)
    val forceSaveWithoutFetchingAnyMetaData = mutableStateOf(false)
    val startDestination = mutableStateOf(Navigation.Root.HomeScreen.toString())
    val serverBaseUrl = mutableStateOf("")
    val serverSecurityToken = mutableStateOf("")
    val serverSyncType = mutableStateOf(SyncType.TwoWay)
    val useLinkoraTopDecoratorOnDesktop = mutableStateOf(true)
    val refreshLinksWorkerTag =
        mutableStateOf("52ae3f4a-d37f-4fdb-a6b6-4397b99ef1bd")
    val showVideoTagOnUIIfApplicable = mutableStateOf(true)
    private var correlation = Correlation.generateRandomCorrelation()

    private var lastSyncedWithServer: Long = 0

    fun lastSyncedLocally(): Long {
        return lastSyncedWithServer
    }

    fun updateLastSyncedLocally(value: Long) {
        lastSyncedWithServer = value
    }

    fun getCorrelation(): Correlation {
        return correlation
    }

    fun isServerConfigured(): Boolean {
        return serverBaseUrl.value.isNotBlank()
    }

    fun canPushToServer(): Boolean {
        return listOf(SyncType.TwoWay, SyncType.ClientToServer).any {
            isServerConfigured() && serverSyncType.value == it
        }
    }

    fun canReadFromServer(): Boolean {
        return listOf(SyncType.TwoWay, SyncType.ServerToClient).any {
            isServerConfigured() && serverSyncType.value == it
        }
    }

    fun readAll(preferencesRepository: PreferencesRepository) = runBlocking {
        supervisorScope {
            listOf(
                launch {
                    serverBaseUrl.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.SERVER_URL.name),
                    ) ?: serverBaseUrl.value
                },
                launch {
                    serverSecurityToken.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.SERVER_AUTH_TOKEN.name),
                    ) ?: serverSecurityToken.value
                },
                launch {
                    serverSyncType.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.SERVER_SYNC_TYPE.name),
                    )?.let { SyncType.valueOf(it) } ?: serverSyncType.value
                },
                launch {
                    isHomeScreenEnabled.value = if (preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.HOME_SCREEN_VISIBILITY.name),
                        ) == null
                    ) {
                        true
                    } else {
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.HOME_SCREEN_VISIBILITY.name),
                        ) == true
                    }
                },
                launch {
                    shouldFollowSystemTheme.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.FOLLOW_SYSTEM_THEME.name),
                    ) ?: showFollowSystemThemeOption
                },
                launch {
                    startDestination.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.INITIAL_ROUTE.name),
                        
                    ) ?: startDestination.value
                },
                launch {
                    shouldUseForceDarkTheme.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.DARK_THEME.name),
                    ) ?: showFollowSystemThemeOption.not()
                },
                launch {
                    shouldUseDynamicTheming.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.DYNAMIC_THEMING.name),
                    ) ?: false
                },
                launch {
                    primaryJsoupUserAgent.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.JSOUP_USER_AGENT.name),
                        
                    )
                        ?: primaryJsoupUserAgent.value
                },
                launch {
                    secondaryJsoupUserAgent.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.SECONDARY_JSOUP_USER_AGENT.name),
                        
                    )
                        ?: secondaryJsoupUserAgent.value
                },
                launch {
                    showDescriptionForSettingsState.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.SETTING_COMPONENT_DESCRIPTION_STATE.name),
                        
                    ) ?: true
                },
                launch {
                    isInAppWebTabEnabled.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.CUSTOM_TABS.name),
                        
                    ) ?: false
                },
                launch {
                    didDataAutoDataMigratedFromV9.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.IS_DATA_MIGRATION_COMPLETED_FROM_V9.name),
                        
                    ) ?: false
                },
                launch {
                    isAutoDetectTitleForLinksEnabled.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.AUTO_DETECT_TITLE_FOR_LINK.name),
                        
                    ) ?: false
                },
                launch {
                    totalRemoteStrings.intValue = preferencesRepository.readPreferenceValue(
                        preferenceKey = intPreferencesKey(AppPreferenceType.TOTAL_REMOTE_STRINGS.name),
                        
                    ) ?: 0
                },
                launch {
                    isSendCrashReportsEnabled.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.SEND_CRASH_REPORTS.name),
                        
                    ) ?: true
                },
                launch {
                    isAutoCheckUpdatesEnabled.value = (preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.AUTO_CHECK_UPDATES.name),
                    ) ?: BUILD_FLAVOUR) != "fdroid"
                },
                launch {
                    selectedSortingTypeType.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.SORTING_PREFERENCE.name),
                    ) ?: SortingType.NEW_TO_OLD.name
                },
                launch {
                    showAssociatedImagesInLinkMenu.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.ASSOCIATED_IMAGES_IN_LINK_MENU_VISIBILITY.name),
                        
                    ) ?: true
                },
                launch {
                    isShelfMinimizedInHomeScreen.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.SHELF_VISIBLE_STATE.name),
                        
                    ) ?: false
                },
                launch {
                    forceSaveWithoutFetchingAnyMetaData.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.FORCE_SAVE_WITHOUT_FETCHING_META_DATA.name),
                        
                    ) ?: forceSaveWithoutFetchingAnyMetaData.value
                },
                launch {
                    preferredAppLanguageName.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.APP_LANGUAGE_NAME.name),
                        
                    ) ?: "English"
                },
                launch {
                    preferredAppLanguageCode.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.APP_LANGUAGE_CODE.name),
                        
                    ) ?: "en"
                },
                launch {
                    lastSelectedPanelID.longValue = (preferencesRepository.readPreferenceValue(
                        preferenceKey = longPreferencesKey(AppPreferenceType.LAST_SELECTED_PANEL_ID.name),
                        
                    ) ?: -1).toLong()
                },
                launch {
                    useLanguageStringsBasedOnFetchedValuesFromServer.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.USE_REMOTE_LANGUAGE_STRINGS.name),
                            
                        ) ?: false
                },
                launch {
                    enableBorderForNonListViews.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.BORDER_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                            
                        ) ?: enableBorderForNonListViews.value
                },
                launch {
                    enableTitleForNonListViews.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.TITLE_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                            
                        ) ?: enableTitleForNonListViews.value
                },
                launch {
                    enableBaseURLForNonListViews.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.BASE_URL_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                            
                        ) ?: enableBaseURLForNonListViews.value
                },
                launch {
                    enableFadedEdgeForNonListViews.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.FADED_EDGE_VISIBILITY_FOR_NON_LIST_VIEWS.name),
                            
                        ) ?: enableFadedEdgeForNonListViews.value
                },
                launch {
                    localizationServerURL.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = stringPreferencesKey(AppPreferenceType.LOCALIZATION_SERVER_URL.name),
                        ) ?: Constants.LOCALIZATION_SERVER_URL
                },
                launch {
                    remoteStringsLastUpdatedOn.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = stringPreferencesKey(AppPreferenceType.REMOTE_STRINGS_LAST_UPDATED_ON.name),
                            
                        ) ?: ""
                },
                launch {
                    currentlySelectedLinkLayout.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = stringPreferencesKey(AppPreferenceType.CURRENTLY_SELECTED_LINK_VIEW.name),
                            
                        ) ?: currentlySelectedLinkLayout.value
                },
                launch {
                    shouldFollowAmoledTheme.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.AMOLED_THEME_STATE.name),
                            
                        ) ?: false
                },
                launch {
                    useLinkoraTopDecoratorOnDesktop.value =
                        preferencesRepository.readPreferenceValue(
                            preferenceKey = booleanPreferencesKey(AppPreferenceType.DESKTOP_TOP_DECORATOR.name)
                        ) ?: useLinkoraTopDecoratorOnDesktop.value
                },
                launch {
                    refreshLinksWorkerTag.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.CURRENT_WORK_MANAGER_WORK_UUID.name)
                    ) ?: refreshLinksWorkerTag.value
                },
                launch {
                    preferencesRepository.readPreferenceValue(
                        preferenceKey = stringPreferencesKey(AppPreferenceType.SERVER_CORRELATION.name)
                    ).let {
                        if (it != null) {
                            correlation = Json.decodeFromString<Correlation>(it)
                        } else {
                            preferencesRepository.changePreferenceValue(
                                preferenceKey = stringPreferencesKey(AppPreferenceType.SERVER_CORRELATION.name),
                                newValue = Json.encodeToString(correlation)
                            )
                        }
                    }
                },
                launch {
                    lastSyncedWithServer = preferencesRepository.readPreferenceValue(
                        preferenceKey = longPreferencesKey(AppPreferenceType.LAST_TIME_STAMP_SYNCED_WITH_SERVER.name)
                    ) ?: lastSyncedWithServer
                },
                launch {
                    showVideoTagOnUIIfApplicable.value = preferencesRepository.readPreferenceValue(
                        preferenceKey = booleanPreferencesKey(AppPreferenceType.SHOW_VIDEO_TAG_IF_APPLICABLE.name)
                    ) ?: showVideoTagOnUIIfApplicable.value
                }
            ).joinAll()
        }
    }
}