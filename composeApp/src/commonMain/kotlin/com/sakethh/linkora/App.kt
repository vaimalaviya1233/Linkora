package com.sakethh.linkora

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakethh.linkora.common.Network
import com.sakethh.linkora.common.NetworkRepoImpl
import com.sakethh.linkora.data.local.repository.LocalFoldersRepoImpl
import com.sakethh.linkora.ui.navigation.NavigationRoute
import com.sakethh.linkora.ui.screens.collections.CollectionsScreen
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.home.HomeScreen
import com.sakethh.linkora.ui.screens.search.SearchScreen
import com.sakethh.linkora.ui.screens.settings.SettingsScreen
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.section.GeneralSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.LayoutSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.ThemeSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.sync.manage.ServerManagementScreen
import com.sakethh.linkora.ui.screens.settings.section.data.sync.setup.ServerSetupScreen
import com.sakethh.linkora.ui.screens.settings.section.data.sync.setup.ServerSetupScreenViewModel
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.linkora.ui.utils.rememberDecodableObject
import com.sakethh.localDatabase
import com.sakethh.platform
import kotlinx.coroutines.flow.collectLatest

@Composable
fun App(
    modifier: Modifier = Modifier,
    navController: NavHostController,
    settingsScreenViewModel: SettingsScreenViewModel
) {
    val snackbarHostState = remember {
        SnackbarHostState()
    }
    val collectionsScreenVM = viewModel<CollectionsScreenVM>(factory = genericViewModelFactory {
        CollectionsScreenVM(LocalFoldersRepoImpl(localDatabase?.foldersDao!!))
    })
    val serverSetupScreenViewModel =
        viewModel<ServerSetupScreenViewModel>(factory = genericViewModelFactory {
            ServerSetupScreenViewModel(
                networkRepo = NetworkRepoImpl(Network.httpClient),
                preferencesRepository = settingsScreenViewModel.preferencesRepository
            )
        })
    LaunchedEffect(Unit) {
        UIEvent.uiEventsReadOnlyChannel.collectLatest {
            when (it) {
                is UIEvent.Type.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(message = it.message)
                }
            }
        }
    }
    val navRouteList = rememberDecodableObject {
        listOf(
            NavigationRoute.Root.HomeScreen,
            NavigationRoute.Root.SearchScreen,
            NavigationRoute.Root.CollectionsScreen,
            NavigationRoute.Root.SettingsScreen,
        )
    }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination
    Row(modifier = Modifier.fillMaxSize().then(modifier)) {
        if (platform() == Platform.Desktop || platform() == Platform.Android.Tablet) {
            Row {
                Box(modifier = Modifier.fillMaxHeight()) {
                    Column(modifier = Modifier.align(Alignment.Center)) {
                        navRouteList.forEach { navRouteItem ->
                            val isSelected = currentRoute?.hasRoute(navRouteItem::class) == true
                            NavigationRailItem(
                                modifier = Modifier.padding(
                                    start = 15.dp, end = 15.dp, top = 15.dp
                                ), selected = isSelected, onClick = {
                                    if (currentRoute?.hasRoute(navRouteItem::class) == false) {
                                        navController.navigate(navRouteItem)
                                    }
                                }, icon = {
                                    Icon(
                                        imageVector = if (isSelected) {
                                            when (navRouteItem) {
                                                NavigationRoute.Root.HomeScreen -> Icons.Filled.Home
                                                NavigationRoute.Root.SearchScreen -> Icons.Filled.Search
                                                NavigationRoute.Root.CollectionsScreen -> Icons.Filled.Folder
                                                NavigationRoute.Root.SettingsScreen -> Icons.Filled.Settings
                                                else -> return@NavigationRailItem
                                            }
                                        } else {
                                            when (navRouteItem) {
                                                NavigationRoute.Root.HomeScreen -> Icons.Outlined.Home
                                                NavigationRoute.Root.SearchScreen -> Icons.Outlined.Search
                                                NavigationRoute.Root.CollectionsScreen -> Icons.Outlined.Folder
                                                NavigationRoute.Root.SettingsScreen -> Icons.Outlined.Settings
                                                else -> return@NavigationRailItem
                                            }
                                        }, contentDescription = null
                                    )
                                }, label = {
                                    Text(
                                        text = navRouteItem.toString(),
                                        style = MaterialTheme.typography.titleSmall,
                                        maxLines = 1
                                    )
                                })
                        }
                    }
                }
                VerticalDivider()
            }
        }
        Scaffold(
            snackbarHost = {
                SnackbarHost(snackbarHostState)
            }, modifier = Modifier.fillMaxSize(), bottomBar = {
                if (platform() == Platform.Android.Mobile) {
                    NavigationBar {
                        navRouteList.forEach { navRouteItem ->
                            val isSelected = currentRoute?.hasRoute(navRouteItem::class) == true
                            NavigationBarItem(selected = isSelected, onClick = {
                                navController.navigate(navRouteItem)
                            }, icon = {
                                Icon(
                                    imageVector = if (isSelected) {
                                        when (navRouteItem) {
                                            NavigationRoute.Root.HomeScreen -> Icons.Filled.Home
                                            NavigationRoute.Root.SearchScreen -> Icons.Filled.Search
                                            NavigationRoute.Root.CollectionsScreen -> Icons.Filled.Folder
                                            NavigationRoute.Root.SettingsScreen -> Icons.Filled.Settings
                                            else -> return@NavigationBarItem
                                        }
                                    } else {
                                        when (navRouteItem) {
                                            NavigationRoute.Root.HomeScreen -> Icons.Outlined.Home
                                            NavigationRoute.Root.SearchScreen -> Icons.Outlined.Search
                                            NavigationRoute.Root.CollectionsScreen -> Icons.Outlined.Folder
                                            NavigationRoute.Root.SettingsScreen -> Icons.Outlined.Settings
                                            else -> return@NavigationBarItem
                                        }
                                    }, contentDescription = null
                                )
                            }, label = {
                                Text(
                                    text = navRouteItem.toString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 1
                                )
                            })
                        }
                    }
                }
            }) {
            NavHost(
                navController = navController,
                startDestination = NavigationRoute.Settings.Data.ServerSetupScreen
            ) {
                composable<NavigationRoute.Root.HomeScreen> {
                    HomeScreen()
                }
                composable<NavigationRoute.Root.SearchScreen> {
                    SearchScreen()
                }
                composable<NavigationRoute.Root.CollectionsScreen> {
                    CollectionsScreen(collectionsScreenVM)
                }
                composable<NavigationRoute.Root.SettingsScreen> {
                    SettingsScreen(navController)
                }
                composable<NavigationRoute.Settings.ThemeSettingsScreen> {
                    ThemeSettingsScreen(
                        navController,
                        settingsScreenViewModel
                    )
                }
                composable<NavigationRoute.Settings.GeneralSettingsScreen> {
                    GeneralSettingsScreen(navController, settingsScreenViewModel)
                }
                composable<NavigationRoute.Settings.LayoutSettingsScreen> {
                    LayoutSettingsScreen(
                        navController = navController,
                        settingsScreenViewModel = settingsScreenViewModel
                    )
                }
                composable<NavigationRoute.Settings.DataSettingsScreen> {
                    DataSettingsScreen(navController)
                }
                composable<NavigationRoute.Settings.Data.ServerSetupScreen> {
                    val initialEntry = rememberSaveable {
                        mutableStateOf(true)
                    }
                    LaunchedEffect(Unit) {
                        if (initialEntry.value) {
                            serverSetupScreenViewModel.resetState()
                            initialEntry.value = false
                        }
                    }
                    ServerSetupScreen(navController, serverSetupScreenViewModel)
                }
                composable<NavigationRoute.Settings.Data.ServerManagementScreen> {
                    ServerManagementScreen(navController)
                }
            }
        }
    }
}