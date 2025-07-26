package com.sakethh.linkora.di

import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.sakethh.linkora.ui.screens.home.panels.SpecificPanelManagerScreenVM

object SpecificPanelManagerScreenVMAssistedFactory {
    fun createForPanelsManagerScreen() = viewModelFactory {
        initializer {
            SpecificPanelManagerScreenVM(
                foldersRepo = DependencyContainer.localFoldersRepo,
                localPanelsRepo = DependencyContainer.localPanelsRepo,
                initData = false,
                preferencesRepository = DependencyContainer.preferencesRepo
            )
        }
    }

    fun createForSpecificPanelManagerScreen() = viewModelFactory {
        initializer {
            SpecificPanelManagerScreenVM(
                foldersRepo = DependencyContainer.localFoldersRepo,
                localPanelsRepo = DependencyContainer.localPanelsRepo,
                preferencesRepository = DependencyContainer.preferencesRepo
            )
        }
    }
}