package com.sakethh.linkora.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.domain.CurrentFABContext
import com.sakethh.linkora.ui.screens.collections.CollectionDetailPane
import com.sakethh.linkora.ui.screens.collections.CollectionsScreen
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.home.HomeScreen
import com.sakethh.linkora.ui.screens.home.panels.PanelsManagerScreen
import com.sakethh.linkora.ui.screens.home.panels.SpecificPanelManagerScreen
import com.sakethh.linkora.ui.screens.onboarding.OnboardingSlidesScreen
import com.sakethh.linkora.ui.screens.search.SearchScreen
import com.sakethh.linkora.ui.screens.settings.SettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.AboutLibrariesScreen
import com.sakethh.linkora.ui.screens.settings.section.AcknowledgementScreen
import com.sakethh.linkora.ui.screens.settings.section.AdvancedSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.GeneralSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.LanguageSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.LayoutSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.ThemeSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.about.AboutScreen
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.snapshots.SnapshotsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerSetupScreen

@Composable
fun LinkoraNavHost(
    startDestination: Navigation.Root,
    onOnboardingComplete: () -> Unit,
    currentFABContext: (CurrentFABContext) -> Unit,
    collectionsScreenVM: CollectionsScreenVM
) {
    val localNavController = LocalNavController.current
    NavHost(
        navController = localNavController, startDestination = startDestination
    ) {
        composable<Navigation.Root.HomeScreen> {
            HomeScreen(currentFABContext)
        }
        composable<Navigation.Root.SearchScreen> {
            SearchScreen(currentFABContext)
        }
        composable<Navigation.Root.CollectionsScreen> {
            CollectionsScreen(
                collectionsScreenVM = collectionsScreenVM, currentFABContext = currentFABContext
            )
        }
        composable<Navigation.Root.SettingsScreen> {
            SettingsScreen(currentFABContext)
        }
        composable<Navigation.Settings.ThemeSettingsScreen> {
            ThemeSettingsScreen()
        }
        composable<Navigation.Settings.GeneralSettingsScreen> {
            GeneralSettingsScreen()
        }
        composable<Navigation.Settings.LayoutSettingsScreen> {
            LayoutSettingsScreen()
        }
        composable<Navigation.Settings.DataSettingsScreen> {
            DataSettingsScreen()
        }
        composable<Navigation.Settings.Data.ServerSetupScreen> {
            ServerSetupScreen()
        }
        composable<Navigation.Settings.LanguageSettingsScreen> {
            LanguageSettingsScreen()
        }
        composable<Navigation.Collection.CollectionDetailPane> {
            CollectionDetailPane(currentFABContext)
        }
        composable<Navigation.Home.PanelsManagerScreen> {
            PanelsManagerScreen(currentFABContext)
        }
        composable<Navigation.Home.SpecificPanelManagerScreen> {
            SpecificPanelManagerScreen()
        }
        composable<Navigation.Settings.AboutScreen> {
            AboutScreen()
        }
        composable<Navigation.Settings.AcknowledgementScreen> {
            AcknowledgementScreen()
        }
        composable<Navigation.Settings.AboutLibraries> {
            AboutLibrariesScreen()
        }
        composable<Navigation.Settings.AdvancedSettingsScreen> {
            AdvancedSettingsScreen()
        }
        composable<Navigation.Settings.Data.SnapshotsScreen> {
            SnapshotsScreen()
        }
        composable<Navigation.Root.OnboardingSlidesScreen> {
            OnboardingSlidesScreen(onOnboardingComplete = onOnboardingComplete)
        }
    }
}