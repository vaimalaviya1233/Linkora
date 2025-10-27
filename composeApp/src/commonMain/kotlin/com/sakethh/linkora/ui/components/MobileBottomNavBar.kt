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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.utils.ifNot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull

@Composable
fun MobileBottomNavBar(
    rootRouteList: List<Navigation.Root>,
    isPerformingStartupSync: Boolean,
    platform: Platform,
    inRootScreen: Boolean?,
    currentRoute: NavDestination?,
    onDoubleTap: (Navigation.Root) -> Unit
) {
    val localNavController = LocalNavController.current
    val mobileBottomNavBarVM: MobileBottomNavBarVM = viewModel()
    val searchNavItemTapTimes by mobileBottomNavBarVM.searchNavItemTapTimes.collectAsStateWithLifecycle()

    var _currentRoute: Navigation.Root? = remember {
        null
    }
    LaunchedEffect(searchNavItemTapTimes == 1) {
        val forceActivateSearch = withTimeoutOrNull(500L) {
            mobileBottomNavBarVM.searchNavItemTapTimes.first { it == 2 }
        }
        if (forceActivateSearch != null) {
            _currentRoute?.let {
                onDoubleTap(it)
            }
        }
        mobileBottomNavBarVM.updateSearchItemTapTimes(0)
    }

    AnimatedVisibility(
        visible = platform == Platform.Android.Mobile && inRootScreen == true && !CollectionsScreenVM.isSelectionEnabled.value,
        exit = slideOutVertically(targetOffsetY = { it }),
        enter = slideInVertically(initialOffsetY = { it })
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().animateContentSize()
        ) {
            if (isPerformingStartupSync) {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            }
            NavigationBar {
                rootRouteList.forEach { navRouteItem ->
                    if (!AppPreferences.isHomeScreenEnabled.value && navRouteItem.toString() == Navigation.Root.HomeScreen.toString()) return@forEach

                    val isSelected = currentRoute?.hasRoute(navRouteItem::class) == true
                    NavigationBarItem(
                        modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand),
                        selected = isSelected,
                        onClick = {
                            mobileBottomNavBarVM.updateSearchItemTapTimes(searchNavItemTapTimes + 1)
                            _currentRoute = navRouteItem
                            isSelected.ifNot {
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

class MobileBottomNavBarVM : ViewModel() {
    private val _searchItemTapTimes = MutableStateFlow(0)
    val searchNavItemTapTimes = _searchItemTapTimes.asStateFlow()

    fun updateSearchItemTapTimes(value: Int) {
        viewModelScope.launch {
            _searchItemTapTimes.emit(value)
        }
    }
}