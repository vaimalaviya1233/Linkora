package com.sakethh.linkora.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.AppVM
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM

@Composable
fun MobileBottomNavBar(rootRouteList:List<Navigation.Root>, appVM: AppVM, platform: Platform, inRootScreen: Boolean?, currentRoute: NavDestination?){
   val localNavController = LocalNavController.current
    AnimatedVisibility(
        visible = platform == Platform.Android.Mobile && inRootScreen == true &&
                !CollectionsScreenVM.isSelectionEnabled.value,
        exit = slideOutVertically(targetOffsetY = { it }),
        enter = slideInVertically(initialOffsetY = { it })
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize()
        ) {
            if (appVM.isPerformingStartupSync.value) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            NavigationBar {
                rootRouteList.forEach { navRouteItem ->
                    if (AppPreferences.isHomeScreenEnabled.value.not()
                        && navRouteItem.toString() == Navigation.Root.HomeScreen.toString()
                    ) return@forEach

                    val isSelected = currentRoute?.hasRoute(navRouteItem::class) == true
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = {
                            isSelected.ifNot {
                                CollectionsScreenVM.resetCollectionDetailPaneInfo()
                                localNavController.navigate(navRouteItem) {
                                    // pop up to home screen on every navigation via bottom nav bar
                                    popUpTo(localNavController.graph.findStartDestination().id) {
                                        saveState = true
                                    }
                                    launchSingleTop = true
                                    restoreState = true

                                }
                            }
                        },
                        icon = {
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
                        },
                        label = {
                            Text(
                                text = navRouteItem.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                maxLines = 1,
                                fontWeight = if (isSelected) FontWeight.SemiBold
                                else FontWeight.Normal
                            )
                        },
                    )
                }
            }
        }
    }
}