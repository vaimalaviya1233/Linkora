package com.sakethh.linkora.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hasRoute
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.utils.Constants
import kotlinx.serialization.json.Json

object CollectionScreenVMAssistedFactory {

    fun createForApp(platform: Platform) = viewModelFactory {
        initializer {
            CollectionsScreenVM(
                localFoldersRepo = DependencyContainer.localFoldersRepo,
                localLinksRepo = DependencyContainer.localLinksRepo,
                localTagsRepo = DependencyContainer.localTagsRepo,
                preferencesRepo = DependencyContainer.preferencesRepo,
                platform = platform,
                databaseUtils = DependencyContainer.localDatabaseUtilsImpl
            )
        }
    }

    fun createForCollectionDetailPane(platform: Platform, navController: NavController) =
        viewModelFactory {
            initializer {
                CollectionsScreenVM(
                    localFoldersRepo = DependencyContainer.localFoldersRepo,
                    localLinksRepo = DependencyContainer.localLinksRepo,
                    loadNonArchivedRootFoldersOnInit = false,
                    loadArchivedRootFoldersOnInit = platform is Platform.Android.Mobile,
                    collectionDetailPaneInfo = if (platform is Platform.Android.Mobile || (navController.currentBackStack.value.find {
                            it.destination.hasRoute(
                                Navigation.Root.HomeScreen::class
                            ) || it.destination.hasRoute(Navigation.Root.SearchScreen::class)
                        } != null)) navController.previousBackStackEntry?.savedStateHandle?.get<String>(
                        Constants.COLLECTION_INFO_SAVED_STATE_HANDLE_KEY,
                    ).run {
                        this?.let {
                            Json.decodeFromString<CollectionDetailPaneInfo>(this)
                        }
                    } else null,
                    localTagsRepo = DependencyContainer.localTagsRepo,
                    platform = platform,
                    databaseUtils = DependencyContainer.localDatabaseUtilsImpl)
            }
        }

    // This shouldn't be in the common codebase since it's specific to Android,
    // but the object name seems to conflict when redeclared with the same name.
    // For now, let's keep it here.
    fun createForIntentActivity() = viewModelFactory {
        initializer {
            CollectionsScreenVM(
                localFoldersRepo = DependencyContainer.localFoldersRepo,
                localLinksRepo = DependencyContainer.localLinksRepo,
                databaseUtils = DependencyContainer.localDatabaseUtilsImpl,
                loadNonArchivedRootFoldersOnInit = true,
                loadArchivedRootFoldersOnInit = false,
                collectionDetailPaneInfo = null,
                localTagsRepo = DependencyContainer.localTagsRepo,
                platform = Platform.Android.Mobile
            )
        }
    }
}