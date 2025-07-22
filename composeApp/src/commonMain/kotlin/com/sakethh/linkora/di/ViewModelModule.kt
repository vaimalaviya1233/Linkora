@file:Suppress("UNCHECKED_CAST")

package com.sakethh.linkora.di

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.ui.AppVM
import com.sakethh.linkora.ui.components.sorting.SortingBtmSheetVM
import com.sakethh.linkora.ui.screens.search.SearchScreenVM
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.section.LanguageSettingsScreenVM
import com.sakethh.linkora.ui.screens.settings.section.about.AboutSettingsScreenVM
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreenVM
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerManagementViewModel
import kotlin.reflect.KClass

@Composable
inline fun <reified T : ViewModel> linkoraViewModel(factory: ViewModelProvider.Factory = LinkoraViewModelFactory): T =
    viewModel(modelClass = T::class, factory = factory)

object LinkoraViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: KClass<T>, extras: CreationExtras): T {
        return when (modelClass) {

            SortingBtmSheetVM::class -> SortingBtmSheetVM(DependencyContainer.preferencesRepo.value)

            AppVM::class -> AppVM(
                remoteSyncRepo = DependencyContainer.remoteSyncRepo.value,
                preferencesRepository = DependencyContainer.preferencesRepo.value,
                networkRepo = DependencyContainer.networkRepo.value,
                linksRepo = DependencyContainer.localLinksRepo.value,
                foldersRepo = DependencyContainer.localFoldersRepo.value,
                localMultiActionRepo = DependencyContainer.localMultiActionRepo.value,
                localPanelsRepo = DependencyContainer.localPanelsRepo.value,
                exportDataRepo = DependencyContainer.exportDataRepo.value
            )


            SearchScreenVM::class -> SearchScreenVM(
                DependencyContainer.localFoldersRepo.value, DependencyContainer.localLinksRepo.value
            )

            SettingsScreenViewModel::class -> SettingsScreenViewModel(DependencyContainer.preferencesRepo.value)
            LanguageSettingsScreenVM::class -> DependencyContainer.localizationRepo.value.let {
                LanguageSettingsScreenVM(it, it)
            }

            AboutSettingsScreenVM::class -> AboutSettingsScreenVM(
                localLinksRepo = DependencyContainer.localLinksRepo.value,
                gitHubReleasesRepo = DependencyContainer.gitHubReleasesRepo.value
            )

            ServerManagementViewModel::class -> ServerManagementViewModel(
                DependencyContainer.networkRepo.value,
                DependencyContainer.preferencesRepo.value,
                DependencyContainer.remoteSyncRepo.value
            )

            DataSettingsScreenVM::class -> DataSettingsScreenVM(
                exportDataRepo = DependencyContainer.exportDataRepo.value,
                importDataRepo = DependencyContainer.importDataRepo.value,
                linksRepo = DependencyContainer.localLinksRepo.value,
                foldersRepo = DependencyContainer.localFoldersRepo.value,
                localPanelsRepo = DependencyContainer.localPanelsRepo.value,
                preferencesRepository = DependencyContainer.preferencesRepo.value,
                pendingSyncQueueRepo = DependencyContainer.pendingSyncQueueRepo.value,
                remoteSyncRepo = DependencyContainer.remoteSyncRepo.value
            )

            else -> error("Not sure how to create an instance of ${modelClass.simpleName}, maybe it's available in *AssistedFactory")
        } as T
    }
}