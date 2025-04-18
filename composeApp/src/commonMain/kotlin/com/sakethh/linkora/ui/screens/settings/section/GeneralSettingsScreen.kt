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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.VideoLabel
import androidx.compose.material.icons.outlined.PresentToAll
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.SettingsScreenViewModel
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.platform

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeneralSettingsScreen() {
    val navController = LocalNavController.current
    val settingsScreenViewModel: SettingsScreenViewModel =
        viewModel(factory = genericViewModelFactory {
            SettingsScreenViewModel(DependencyContainer.preferencesRepo.value)
        })
    val showInitialNavigationChangerDialogBox = rememberSaveable {
        mutableStateOf(false)
    }
    val generalSectionData = settingsScreenViewModel.generalSection()
    val platform = platform()
    val isLinkoraTopAppBarEnabled = rememberSaveable {
        mutableStateOf(AppPreferences.useLinkoraTopDecoratorOnDesktop.value)
    }
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.GeneralSettingsScreen.toString(),
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
            item {
                Spacer(Modifier)
            }
            if (platform == Platform.Desktop) {
                item {
                    SettingComponent(
                        SettingComponentParam(
                            title = Localization.Key.TopDecoratorSetting.rememberLocalizedString(),
                            doesDescriptionExists = true,
                            description = Localization.Key.TopDecoratorSettingDesc.rememberLocalizedString(),
                            isSwitchNeeded = true,
                            isSwitchEnabled = isLinkoraTopAppBarEnabled,
                            onSwitchStateChange = {
                                isLinkoraTopAppBarEnabled.value = it
                                settingsScreenViewModel.changeSettingPreferenceValue(
                                    preferenceKey = booleanPreferencesKey(
                                        AppPreferenceType.DESKTOP_TOP_DECORATOR.name
                                    ),
                                    newValue = it
                                )
                            },
                            isIconNeeded = mutableStateOf(true),
                            icon = Icons.Default.VideoLabel
                        )
                    )
                }
            }
            itemsIndexed(generalSectionData) { index, setting ->
                if (generalSectionData.lastIndex == index && platform() !is Platform.Android.Mobile) {
                    return@itemsIndexed
                }
                SettingComponent(setting)
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        title = "Show Onboarding Slides",
                        doesDescriptionExists = false,
                        description = Localization.Key.InitialScreenOnLaunchDesc.rememberLocalizedString(),
                        isSwitchNeeded = false,
                        isSwitchEnabled = mutableStateOf(false),
                        onSwitchStateChange = {
                            navController.navigate(Navigation.Root.OnboardingSlidesScreen)
                        },
                        onAcknowledgmentClick = { uriHandler ->
                        },
                        icon = Icons.Outlined.PresentToAll,
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
                Text(
                    text = Localization.Key.Confirm.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }, dismissButton = {
            OutlinedButton(onClick = {
                showInitialNavigationChangerDialogBox.value = false
            }, modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = Localization.Key.Cancel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }, text = {
            Column {
                listOf(
                    Navigation.Root.HomeScreen,
                    Navigation.Root.SearchScreen,
                    Navigation.Root.CollectionsScreen
                ).forEach {
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
                            text = it.toString()
                        )
                    }
                }
            }
        }, title = {
            Text(
                text = Localization.Key.SelectTheInitialScreen.rememberLocalizedString(),
                style = MaterialTheme.typography.titleMedium,
                fontSize = 16.sp
            )
        })
    }
}