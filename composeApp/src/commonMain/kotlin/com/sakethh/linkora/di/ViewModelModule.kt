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

            SortingBtmSheetVM::class -> SortingBtmSheetVM(DependencyContainer.preferencesRepo)

            AppVM::class -> AppVM(
                remoteSyncRepo = DependencyContainer.remoteSyncRepo,
                preferencesRepository = DependencyContainer.preferencesRepo,
                networkRepo = DependencyContainer.networkRepo,
                linksRepo = DependencyContainer.localLinksRepo,
                foldersRepo = DependencyContainer.localFoldersRepo,
                localMultiActionRepo = DependencyContainer.localMultiActionRepo,
                localPanelsRepo = DependencyContainer.localPanelsRepo,
                exportDataRepo = DependencyContainer.exportDataRepo,
                permissionManager = SharedSDK.getInstance().permissionManager,
                fileManager = SharedSDK.getInstance().fileManager,
                dataSyncingNotificationService = SharedSDK.getInstance().dataSyncingNotificationService,
            )


            SearchScreenVM::class -> SearchScreenVM(
                DependencyContainer.localFoldersRepo, DependencyContainer.localLinksRepo
            )

            SettingsScreenViewModel::class -> SettingsScreenViewModel(DependencyContainer.preferencesRepo)
            LanguageSettingsScreenVM::class -> DependencyContainer.localizationRepo.let {
                LanguageSettingsScreenVM(it, it)
            }

            AboutSettingsScreenVM::class -> AboutSettingsScreenVM(
                localLinksRepo = DependencyContainer.localLinksRepo,
                gitHubReleasesRepo = DependencyContainer.gitHubReleasesRepo
            )

            ServerManagementViewModel::class -> ServerManagementViewModel(
                DependencyContainer.networkRepo,
                DependencyContainer.preferencesRepo,
                DependencyContainer.remoteSyncRepo,
                fileManager = SharedSDK.getInstance().fileManager,
                permissionManager = SharedSDK.getInstance().permissionManager,
            )

            DataSettingsScreenVM::class -> DataSettingsScreenVM(
                exportDataRepo = DependencyContainer.exportDataRepo,
                importDataRepo = DependencyContainer.importDataRepo,
                linksRepo = DependencyContainer.localLinksRepo,
                preferencesRepository = DependencyContainer.preferencesRepo,
                remoteSyncRepo = DependencyContainer.remoteSyncRepo,
                nativeUtils = SharedSDK.getInstance().nativeUtils,
                fileManager = SharedSDK.getInstance().fileManager,
                permissionManager = SharedSDK.getInstance().permissionManager,
            )

            else -> error("Not sure how to create an instance of ${modelClass.simpleName}, maybe it's available in *AssistedFactory")
        } as T
    }
}