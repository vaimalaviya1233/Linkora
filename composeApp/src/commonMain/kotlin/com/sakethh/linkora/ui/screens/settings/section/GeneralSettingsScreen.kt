package com.sakethh.linkora.ui.screens.settings.section

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.UriHandler
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.navigation.NavController
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.ui.navigation.NavigationRoute
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.utils.pulsateEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen(navController: NavController,settingsScreenViewModel: SettingsScreenViewModel) {
    val showInitialNavigationChangerDialogBox = rememberSaveable {
        mutableStateOf(false)
    }

    SettingsSectionScaffold(
        topAppBarText = NavigationRoute.Settings.GeneralSettingsScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                .navigationBarsPadding(),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            items(settingsScreenViewModel.generalSection()) {
                SettingComponent(it)
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        title = "Initial Screen on Launch",
                        doesDescriptionExists = true,
                        description = "Changes made with this option will reflect in the navigation of the initial screen that will open when you launch Linkora.",
                        isSwitchNeeded = false,
                        isSwitchEnabled = mutableStateOf(false),
                        onSwitchStateChange = {
                            showInitialNavigationChangerDialogBox.value = true
                        },
                        onAcknowledgmentClick = { uriHandler: UriHandler ->
                            showInitialNavigationChangerDialogBox.value = true
                        },
                        icon = Icons.AutoMirrored.Filled.Launch,
                        isIconNeeded = mutableStateOf(true),
                    )
                )
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
    }
    if (showInitialNavigationChangerDialogBox.value) {
        val currentlySelectedRoute = rememberSaveable {
            mutableStateOf(AppPreferences.startDestination.value)
        }
        AlertDialog(onDismissRequest = {
            showInitialNavigationChangerDialogBox.value = false
        }, confirmButton = {
            Button(onClick = {
                settingsScreenViewModel.changeSettingPreferenceValue(
                    stringPreferencesKey(
                        AppPreferenceType.INITIAL_ROUTE.name
                    ), currentlySelectedRoute.value.toString()
                )
                AppPreferences.startDestination.value = currentlySelectedRoute.value
                showInitialNavigationChangerDialogBox.value = false
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Confirm", style = MaterialTheme.typography.titleSmall)
            }
        }, dismissButton = {
            OutlinedButton(onClick = {
                showInitialNavigationChangerDialogBox.value = false
            }, modifier = Modifier.fillMaxWidth()) {
                Text(text = "Cancel", style = MaterialTheme.typography.titleSmall)
            }
        }, text = {
            Column {
                listOf(NavigationRoute.Root.HomeScreen, NavigationRoute.Root.SearchScreen, NavigationRoute.Root.CollectionsScreen).forEach {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 5.dp, bottom = 5.dp)
                            .clickable(onClick = {
                                currentlySelectedRoute.value = it.toString()
                            }, indication = null, interactionSource = remember {
                                MutableInteractionSource()
                            })
                            .pulsateEffect(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(
                            selected = currentlySelectedRoute.value == it.toString(),
                            onClick = {
                                currentlySelectedRoute.value = it.toString()
                            })
                        Text(
                            style = if (currentlySelectedRoute.value == it.toString()) MaterialTheme.typography.titleLarge else MaterialTheme.typography.titleSmall,
                            text = it.toString().substringBefore("Screen")
                        )
                    }
                }
            }
        }, title = {
            Text(
                text = "Select the initial screen on launch",
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp
            )
        })
    }
}