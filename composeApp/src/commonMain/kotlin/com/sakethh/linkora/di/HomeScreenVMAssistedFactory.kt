package com.sakethh.linkora.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.screens.home.HomeScreenVM

object HomeScreenVMAssistedFactory {
    fun createForPanelsManagerScreen(platform: Platform) = viewModelFactory {
        initializer {
            HomeScreenVM(
                localLinksRepo = DependencyContainer.localLinksRepo,
                localFoldersRepo = DependencyContainer.localFoldersRepo,
                localPanelsRepo = DependencyContainer.localPanelsRepo,
                triggerCollectionOfPanels = true,
                triggerCollectionOfPanelFolders = false,
                preferencesRepository = DependencyContainer.preferencesRepo,
                localTagsRepo = DependencyContainer.localTagsRepo,
                platform = platform
            )
        }
    }

    fun createForHomeScreen(platform: Platform) = viewModelFactory {
        initializer {
            HomeScreenVM(
                localPanelsRepo = DependencyContainer.localPanelsRepo,
                localLinksRepo = DependencyContainer.localLinksRepo,
                localFoldersRepo = DependencyContainer.localFoldersRepo,
                preferencesRepository = DependencyContainer.preferencesRepo,
                localTagsRepo = DependencyContainer.localTagsRepo,
                platform = platform
            )
        }
    }
}