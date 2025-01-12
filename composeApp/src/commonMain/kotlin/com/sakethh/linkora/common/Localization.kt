package com.sakethh.linkora.common

import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.Constants.DEFAULT_APP_LANGUAGE_CODE
import com.sakethh.linkora.common.utils.Constants.DEFAULT_APP_LANGUAGE_NAME
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import kotlinx.coroutines.runBlocking

typealias LocalizedStringKey = String

object Localization {
    private val localizedStrings = mutableStateMapOf<LocalizedStringKey, String>()

    fun loadLocalizedStrings(
        languageCode: String,
        forceLoadDefaultValues: Boolean = false
    ) = runBlocking {
        if (languageCode == DEFAULT_APP_LANGUAGE_CODE && forceLoadDefaultValues.not()) return@runBlocking
        if (AppPreferences.preferredAppLanguageCode.value != languageCode) {
            AppPreferences.preferredAppLanguageName.value =
                if (languageCode == DEFAULT_APP_LANGUAGE_CODE) {
                    DEFAULT_APP_LANGUAGE_NAME
                } else {
                    DependencyContainer.localizationRepo.value.getLanguageNameForTheCode(
                        languageCode
                    )
                }
            AppPreferences.preferredAppLanguageCode.value = languageCode
            DependencyContainer.preferencesRepo.value.changePreferenceValue(
                stringPreferencesKey(AppPreferenceType.APP_LANGUAGE_NAME.name),
                AppPreferences.preferredAppLanguageName.value
            )
            DependencyContainer.preferencesRepo.value.changePreferenceValue(
                stringPreferencesKey(AppPreferenceType.APP_LANGUAGE_CODE.name),
                AppPreferences.preferredAppLanguageCode.value
            )
        }
        Key.entries.forEach {
            localizedStrings[it.toString()] =
                if (languageCode == DEFAULT_APP_LANGUAGE_CODE || forceLoadDefaultValues) {
                it.defaultValue
            } else {
                    DependencyContainer.localizationRepo.value.getLocalizedStringValueFor(
                        it.toString(),
                        languageCode
                    )
                    ?: it.defaultValue
            }
        }
    }

    @Composable
    fun rememberLocalizedString(key: Key): String {
        val localizedString by remember {
            derivedStateOf { localizedStrings[key.toString()] ?: key.defaultValue }
        }
        return localizedString
    }

    fun getLocalizedString(key: Key): String {
        return localizedStrings[key.toString()] ?: key.defaultValue
    }

    enum class Key(val defaultValue: String) {
        Settings(defaultValue = "Settings") {
            override fun toString(): String {
                return "settings"
            }
        },
        Theme(defaultValue = "Theme") {
            override fun toString(): String {
                return "theme"
            }
        },
        General(defaultValue = "General") {
            override fun toString(): String {
                return "general"
            }
        },
        Advanced(defaultValue = "Advanced") {
            override fun toString(): String {
                return "advanced"
            }
        },
        Layout(defaultValue = "Layout") {
            override fun toString(): String {
                return "layout"
            }
        },
        Language(defaultValue = "Language") {
            override fun toString(): String {
                return "language"
            }
        },
        Data(defaultValue = "Data") {
            override fun toString(): String {
                return "data"
            }
        },
        Privacy(defaultValue = "Privacy") {
            override fun toString(): String {
                return "privacy"
            }
        },
        About(defaultValue = "About") {
            override fun toString(): String {
                return "about"
            }
        },
        Acknowledgments(defaultValue = "Acknowledgments") {
            override fun toString(): String {
                return "acknowledgments"
            }
        },
        UseDarkMode(defaultValue = "Use Dark Theme") {
            override fun toString(): String {
                return "use_dark_theme"
            }
        },
        AppLanguage(defaultValue = "App Language") {
            override fun toString(): String {
                return "app_language"
            }
        },
        DisplayingRemoteStrings(defaultValue = "Displaying Remote Strings") {
            override fun toString(): String {
                return "displaying_remote_strings"
            }
        },
        ResetAppLanguage(defaultValue = "Reset App Language") {
            override fun toString(): String {
                return "reset_app_language"
            }
        },
        AvailableLanguages(defaultValue = "Available Languages") {
            override fun toString(): String {
                return "available_languages"
            }
        },
        LoadServerStrings(defaultValue = "Load Server Strings") {
            override fun toString(): String {
                return "load_server_strings"
            }
        },
        LoadCompiledStrings(defaultValue = "Load Compiled Strings") {
            override fun toString(): String {
                return "load_compiled_strings"
            }
        },
        UpdateLanguageStrings(defaultValue = "Update Language Strings") {
            override fun toString(): String {
                return "update_language_strings"
            }
        },
        DownloadLanguageStrings(defaultValue = "Download Language Strings") {
            override fun toString(): String {
                return "download_language_strings"
            }
        },
        RemoveLanguageStrings(defaultValue = "Remove Language Strings") {
            override fun toString(): String {
                return "remove_language_strings"
            }
        },
        DisplayingCompiledStrings(defaultValue = "Displaying Compiled Strings") {
            override fun toString(): String {
                return "displaying_compiled_strings"
            }
        },
        Home(defaultValue = "Home") {
            override fun toString(): String {
                return "home"
            }
        },

        Search(defaultValue = "Search") {
            override fun toString(): String {
                return "search"
            }
        },

        History(defaultValue = "History") {
            override fun toString(): String {
                return "history"
            }
        },

        HistoryLinks(defaultValue = "History Links") {
            override fun toString(): String {
                return "history_links"
            }
        },

        FolderLinks(defaultValue = "Folder Links") {
            override fun toString(): String {
                return "folder_links"
            }
        },

        Collections(defaultValue = "Collections") {
            override fun toString(): String {
                return "collections"
            }
        },

        LinkoraServerSetup(defaultValue = "Linkora Server Setup") {
            override fun toString(): String {
                return "linkora_server_setup"
            }
        },

        CollectionDetailPane(defaultValue = "Collection Detail Pane") {
            override fun toString(): String {
                return "collection_detail_pane"
            }
        },

        CreateANewFolder(defaultValue = "Create A New Folder") {
            override fun toString(): String {
                return "create_a_new_folder"
            }
        },
        CreateANewFolderIn(defaultValue = "Create A New Folder In ${LinkoraPlaceHolder.First.value}") {
            override fun toString(): String {
                return "create_a_new_folder_in"
            }
        },

        Create(defaultValue = "Create") {
            override fun toString(): String {
                return "create"
            }
        },

        AddANewLink(defaultValue = "Add A New Link") {
            override fun toString(): String {
                return "add_a_new_link"
            }
        },

        SavedLinks(defaultValue = "Saved Links") {
            override fun toString(): String {
                return "saved_links"
            }
        },
        AddANewLinkInImportantLinks(defaultValue = "Add a new link in Important Links") {
            override fun toString(): String {
                return "add_a_new_link_in_important_links"
            }
        },
        AddANewLinkInSavedLinks(defaultValue = "Add a new link in Saved Links") {
            override fun toString(): String {
                return "add_a_new_link_in_saved_links"
            }
        },
        AddANewLinkIn(defaultValue = "Add a new link in ${LinkoraPlaceHolder.First.value}") {
            override fun toString(): String {
                return "add_a_new_link_in"
            }
        },
        LinkAddress(defaultValue = "Link Address") {
            override fun toString(): String {
                return "link_address"
            }
        },
        TitleForTheLink(defaultValue = "Title for the link") {
            override fun toString(): String {
                return "title_for_the_link"
            }
        },
        NoteForSavingTheLink(defaultValue = "Note for saving the link") {
            override fun toString(): String {
                return "note_for_saving_the_link"
            }
        },
        AutoDetectTitleIsEnabled(defaultValue = "Auto Detect Title is currently active.") {
            override fun toString(): String {
                return "auto_detect_title_is_enabled"
            }
        },
        DataRetrievalDisabled(defaultValue = "Data retrieval is blocked as the 'Force Save Links without retrieval' feature is currently active.") {
            override fun toString(): String {
                return "data_retrieval_disabled"
            }
        },
        ForceAutoDetectTitle(defaultValue = "Force Auto-Detect Title") {
            override fun toString(): String {
                return "force_auto_detect_title"
            }
        },
        RetryingWithSecondaryUserAgent(defaultValue = "Retrying metadata retrieval using a secondary user agent.") {
            override fun toString(): String {
                return "retrying_with_secondary_user_agent"
            }
        },
        ForceSaveWithoutRetrievingMetadata(defaultValue = "Force Save Without Retrieving Metadata") {
            override fun toString(): String {
                return "force_save_without_retrieving_metadata"
            }
        },
        AddIn(defaultValue = "Add in") {
            override fun toString(): String {
                return "add_in"
            }
        },
        InitialRequestFailed(defaultValue = "he initial request failed.") {
            override fun toString(): String {
                return "initial_request_failed"
            }
        },
        ImportantLinks(defaultValue = "Important Links") {
            override fun toString(): String {
                return "important_links"
            }
        },
        Save(defaultValue = "Save") {
            override fun toString(): String {
                return "save"
            }
        },
        Cancel(defaultValue = "Cancel") {
            override fun toString(): String {
                return "cancel"
            }
        },
        FolderName(defaultValue = "Folder Name") {
            override fun toString(): String {
                return "folder_name"
            }
        },
        NoteForCreatingTheFolder(defaultValue = "Note For Creating The Folder") {
            override fun toString(): String {
                return "note_for_creating_the_folder"
            }
        },
        AllLinks(defaultValue = "All Links") {
            override fun toString(): String {
                return "all_links"
            }
        },
        Links(defaultValue = "Links") {
            override fun toString(): String {
                return "links"
            }
        },
        Archive(defaultValue = "Archive") {
            override fun toString(): String {
                return "archive"
            }
        },
        ArchiveLinks(defaultValue = "Archive Links") {
            override fun toString(): String {
                return "archive_links"
            }
        },
        Folders(defaultValue = "Folders") {
            override fun toString(): String {
                return "folders"
            }
        },
        SuccessfullySavedConnectionDetails(defaultValue = "Successfully saved connection details.") {
            override fun toString(): String {
                return "successfully_saved_connection_details"
            }
        },
        DeletedTheServerConnectionSuccessfully(defaultValue = "Deleted the server connection successfully.") {
            override fun toString(): String {
                return "deleted_the_server_connection_successfully"
            }
        },
        UseInAppBrowser(defaultValue = "Use In-App Browser") {
            override fun toString(): String {
                return "use_in_app_browser"
            }
        },
        UseInAppBrowserDesc(defaultValue = "Enable this to open links within the app; otherwise, your default browser will open when clicking on links.") {
            override fun toString(): String {
                return "use_in_app_browser_desc"
            }
        },
        EnableHomeScreen(defaultValue = "Enable Home Screen") {
            override fun toString(): String {
                return "enable_home_screen"
            }
        },
        EnableHomeScreenDesc(defaultValue = "If this is enabled, Home Screen option will be shown in Bottom Navigation Bar; if this setting is not enabled, Home screen option will NOT be shown.") {
            override fun toString(): String {
                return "enable_home_screen_desc"
            }
        },
        AutoDetectTitle(defaultValue = "Auto-Detect Title") {
            override fun toString(): String {
                return "auto_detect_title"
            }
        },
        AutoDetectTitleDesc(defaultValue = "Note: This may not detect every website.") {
            override fun toString(): String {
                return "auto_detect_title_desc"
            }
        },
        ForceSaveWithoutRetrievingMetadataDesc(defaultValue = "Link will be saved as you save it, nothing gets fetched. Note that this will impact on refreshing links from link menu, link will NOT be refreshed if this is enabled.") {
            override fun toString(): String {
                return "force_save_without_retrieving_metadata_desc"
            }
        },
        ShowAssociatedImageInLinkMenu(defaultValue = "Show associated image in link menu") {
            override fun toString(): String {
                return "show_associated_image_in_link_menu"
            }
        },
        ShowAssociatedImageInLinkMenuDesc(defaultValue = "Enables the display of an associated image within the link menu.") {
            override fun toString(): String {
                return "show_associated_image_in_link_menu_desc"
            }
        },
        AutoCheckForUpdates(defaultValue = "Enables the display of an associated image within the link menu.") {
            override fun toString(): String {
                return "auto_check_for_updates"
            }
        },
        AutoCheckForUpdatesDesc(defaultValue = "Enable to auto-check for updates on app launch. Disable for manual checks.") {
            override fun toString(): String {
                return "auto_check_for_updates_desc"
            }
        },
        ShowDescriptionForSettings(defaultValue = "Show description for Settings") {
            override fun toString(): String {
                return "show_description_for_settings"
            }
        },
        ShowDescriptionForSettingsDesc(defaultValue = "Enable to show detailed descriptions for settings. Disable to show only titles.") {
            override fun toString(): String {
                return "show_description_for_settings_desc"
            }
        },
        ManageConnectedServer(defaultValue = "Manage Connected Server") {
            override fun toString(): String {
                return "manage_connected_server"
            }
        },
        ManageConnectedServerDesc(defaultValue = "Your data is synced with the Linkora server. Tap to manage or disconnect.") {
            override fun toString(): String {
                return "manage_connected_server_desc"
            }
        },
        CurrentlyConnectedTo(defaultValue = "Currently Connected To") {
            override fun toString(): String {
                return "currently_connected_to"
            }
        },
        SyncType(defaultValue = "Sync Type") {
            override fun toString(): String {
                return "sync_type"
            }
        },
        EditServerConfiguration(defaultValue = "Edit server configuration") {
            override fun toString(): String {
                return "edit_server_configuration"
            }
        },
        DeleteTheServerConnection(defaultValue = "Delete the connection") {
            override fun toString(): String {
                return "delete_the_server_connection"
            }
        },
        Configuration(defaultValue = "Configuration") {
            override fun toString(): String {
                return "configuration"
            }
        },
        ServerURL(defaultValue = "Server URL") {
            override fun toString(): String {
                return "server_url"
            }
        },
        ServerSetupInstruction(defaultValue = "Ensure the server is running. If hosted locally, the server URL should include the correct port number. No port is needed if the server is not hosted locally.") {
            override fun toString(): String {
                return "server_setup_instruction"
            }
        },
        SecurityToken(defaultValue = "Security Token") {
            override fun toString(): String {
                return "security_token"
            }
        },
        ServerIsReachable(defaultValue = "Server Exists and Is Reachable!") {
            override fun toString(): String {
                return "server_is_reachable"
            }
        },
        TestServerAvailability(defaultValue = "Test Server Availability") {
            override fun toString(): String {
                return "test_server_availability"
            }
        },
        UseThisConnection(defaultValue = "Use This Connection") {
            override fun toString(): String {
                return "use_this_connection"
            }
        },
        ClientToServer(defaultValue = "Client To Server") {
            override fun toString(): String {
                return "client_to_server"
            }
        },
        ClientToServerDesc(defaultValue = "Client changes are sent to the server, but client is not updated with server changes.") {
            override fun toString(): String {
                return "client_to_server_desc"
            }
        },
        ServerToClient(defaultValue = "Server To Client") {
            override fun toString(): String {
                return "server_to_client"
            }
        },
        ServerToClientDesc(defaultValue = "Server changes are sent to the client, but server is not updated with client changes.") {
            override fun toString(): String {
                return "server_to_client_desc"
            }
        },
        TwoWaySync(defaultValue = "Two-Way Sync") {
            override fun toString(): String {
                return "two_way_sync"
            }
        },
        TwoWaySyncDesc(defaultValue = "Changes are sent both ways: client updates the server, and server updates the client.") {
            override fun toString(): String {
                return "two_way_sync_desc"
            }
        },
        ImportLabel(defaultValue = "Import") {
            override fun toString(): String {
                return "import"
            }
        },
        ExportLabel(defaultValue = "Export") {
            override fun toString(): String {
                return "export"
            }
        },
        ImportUsingJsonFile(defaultValue = "Import using JSON file") {
            override fun toString(): String {
                return "import_using_json_file"
            }
        },
        ImportUsingJsonFileDesc(defaultValue = "Import data from external JSON file based on Linkora Schema.") {
            override fun toString(): String {
                return "import_using_json_file_desc"
            }
        },
        ImportDataFromHtmlFile(defaultValue = "Import data from HTML file") {
            override fun toString(): String {
                return "import_data_from_html_file"
            }
        },
        ImportDataFromHtmlFileDesc(defaultValue = "Import data from an external HTML file that follows the standard bookmarks import/export format.") {
            override fun toString(): String {
                return "import_data_from_html_file_desc"
            }
        },
        ExportDataAsJson(defaultValue = "Export Data as JSON") {
            override fun toString(): String {
                return "export_data_as_json"
            }
        },
        ExportDataAsJsonDesc(defaultValue = "Export All Data to a JSON File") {
            override fun toString(): String {
                return "export_data_as_json_desc"
            }
        },
        ExportDataAsHtml(defaultValue = "Export Data as HTML") {
            override fun toString(): String {
                return "export_data_as_html"
            }
        },
        ExportDataAsHtmlDesc(defaultValue = "Export All Your Data (Excluding Panels) as HTML File") {
            override fun toString(): String {
                return "export_data_as_html_desc"
            }
        },
        Sync(defaultValue = "Sync") {
            override fun toString(): String {
                return "sync"
            }
        },
        ConnectToALinkoraServer(defaultValue = "Connect to a Linkora Server") {
            override fun toString(): String {
                return "connect_to_a_linkora_server"
            }
        },
        ConnectToALinkoraServerDesc(defaultValue = "By connecting to a Linkora server, you can sync your data and access it on any device using the Linkora app.") {
            override fun toString(): String {
                return "connect_to_a_linkora_server_desc"
            }
        },
        DeleteEntireDataPermanently(defaultValue = "Delete entire data permanently") {
            override fun toString(): String {
                return "delete_entire_data_permanently"
            }
        },
        DeleteEntireDataPermanentlyDesc(defaultValue = "Delete all links and folders permanently including archives.") {
            override fun toString(): String {
                return "delete_entire_data_permanently_desc"
            }
        },
        ClearImageCache(defaultValue = "Clear Image Cache") {
            override fun toString(): String {
                return "clear_image_cache"
            }
        },
        ClearImageCacheDesc(defaultValue = "Images are cached by default. Changing the user agent might affect what you see. Clear the cache to resolve it.") {
            override fun toString(): String {
                return "clear_image_cache_desc"
            }
        },
        RefreshAllLinksTitlesAndImages(defaultValue = "Refresh All Links\\' Titles and Images") {
            override fun toString(): String {
                return "refresh_all_links_titles_and_images"
            }
        },
        RefreshAllLinksTitlesAndImagesDesc(defaultValue = "Manually entered titles will be replaced with detected titles.") {
            override fun toString(): String {
                return "refresh_all_links_titles_and_images_desc"
            }
        },
        RefreshingLinks(defaultValue = "Refreshing links…") {
            override fun toString(): String {
                return "refreshing_links"
            }
        },
        RefreshingLinksDesc(defaultValue = "Closing Linkora won\\'t interrupt link refreshing, but newly added links might not be processed.") {
            override fun toString(): String {
                return "refreshing_links_Desc"
            }
        },
        InitialScreenOnLaunch(defaultValue = "Initial Screen on Launch") {
            override fun toString(): String {
                return "initial_screen_on_launch"
            }
        },
        InitialScreenOnLaunchDesc(defaultValue = "Changes made with this option will reflect in the navigation of the initial screen that will open when you launch Linkora.") {
            override fun toString(): String {
                return "initial_screen_on_launch_Desc"
            }
        },
        Confirm(defaultValue = "Confirm") {
            override fun toString(): String {
                return "confirm"
            }
        },
        SelectTheInitialScreen(defaultValue = "Select the initial screen on launch") {
            override fun toString(): String {
                return "select_the_initial_screen_on_launch"
            }
        },
        ShowBorderAroundLinks(defaultValue = "Show Border Around Links") {
            override fun toString(): String {
                return "show_border_around_links"
            }
        },
        ShowTitle(defaultValue = "Show Title") {
            override fun toString(): String {
                return "show_title"
            }
        },
        ShowBaseURL(defaultValue = "Show Base URL") {
            override fun toString(): String {
                return "show_base_url"
            }
        },
        ShowBottomFadedEdge(defaultValue = "Show Bottom Faded Edge") {
            override fun toString(): String {
                return "show_bottom_faded_edge"
            }
        },
        LinkLayoutSettings(defaultValue = "Link Layout Settings") {
            override fun toString(): String {
                return "link_layout_settings"
            }
        },
        ChooseTheLayoutYouLikeBest(defaultValue = "Choose the layout you like best") {
            override fun toString(): String {
                return "choose_the_layout_you_like_best"
            }
        },
        FeedPreview(defaultValue = "Feed Preview") {
            override fun toString(): String {
                return "feed_preview"
            }
        },
        RegularListView(defaultValue = "Regular List View") {
            override fun toString(): String {
                return "regular_list_view"
            }
        },
        TitleOnlyListView(defaultValue = "Title Only List View") {
            override fun toString(): String {
                return "title_only_list_view"
            }
        },
        GridView(defaultValue = "Grid View") {
            override fun toString(): String {
                return "grid_view"
            }
        },
        StaggeredView(defaultValue = "Staggered View") {
            override fun toString(): String {
                return "staggered_view"
            }
        },
        FollowSystemTheme(defaultValue = "Follow System Theme") {
            override fun toString(): String {
                return "follow_system_theme"
            }
        },
        UseDynamicTheming(defaultValue = "Use dynamic theming") {
            override fun toString(): String {
                return "use_dynamic_theming"
            }
        },
        UseDynamicThemingDesc(defaultValue = "Change colour themes within the app based on your wallpaper.") {
            override fun toString(): String {
                return "use_dynamic_theming_desc"
            }
        },
        UseAmoledTheme(defaultValue = "Use Amoled Theme") {
            override fun toString(): String {
                return "use_amoled_theme"
            }
        },
        RetrieveLanguageInfoFromServer(defaultValue = "Retrieve Language Info from Server") {
            override fun toString(): String {
                return "retrieve_language_info_from_server"
            }
        },
        SelectACollection(defaultValue = "Select a Collection") {
            override fun toString(): String {
                return "select_a_collection"
            }
        },
        SelectAPanel(defaultValue = "Select a Panel") {
            override fun toString(): String {
                return "select_a_panel"
            }
        },
        FolderHasBeenCreatedSuccessful(defaultValue = "The folder ${LinkoraPlaceHolder.First.value} has been successfully created.") {
            override fun toString(): String {
                return "folder_has_been_created_successful"
            }
        },
        RemoteExecutionFailed(defaultValue = "Remote execution failed :") {
            override fun toString(): String {
                return "remote_execution_failed"
            }
        },
        SavedAvailableLanguagesInfoLocally(defaultValue = "Saved Available Languages Info locally.") {
            override fun toString(): String {
                return "saved_available_languages_info_locally"
            }
        },
        DeletedTheStringsPack(defaultValue = "Deleted the ${LinkoraPlaceHolder.First.value} strings pack.") {
            override fun toString(): String {
                return "deleted_the_strings_pack"
            }
        },
        DownloadedLanguageStrings(defaultValue = "Downloaded Language Strings for the ${LinkoraPlaceHolder.First.value}.") {
            override fun toString(): String {
                return "downloaded_language_strings"
            }
        },
        Linkora(defaultValue = "Linkora") {
            override fun toString(): String {
                return "linkora"
            }
        },
        LinkoraIsConnectedToAServer(defaultValue = "Linkora is connected to the server; syncing is based on ${LinkoraPlaceHolder.First.value}.") {
            override fun toString(): String {
                return "linkora_is_connected_to_a_server"
            }
        },
        CopiedLinkToClipboard(defaultValue = "Copied Link to Clipboard") {
            override fun toString(): String {
                return "copied_link_to_clipboard"
            }
        },
        FetchingAvailableLanguages(defaultValue = "Fetching Available Languages") {
            override fun toString(): String {
                return "fetching_available_languages"
            }
        },
        DownloadingStrings(defaultValue = "Downloading Strings for ${LinkoraPlaceHolder.First.value}") {
            override fun toString(): String {
                return "downloading_strings"
            }
        },
        CopiedTitleToTheClipboard(defaultValue = "Copied Title to The Clipboard") {
            override fun toString(): String {
                return "copied_title_to_the_clipboard"
            }
        },
        CopiedNoteToTheClipboard(defaultValue = "Copied Note to The Clipboard") {
            override fun toString(): String {
                return "copied_note_to_the_clipboard"
            }
        },
        ViewNote(defaultValue = "View Note") {
            override fun toString(): String {
                return "view_note"
            }
        },
        Rename(defaultValue = "Rename") {
            override fun toString(): String {
                return "rename"
            }
        },
        RefreshImageAndTitle(defaultValue = "Refresh Image And Title") {
            override fun toString(): String {
                return "refresh_image_and_title"
            }
        },
        Refresh(defaultValue = "Refresh") {
            override fun toString(): String {
                return "refresh"
            }
        },
        UnArchive(defaultValue = "Unarchive") {
            override fun toString(): String {
                return "unarchive"
            }
        },
        DeleteTheNote(defaultValue = "Delete The Note") {
            override fun toString(): String {
                return "delete_the_note"
            }
        },
        MoveToRootFolders(defaultValue = "Move To Root Folders") {
            override fun toString(): String {
                return "move_to_root_folders"
            }
        },
        DeleteTheLink(defaultValue = "Delete the Link") {
            override fun toString(): String {
                return "delete_the_link"
            }
        },
        DeletedTheLink(defaultValue = "Deleted the Link") {
            override fun toString(): String {
                return "deleted_the_link"
            }
        },
        CopyFolder(defaultValue = "Copy Folder") {
            override fun toString(): String {
                return "copy_folder"
            }
        },
        MoveToOtherFolder(defaultValue = "Move To Other Folder") {
            override fun toString(): String {
                return "move_to_other_folder"
            }
        },
        CopyLink(defaultValue = "Copy Link") {
            override fun toString(): String {
                return "copy_link"
            }
        },
        MoveLink(defaultValue = "Move Link") {
            override fun toString(): String {
                return "move_link"
            }
        },
        DeleteTheFolder(defaultValue = "Delete The Folder") {
            override fun toString(): String {
                return "delete_the_folder"
            }
        },
        DeletedTheFolder(defaultValue = "Folder ${LinkoraPlaceHolder.First.value}, all internal folders, and associated links have been successfully deleted.") {
            override fun toString(): String {
                return "deleted_the_folder"
            }
        },
        DeletedTheNoteOfAFolder(defaultValue = "Successfully deleted the note of the ${LinkoraPlaceHolder.First.value}.") {
            override fun toString(): String {
                return "deleted_the_folder_note"
            }
        },
        DeletedTheNoteOfALink(defaultValue = "Successfully deleted the note.") {
            override fun toString(): String {
                return "deleted_the_link_note"
            }
        },
        FolderDeletionLabel(defaultValue = "Deleting this folder will also remove all its subfolders") {
            override fun toString(): String {
                return "folder_deletion_warning"
            }
        },
        AreYouSureDeleteSelectedLinks(defaultValue = "Are you sure you want to delete all selected links?") {
            override fun toString(): String {
                return "are_you_sure_delete_selected_links"
            }
        },
        AreYouSureDeleteLink(defaultValue = "Are you sure you want to delete the link?") {
            override fun toString(): String {
                return "are_you_sure_delete_link"
            }
        },
        AreYouSureDeleteSelectedFolders(defaultValue = "Are you sure you want to delete all selected folders?") {
            override fun toString(): String {
                return "are_you_sure_delete_selected_folders"
            }
        },
        AreYouSureDeleteFolder(defaultValue = "Are you sure you want to delete the folder?") {
            override fun toString(): String {
                return "are_you_sure_delete_folder"
            }
        },
        AreYouSureDeleteSelectedItems(defaultValue = "Are you sure you want to delete all selected items?") {
            override fun toString(): String {
                return "are_you_sure_delete_selected_items"
            }
        },
        AreYouSureDeleteAllFoldersAndLinks(defaultValue = "Are you sure you want to delete all folders and links?") {
            override fun toString(): String {
                return "are_you_sure_delete_all_folders_and_links"
            }
        },
        SavedNote(defaultValue = "Saved Note") {
            override fun toString(): String {
                return "saved_note"
            }
        },
        NoNoteAdded(defaultValue = "You haven't added a note for this.") {
            override fun toString(): String {
                return "no_note_added"
            }
        },
        Delete(defaultValue = "Delete") {
            override fun toString(): String {
                return "delete"
            }
        },
        ArchivedTheFolder(defaultValue = "The folder ${LinkoraPlaceHolder.First.value} has been archived.") {
            override fun toString(): String {
                return "archived_the_folder"
            }
        },
        UnArchivedTheFolder(defaultValue = "The folder ${LinkoraPlaceHolder.First.value} has been unarchived.") {
            override fun toString(): String {
                return "unarchived_the_folder"
            }
        },
        ArchivedTheLink(defaultValue = "Link has been archived.") {
            override fun toString(): String {
                return "archived_the_link"
            }
        },
        ChangeBothNameAndNote(defaultValue = "Change Name and Note") {
            override fun toString(): String {
                return "change_both_name_and_note"
            }
        },
        ChangeNoteOnly(defaultValue = "Change Note") {
            override fun toString(): String {
                return "change_note_only"
            }
        },
        RenameFolder(defaultValue = "Rename Folder ${LinkoraPlaceHolder.First.value}:") {
            override fun toString(): String {
                return "rename_folder"
            }
        },
        ChangeLinkData(defaultValue = "Change Link data:") {
            override fun toString(): String {
                return "change_link_data"
            }
        },
        NewName(defaultValue = "New Name") {
            override fun toString(): String {
                return "new_name"
            }
        },
        NewTitle(defaultValue = "New Title") {
            override fun toString(): String {
                return "new_title"
            }
        },
        NewNote(defaultValue = "New Note") {
            override fun toString(): String {
                return "new_note"
            }
        },
        UpdatedTheNote(defaultValue = "Updated the note successfully.") {
            override fun toString(): String {
                return "updated_the_note"
            }
        },
        UpdatedTheName(defaultValue = "Updated the name successfully.") {
            override fun toString(): String {
                return "updated_the_name"
            }
        },
        UpdatedTheTitle(defaultValue = "Updated the title successfully.") {
            override fun toString(): String {
                return "updated_the_title"
            }
        },
        UpdatedTheFolderData(defaultValue = "Folder data has been updated successfully.") {
            override fun toString(): String {
                return "updated_the_name"
            }
        },
        InvalidLink(defaultValue = "The link provided is invalid. Please check and try again.") {
            override fun toString(): String {
                return "InvalidLink"
            }
        },
        SavedTheLink(defaultValue = "Saved the link successfully.") {
            override fun toString(): String {
                return "saved_the_link"
            }
        },
        DeletionInProgress(defaultValue = "Deletion In Progress..") {
            override fun toString(): String {
                return "deletion_in_progress"
            }
        },
        RemoveALinkFromImpLink(defaultValue = "Remove from Important Links") {
            override fun toString(): String {
                return "remove_a_link_from_imp_link"
            }
        },
        MarkALinkAsImpLink(defaultValue = "Mark Link as Important") {
            override fun toString(): String {
                return "mark_a_link_as_imp_link"
            }
        },
        NewestToOldest(defaultValue = "Newest to Oldest") {
            override fun toString(): String {
                return "newest_to_oldest"
            }
        },
        OldestToNewest(defaultValue = "Oldest to Newest") {
            override fun toString(): String {
                return "oldest_to_newest"
            }
        },
        AToZSequence(defaultValue = "A to Z Sequence") {
            override fun toString(): String {
                return "a_to_z_sequence"
            }
        },
        ZToASequence(defaultValue = "Z to A Sequence") {
            override fun toString(): String {
                return "z_to_a_sequence"
            }
        },
        SortFoldersBy(defaultValue = "Sort folders by") {
            override fun toString(): String {
                return "sort_folders_by"
            }
        },
        SortHistoryLinksBy(defaultValue = "Sort History Links by") {
            override fun toString(): String {
                return "sort_history_links_by"
            }
        },
        SortBy(defaultValue = "Sort by") {
            override fun toString(): String {
                return "sort_by"
            }
        },
        SortSavedLinksBy(defaultValue = "Sort Saved Links by") {
            override fun toString(): String {
                return "sort_saved_links_by"
            }
        },
        SortImportantLinksBy(defaultValue = "Sort Important Links by") {
            override fun toString(): String {
                return "sort_important_links_by"
            }
        },
        SortBasedOn(defaultValue = "Sort Based on") {
            override fun toString(): String {
                return "sort_based_on"
            }
        },
        SearchTitlesToFindLinksAndFolders(defaultValue = "Search titles or notes to find links and folders") {
            override fun toString() = "search_titles_to_find_links_and_folders"
        },
        RegularFolder(defaultValue = "Regular Folder") {
            override fun toString(): String {
                return "regular_folder"
            }
        },
        ArchiveFolder(defaultValue = "Archive Folder") {
            override fun toString(): String {
                return "archive_folder"
            }
        },
        GoodMorning(defaultValue = "Good Morning") {
            override fun toString() = "good_morning"
        },
        GoodAfternoon(defaultValue = "Good Afternoon") {
            override fun toString() = "good_afternoon"
        },
        GoodEvening(defaultValue = "Good Evening") {
            override fun toString() = "good_evening"
        },
        HeyHi(defaultValue = "Hey, hi👋") {
            override fun toString() = "hey_hi"
        },
        Default(defaultValue = "Default") {
            override fun toString() = "default"
        },
        SelectedPanel(defaultValue = "Selected Panel") {
            override fun toString() = "selected_panel"
        },
        AddANewPanel(defaultValue = "Add A New Panel") {
            override fun toString() = "add_a_new_panel"
        },
        Panels(defaultValue = "Panels") {
            override fun toString() = "panels"
        },
        FoldersInThisPanel(defaultValue = "Folders in This Panel") {
            override fun toString(): String {
                return "folders_in_this_panel"
            }
        },
        FoldersThatCanBeAddedToThisPanel(defaultValue = "Folders that can be added to this panel") {
            override fun toString(): String {
                return "folders_that_can_be_added_to_this_panel"
            }
        },
        PanelName(defaultValue = "Panel Name") {
            override fun toString(): String {
                return "panel_name"
            }
        },
        PermanentlyDeleteThePanel(defaultValue = "Permanently Delete Panel") {
            override fun toString() = "permanently_delete_the_panel"
        },
        OnceDeletedThisPanelCannotBeRestored(defaultValue = "Once deleted, this Panel cannot be restored.") {
            override fun toString() = "once_deleted_this_panel_cannot_be_restarted"
        },
        AreYouSureWantToDeleteThePanel(defaultValue = "Are you sure want to delete the panel named ${LinkoraPlaceHolder.First.value}?") {
            override fun toString(): String {
                return "are_you_sure_want_to_delete_the_panel"
            }
        },
        NewNameForPanel(defaultValue = "New Name for Panel") {
            override fun toString(): String {
                return "new_name_for_panel"
            }
        },
        ChangePanelName(defaultValue = "Change Panel Name") {
            override fun toString(): String {
                return "change_panel_name"
            }
        },
        EditPanelName(defaultValue = "Edit ${LinkoraPlaceHolder.First.value} Panel Name") {
            override fun toString(): String {
                return "edit_panel_name"
            }
        },
        RedirectToLatestReleasePage(defaultValue = "Redirect to latest release page") {
            override fun toString() = "redirect_to_latest_release_page"
        },
        NewUpdateIsAvailable(defaultValue = "Linkora just got better, new update is available.") {
            override fun toString() = "new_update_is_available"
        },
        CurrentVersion(defaultValue = "version you're using") {
            override fun toString() = "current_version"
        },
        LatestVersion(defaultValue = "latest version which you should be using") {
            override fun toString() = "latest_version"
        },
        TrackRecentChangesAndUpdatesToLinkora(defaultValue = "Track recent changes and updates to Linkora.") {
            override fun toString() = "track_recent_changes_and_updates_to_linkora"
        },
        Changelog(defaultValue = "Changelog") {
            override fun toString() = "changelog"
        },
        OpenAGithubIssue(defaultValue = "Open a GitHub Issue") {
            override fun toString() = "open_a_github_issue"
        },
        HaveASuggestionCreateAnIssueOnGithubToImproveLinkora(defaultValue = "Have a suggestion? Create an issue on GitHub to improve Linkora.") {
            override fun toString() =
                "have_a_suggestion_create_an_issue_on_github_to_improve_linkora"
        },
        GithubDesc(defaultValue = "The Linkora app, sync server, and localization server are public and open-source—feel free to explore the code.") {
            override fun toString() = "github_desc"
        },
        Github(defaultValue = "Github") {
            override fun toString() = "github"
        },
        Discord(defaultValue = "Discord") {
            override fun toString() = "discord"
        },
        Twitter(defaultValue = "Twitter") {
            override fun toString() = "twitter"
        },
        Development(defaultValue = "Development") {
            override fun toString() = "development"
        },
        Socials(defaultValue = "Socials") {
            override fun toString(): String {
                return "socials"
            }
        },
        YouAreUsingLatestVersionOfLinkora(defaultValue = "You are using latest version of Linkora.") {
            override fun toString() = "you_are_using_latest_version_of_linkora"
        },
        CheckForLatestVersion(defaultValue = "Check for latest version") {
            override fun toString() = "check_for_latest_version"
        },
        RetrievingLatestInformation(defaultValue = "Retrieving latest information, this may take sometime.") {
            override fun toString() = "retrieving_latest_information"
        },
        LinkoraOpenSourceAcknowledgement(defaultValue = "Linkora wouldn't be possible without the following open-source software, libraries.") {
            override fun toString() = "linkora_open_source_acknowledgement"
        },
        UserAgent(defaultValue = "User Agent") {
            override fun toString() = "user_agent"
        },
        UserAgentDesc(defaultValue = "Detects images and titles from webpage meta tags. Detected data may vary based on the agent string used.") {
            override fun toString() = "user_agent_desc"
        },
        LocalizationServerDesc(
            defaultValue = "Linkora’s localization server lets you update strings without updating the app. By default, it uses Linkora’s server.\n" +
                    "\n" +
                    "You can switch to your own server if needed. Changes will reflect in Linkora’s network requests for language or string updates.\n" +
                    "\n" +
                    "Only change this if you’re sure about what you’re doing."
        ) {
            override fun toString() = "localization_server_desc"
        },
        LocalizationServer(defaultValue = "Localization Server") {
            override fun toString() = "localization_server"
        },
        PreparingToExportYourData(defaultValue = "Preparing to export your data...") {
            override fun toString() = "preparing_to_export_your_data"
        },
        CollectingLinksForExport(defaultValue = "Collecting links for export...") {
            override fun toString() = "collecting_links_for_export"
        },
        CollectingFoldersForExport(defaultValue = "Collecting folders for export...") {
            override fun toString() = "collecting_folders_for_export"
        },
        CollectingPanelsForExport(defaultValue = "Collecting panels for export...") {
            override fun toString() = "collecting_panels_for_export"
        },
        CollectingPanelFoldersForExport(defaultValue = "Collecting panel-folders for export...") {
            override fun toString() = "collecting_panel_folders_for_export"
        },
        SerializingCollectedDataForExport(defaultValue = "Serializing the collected data for export...") {
            override fun toString() = "serializing_collected_data_for_export"
        },
        ImportExportScreenTopAppBarDesc(defaultValue = "Stay on this page! DO NOT PANIC IF IT LOOKS STUCK.") {
            override fun toString(): String {
                return "import_export_screen_top_app_bar_desc"
            }
        },
        ExportedSuccessfully(defaultValue = "ExportedSuccessfully") {
            override fun toString(): String {
                return "exported_successfully"
            }
        }
    }
}

