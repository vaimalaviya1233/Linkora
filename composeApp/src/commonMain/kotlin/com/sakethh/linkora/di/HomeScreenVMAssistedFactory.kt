package com.sakethh.linkora.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.linkora.ui.screens.home.HomeScreenVM

object HomeScreenVMAssistedFactory {
    fun createForPanelsManagerScreen() = viewModelFactory {
        initializer {
            HomeScreenVM(
                localLinksRepo = DependencyContainer.localLinksRepo,
                localFoldersRepo = DependencyContainer.localFoldersRepo,
                localPanelsRepo = DependencyContainer.localPanelsRepo,
                triggerCollectionOfPanels = true,
                triggerCollectionOfPanelFolders = false,
                preferencesRepository = DependencyContainer.preferencesRepo
            )
        }
    }

    fun createForHomeScreen() = viewModelFactory {
        initializer {
            HomeScreenVM(
                localPanelsRepo = DependencyContainer.localPanelsRepo,
                localLinksRepo = DependencyContainer.localLinksRepo,
                localFoldersRepo = DependencyContainer.localFoldersRepo,
                preferencesRepository = DependencyContainer.preferencesRepo
            )
        }
    }
}