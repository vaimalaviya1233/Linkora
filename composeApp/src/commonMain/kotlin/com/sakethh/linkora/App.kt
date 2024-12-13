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
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakethh.linkora.ui.navigation.NavigationRoute
import com.sakethh.linkora.ui.screens.collections.CollectionsScreen
import com.sakethh.linkora.ui.screens.home.HomeScreen
import com.sakethh.linkora.ui.screens.search.SearchScreen
import com.sakethh.linkora.ui.screens.settings.SettingsScreen
import com.sakethh.linkora.ui.screens.settings.section.ThemeSettingsScreen
import com.sakethh.linkora.utils.rememberObject
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
@Preview
fun App(
    modifier: Modifier = Modifier,
    platform: Platform,
    navController: NavHostController,
    shouldFollowSystemThemeComposableBeVisible: Boolean
) {
    val navRouteList = rememberObject {
        listOf(
            NavigationRoute.HomeScreen,
            NavigationRoute.SearchScreen,
            NavigationRoute.CollectionsScreen,
            NavigationRoute.SettingsScreen,
        )
    }
    val currentRoute = navController.currentBackStackEntryAsState().value?.destination
    Row(modifier = Modifier.fillMaxSize().then(modifier)) {
        if (platform == Platform.Desktop || platform == Platform.Android.Tablet) {
            Row {
                Box(modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.Center) {
                    Column {
                        navRouteList.forEach { navRouteItem ->
                            val isSelected = currentRoute?.hasRoute(navRouteItem::class) == true
                            NavigationRailItem(
                                modifier = Modifier.padding(
                                    start = 15.dp,
                                    end = 15.dp,
                                    top = 15.dp
                                ), selected = isSelected, onClick = {
                                    if (currentRoute?.hasRoute(navRouteItem::class) == false) {
                                        navController.navigate(navRouteItem)
                                    }
                                }, icon = {
                                    Icon(
                                        imageVector = if (isSelected) {
                                            when (navRouteItem) {
                                                NavigationRoute.HomeScreen -> Icons.Filled.Home
                                                NavigationRoute.SearchScreen -> Icons.Filled.Search
                                                NavigationRoute.CollectionsScreen -> Icons.Filled.Folder
                                                NavigationRoute.SettingsScreen -> Icons.Filled.Settings
                                                else -> return@NavigationRailItem
                                            }
                                        } else {
                                            when (navRouteItem) {
                                                NavigationRoute.HomeScreen -> Icons.Outlined.Home
                                                NavigationRoute.SearchScreen -> Icons.Outlined.Search
                                                NavigationRoute.CollectionsScreen -> Icons.Outlined.Folder
                                                NavigationRoute.SettingsScreen -> Icons.Outlined.Settings
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
        Scaffold(modifier = Modifier.fillMaxSize(), bottomBar = {
            if (platform == Platform.Android.Mobile) {
                NavigationBar {
                    navRouteList.forEach { navRouteItem ->
                        val isSelected = currentRoute?.hasRoute(navRouteItem::class) == true
                        NavigationBarItem(selected = isSelected, onClick = {
                            navController.navigate(navRouteItem)
                        }, icon = {
                            Icon(
                                imageVector = if (isSelected) {
                                    when (navRouteItem) {
                                        NavigationRoute.HomeScreen -> Icons.Filled.Home
                                        NavigationRoute.SearchScreen -> Icons.Filled.Search
                                        NavigationRoute.CollectionsScreen -> Icons.Filled.Folder
                                        NavigationRoute.SettingsScreen -> Icons.Filled.Settings
                                        else -> return@NavigationBarItem
                                    }
                                } else {
                                    when (navRouteItem) {
                                        NavigationRoute.HomeScreen -> Icons.Outlined.Home
                                        NavigationRoute.SearchScreen -> Icons.Outlined.Search
                                        NavigationRoute.CollectionsScreen -> Icons.Outlined.Folder
                                        NavigationRoute.SettingsScreen -> Icons.Outlined.Settings
                                        else -> return@NavigationBarItem
                                    }
                                }, contentDescription = null
                            )
                        }, label = {
                            Text(
                                text = if (navRouteItem == NavigationRoute.SettingsScreen) "Settings" else navRouteItem.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1
                            )
                        })
                    }
                }
            }
        }) {
            NavHost(navController = navController, startDestination = NavigationRoute.HomeScreen) {
                composable<NavigationRoute.HomeScreen> {
                    HomeScreen()
                }
                composable<NavigationRoute.SearchScreen> {
                    SearchScreen()
                }
                composable<NavigationRoute.CollectionsScreen> {
                    CollectionsScreen()
                }
                composable<NavigationRoute.SettingsScreen> {
                    SettingsScreen(navController)
                }
                composable<NavigationRoute.ThemeSettingsScreen> {
                    ThemeSettingsScreen(
                        navController,
                        platform,
                        shouldFollowSystemThemeComposableBeVisible
                    )
                }
            }
        }
    }
}