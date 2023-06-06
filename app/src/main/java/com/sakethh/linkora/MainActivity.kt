package com.sakethh.linkora

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.createDataStore
import androidx.datastore.preferences.preferencesKey
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.google.accompanist.systemuicontroller.rememberSystemUiController
import com.sakethh.linkora.navigation.BottomNavigationBar
import com.sakethh.linkora.navigation.MainNavigation
import com.sakethh.linkora.navigation.NavigationRoutes
import com.sakethh.linkora.navigation.NavigationVM
import com.sakethh.linkora.screens.settings.SettingsScreenVM
import com.sakethh.linkora.ui.theme.LinkoraTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterialApi::class)
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SettingsScreenVM.Settings.dataStore = createDataStore("linkoraDataStore")
        lifecycleScope.launch {
            awaitAll(
                async {
                    SettingsScreenVM.Settings.shouldFollowSystemTheme.value =
                        SettingsScreenVM.Settings.readPreferenceValue(
                            preferenceKey = preferencesKey(SettingsScreenVM.SettingsPreferences.FOLLOW_SYSTEM_THEME.name),
                            dataStore = SettingsScreenVM.Settings.dataStore
                        ) == true
                },
                async {
                    SettingsScreenVM.Settings.shouldDarkThemeBeEnabled.value =
                        SettingsScreenVM.Settings.readPreferenceValue(
                            preferenceKey = preferencesKey(SettingsScreenVM.SettingsPreferences.DARK_THEME.name),
                            dataStore = SettingsScreenVM.Settings.dataStore
                        ) == true
                },
                async {
                    SettingsScreenVM.Settings.shouldFollowDynamicTheming.value =
                        SettingsScreenVM.Settings.readPreferenceValue(
                            preferenceKey = preferencesKey(SettingsScreenVM.SettingsPreferences.DYNAMIC_THEMING.name),
                            dataStore = SettingsScreenVM.Settings.dataStore
                        ) == true
                }
            )
        }
        setContent {
            LinkoraTheme {
                val coroutineScope = rememberCoroutineScope()
                val navController = rememberNavController()
                val bottomBarSheetState =
                    androidx.compose.material.rememberBottomSheetScaffoldState()
                val currentBackStackEntry = navController.currentBackStackEntryAsState()
                val currentRoute = rememberSaveable(
                    inputs = arrayOf(currentBackStackEntry.value?.destination?.route)
                ) {
                    currentBackStackEntry.value?.destination?.route.toString()
                }
                val systemUIController = rememberSystemUiController()
                systemUIController.setNavigationBarColor(NavigationVM.btmNavBarContainerColor.value)
                systemUIController.setStatusBarColor(MaterialTheme.colorScheme.surface)
                LaunchedEffect(key1 = currentRoute) {
                    if (NavigationRoutes.values().any {
                            it.name == currentRoute
                        }) {

                        if (bottomBarSheetState.bottomSheetState.isCollapsed) {
                            coroutineScope.launch {
                                bottomBarSheetState.bottomSheetState.expand()
                            }
                        }
                    } else {
                        if (bottomBarSheetState.bottomSheetState.isExpanded) {
                            coroutineScope.launch {
                                bottomBarSheetState.bottomSheetState.collapse()
                            }
                        }
                    }
                }
                Scaffold(modifier = Modifier.fillMaxSize()) {
                    androidx.compose.material.BottomSheetScaffold(sheetPeekHeight = 0.dp,
                        sheetGesturesEnabled = false,
                        scaffoldState = bottomBarSheetState, sheetContent = {
                            BottomNavigationBar(navController = navController)
                        }) {
                        Box {
                            MainNavigation(navController = navController)
                        }
                    }
                }
            }
        }
    }
}