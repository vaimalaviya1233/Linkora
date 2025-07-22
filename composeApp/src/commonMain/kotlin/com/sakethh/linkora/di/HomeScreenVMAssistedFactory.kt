package com.sakethh.linkora.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.linkora.ui.screens.home.HomeScreenVM

object HomeScreenVMAssistedFactory {
    fun createForPanelsManagerScreen() = viewModelFactory {
        initializer {
            HomeScreenVM(
                localLinksRepo = DependencyContainer.localLinksRepo.value,
                localFoldersRepo = DependencyContainer.localFoldersRepo.value,
                localPanelsRepo = DependencyContainer.localPanelsRepo.value,
                triggerCollectionOfPanels = true,
                triggerCollectionOfPanelFolders = false,
                preferencesRepository = DependencyContainer.preferencesRepo.value
            )
        }
    }

    fun createForHomeScreen() = viewModelFactory {
        initializer {
            HomeScreenVM(
                localPanelsRepo = DependencyContainer.localPanelsRepo.value,
                localLinksRepo = DependencyContainer.localLinksRepo.value,
                localFoldersRepo = DependencyContainer.localFoldersRepo.value,
                preferencesRepository = DependencyContainer.preferencesRepo.value
            )
        }
    }
}