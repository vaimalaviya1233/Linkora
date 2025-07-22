package com.sakethh.linkora.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.linkora.ui.screens.home.panels.SpecificPanelManagerScreenVM

object SpecificPanelManagerScreenVMAssistedFactory {
    fun createForPanelsManagerScreen() = viewModelFactory {
        initializer {
            SpecificPanelManagerScreenVM(
                foldersRepo = DependencyContainer.localFoldersRepo.value,
                localPanelsRepo = DependencyContainer.localPanelsRepo.value,
                initData = false,
                preferencesRepository = DependencyContainer.preferencesRepo.value
            )
        }
    }

    fun createForSpecificPanelManagerScreen() = viewModelFactory {
        initializer {
            SpecificPanelManagerScreenVM(
                foldersRepo = DependencyContainer.localFoldersRepo.value,
                localPanelsRepo = DependencyContainer.localPanelsRepo.value,
                preferencesRepository = DependencyContainer.preferencesRepo.value
            )
        }
    }
}