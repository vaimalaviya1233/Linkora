package com.sakethh.linkora.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavController
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.domain.model.CollectionDetailPaneInfo
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import kotlinx.serialization.json.Json

object CollectionScreenVMAssistedFactory {

    fun createForApp() = viewModelFactory {
        initializer {
            CollectionsScreenVM(
                DependencyContainer.localFoldersRepo, DependencyContainer.localLinksRepo
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
                    collectionDetailPaneInfo = if (platform is Platform.Android.Mobile) navController.previousBackStackEntry?.savedStateHandle?.get<String>(
                        Constants.COLLECTION_INFO_SAVED_STATE_HANDLE_KEY,
                    ).run {
                        this as String
                        Json.decodeFromString<CollectionDetailPaneInfo>(this).also {
                            if (it.currentFolder != null) {
                                CollectionsScreenVM.updateCollectionDetailPaneInfo(it)
                            }
                        }
                    } else CollectionsScreenVM.collectionDetailPaneInfo.value)
            }
        }
}