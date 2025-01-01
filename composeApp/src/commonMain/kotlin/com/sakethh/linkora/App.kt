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
import com.sakethh.linkora.common.network.Network
import com.sakethh.linkora.common.network.repository.NetworkRepoImpl
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.data.local.repository.LocalFoldersRepoImpl
import com.sakethh.linkora.data.remote.repository.RemoteFoldersRepoImpl
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.CollectionsScreen
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.home.HomeScreen
import com.sakethh.linkora.ui.screens.search.SearchScreen
import com.sakethh.linkora.ui.screens.settings.SettingsScreen
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.section.GeneralSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.LanguageSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.LayoutSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.ThemeSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerManagementViewModel
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerSetupScreen
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.linkora.ui.utils.rememberDeserializableObject
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
        CollectionsScreenVM(
            LocalFoldersRepoImpl(
                foldersDao = localDatabase?.foldersDao!!,
                remoteFoldersRepo = RemoteFoldersRepoImpl(
                    httpClient = Network.client,
                    baseUrl = AppPreferences.serverUrl.value,
                    authToken = AppPreferences.serverSecurityToken.value
                ),
                canPushToServer = AppPreferences.canPushToServer()
            )
        )
    })
    val serverManagementViewModel =
        viewModel<ServerManagementViewModel>(factory = genericViewModelFactory {
            ServerManagementViewModel(
                networkRepo = NetworkRepoImpl(Network.client),
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
    val navRouteList = rememberDeserializableObject {
        listOf(
            Navigation.Root.HomeScreen,
            Navigation.Root.SearchScreen,
            Navigation.Root.CollectionsScreen,
            Navigation.Root.SettingsScreen,
        )
    }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination
    Row(modifier = Modifier.fillMaxSize().then(modifier)) {
        if (platform() == Platform.Desktop || platform() == Platform.Android.Tablet) {
            Row {
                Box(modifier = Modifier.fillMaxHeight()) {
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
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
                                                Navigation.Root.HomeScreen -> Icons.Filled.Home
                                                Navigation.Root.SearchScreen -> Icons.Filled.Search
                                                Navigation.Root.CollectionsScreen -> Icons.Filled.Folder
                                                Navigation.Root.SettingsScreen -> Icons.Filled.Settings
                                                else -> return@NavigationRailItem
                                            }
                                        } else {
                                            when (navRouteItem) {
                                                Navigation.Root.HomeScreen -> Icons.Outlined.Home
                                                Navigation.Root.SearchScreen -> Icons.Outlined.Search
                                                Navigation.Root.CollectionsScreen -> Icons.Outlined.Folder
                                                Navigation.Root.SettingsScreen -> Icons.Outlined.Settings
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
                                            Navigation.Root.HomeScreen -> Icons.Filled.Home
                                            Navigation.Root.SearchScreen -> Icons.Filled.Search
                                            Navigation.Root.CollectionsScreen -> Icons.Filled.Folder
                                            Navigation.Root.SettingsScreen -> Icons.Filled.Settings
                                            else -> return@NavigationBarItem
                                        }
                                    } else {
                                        when (navRouteItem) {
                                            Navigation.Root.HomeScreen -> Icons.Outlined.Home
                                            Navigation.Root.SearchScreen -> Icons.Outlined.Search
                                            Navigation.Root.CollectionsScreen -> Icons.Outlined.Folder
                                            Navigation.Root.SettingsScreen -> Icons.Outlined.Settings
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
                startDestination = Navigation.Settings.LanguageSettingsScreen
            ) {
                composable<Navigation.Root.HomeScreen> {
                    HomeScreen()
                }
                composable<Navigation.Root.SearchScreen> {
                    SearchScreen()
                }
                composable<Navigation.Root.CollectionsScreen> {
                    CollectionsScreen(collectionsScreenVM)
                }
                composable<Navigation.Root.SettingsScreen> {
                    SettingsScreen(navController)
                }
                composable<Navigation.Settings.ThemeSettingsScreen> {
                    ThemeSettingsScreen(
                        navController,
                        settingsScreenViewModel
                    )
                }
                composable<Navigation.Settings.GeneralSettingsScreen> {
                    GeneralSettingsScreen(navController, settingsScreenViewModel)
                }
                composable<Navigation.Settings.LayoutSettingsScreen> {
                    LayoutSettingsScreen(
                        navController = navController,
                        settingsScreenViewModel = settingsScreenViewModel
                    )
                }
                composable<Navigation.Settings.DataSettingsScreen> {
                    DataSettingsScreen(navController, serverManagementViewModel)
                }
                composable<Navigation.Settings.Data.ServerSetupScreen> {
                    val initialEntry = rememberSaveable {
                        mutableStateOf(true)
                    }
                    LaunchedEffect(Unit) {
                        if (initialEntry.value) {
                            serverManagementViewModel.resetState()
                            initialEntry.value = false
                        }
                    }
                    ServerSetupScreen(navController, serverManagementViewModel)
                }
                composable<Navigation.Settings.LanguageSettingsScreen> {
                    LanguageSettingsScreen(navController)
                }
            }
        }
    }
}