package com.sakethh.linkora.ui.screens.settings.section.data

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerManagementBottomSheet
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerManagementViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen(
    navController: NavController,
    serverManagementViewModel: ServerManagementViewModel
) {
    val importModalBottomSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isPermissionDialogBoxVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val isImportExceptionBoxVisible = rememberSaveable {
        mutableStateOf(false)
    }

    val shouldDeleteEntireDialogBoxAppear = rememberSaveable { mutableStateOf(false) }
    var importBasedOnJsonFormat = rememberSaveable {
        false
    }
    val shouldServerInfoBtmSheetBeVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val serverInfoBtmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.DataSettingsScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection)
                .navigationBarsPadding(), verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                Spacer(modifier = Modifier)
            }
            item {
                Text(
                    text = "LocalizedStrings.import.value",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                )
            }

            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = "LocalizedStrings.importUsingJsonFile.value",
                        doesDescriptionExists = true,
                        description = "LocalizedStrings.importUsingJsonFileDesc.value",
                        isSwitchNeeded = false,
                        isSwitchEnabled = rememberSaveable { mutableStateOf(false) },
                        onSwitchStateChange = {

                        },
                        icon = Icons.Default.DataObject,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = "LocalizedStrings.importDataFromHtmlFile.value",
                        doesDescriptionExists = true,
                        description = "LocalizedStrings.importDataFromHtmlFileDesc.value",
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {

                        },
                        icon = Icons.Default.Html,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                Text(
                    text = "LocalizedStrings.export.value",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                )
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = "LocalizedStrings.exportDataAsJson.value",
                        doesDescriptionExists = true,
                        description = "LocalizedStrings.exportDataAsJsonDesc.value",
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {

                        },
                        icon = Icons.Default.DataObject,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = "LocalizedStrings.exportDataAsHtml.value",
                        doesDescriptionExists = true,
                        description = "LocalizedStrings.exportDataAsHtmlDesc.value",
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {

                        },
                        icon = Icons.Default.Html,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                Spacer(modifier = Modifier)
            }
            item {
                Text(
                    text = "Sync",
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                )
            }

            item {
                if (AppPreferences.isServerConfigured().not()) {
                    SettingComponent(
                        SettingComponentParam(
                            isIconNeeded = rememberSaveable { mutableStateOf(true) },
                            title = "Connect to a Linkora Server",
                            doesDescriptionExists = true,
                            description = "By connecting to a Linkora server, you can sync your data and access it on any device using the Linkora app.",
                            isSwitchNeeded = false,
                            isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                            onSwitchStateChange = {
                                navController.navigate(Navigation.Settings.Data.ServerSetupScreen)
                            },
                            icon = Icons.Default.CloudSync,
                            shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                    )
                } else {
                    SettingComponent(
                        SettingComponentParam(
                            isIconNeeded = rememberSaveable { mutableStateOf(true) },
                            title = "Manage Linkora Server",
                            doesDescriptionExists = true,
                            description = "Your data is synced with the Linkora server. Tap to manage or disconnect.",
                            isSwitchNeeded = false,
                            isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                            onSwitchStateChange = {
                                shouldServerInfoBtmSheetBeVisible.value = true
                                coroutineScope.launch {
                                    serverInfoBtmSheetState.expand()
                                }
                            },
                            icon = Icons.Default.CloudDone,
                            shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) }
                        )
                    )

                }
            }


            item {
                HorizontalDivider(
                    Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                        bottom = 30.dp,
                    ), color = DividerDefaults.color.copy(0.5f)
                )
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = "deleteEntireDataPermanently.value",
                        doesDescriptionExists = true,
                        description = "deleteEntireDataPermanentlyDesc.value",
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {
                            shouldDeleteEntireDialogBoxAppear.value = true
                        },
                        icon = Icons.Default.DeleteForever,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                HorizontalDivider(
                    Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                        bottom = 30.dp,
                    ), color = DividerDefaults.color.copy(0.5f)
                )
                SettingComponent(
                    SettingComponentParam(
                        title = "LocalizedStrings.clearImageCache.value",
                        doesDescriptionExists = true,
                        description = "LocalizedStrings.clearImageCacheDesc.value",
                        isSwitchNeeded = false,
                        isIconNeeded = rememberSaveable {
                            mutableStateOf(true)
                        },
                        icon = Icons.Default.BrokenImage,
                        isSwitchEnabled = rememberSaveable {
                            mutableStateOf(false)
                        },
                        onSwitchStateChange = {

                        },
                        shouldFilledIconBeUsed = rememberSaveable {
                            mutableStateOf(true)
                        })
                )
            }
            item {
                HorizontalDivider(
                    Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                        bottom = if (/* TODO SettingsScreenVM.isAnyRefreshingTaskGoingOn.value*/true) 0.dp else 30.dp
                    ), color = DividerDefaults.color.copy(0.5f)
                )
                Box(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().animateContentSize()
                ) {
                    if (true/*TODO !SettingsScreenVM.isAnyRefreshingTaskGoingOn.value*/) {
                        SettingComponent(
                            SettingComponentParam(
                                title = "LocalizedStrings.refreshAllLinksTitlesAndImages.value",
                                doesDescriptionExists = true,
                                description = "LocalizedStrings.refreshAllLinksTitlesAndImagesDesc.value",
                                isSwitchNeeded = false,
                                isIconNeeded = rememberSaveable {
                                    mutableStateOf(true)
                                },
                                icon = Icons.Default.Refresh,
                                isSwitchEnabled = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                onSwitchStateChange = {

                                },
                                shouldFilledIconBeUsed = rememberSaveable {
                                    mutableStateOf(true)
                                })
                        )
                    }
                }
            }
            item {
                Box(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().animateContentSize()
                ) {
                    if (true/*TODO SettingsScreenVM.isAnyRefreshingTaskGoingOn.value*/) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) {
                            Text(
                                text = "LocalizedStrings.refreshingLinks.value",
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(
                                    start = 15.dp, end = 15.dp
                                )
                            )/*
                            TODO
                            if (RefreshLinksWorker.totalLinksCount.intValue != 0) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LinearProgressIndicator(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f),
                                        progress = {
                                            if (!(successfulRefreshLinkCount.value.toFloat() / RefreshLinksWorker.totalLinksCount.intValue.toFloat()).isNaN() && successfulRefreshLinkCount.value.toFloat() < RefreshLinksWorker.totalLinksCount.intValue.toFloat()) {
                                                successfulRefreshLinkCount.value.toFloat() / RefreshLinksWorker.totalLinksCount.intValue.toFloat()
                                            } else {
                                                0f
                                            }
                                        }
                                    )
                                    IconButton(onClick = {
                                        settingsScreenVM.cancelRefreshAllLinksImagesAndTitlesWork()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = ""
                                        )
                                    }
                                }
                            }
                            if (successfulRefreshLinkCount.collectAsStateWithLifecycle().value == 0 && RefreshLinksWorker.totalLinksCount.intValue == 0) {
                                Spacer(modifier = Modifier.height(15.dp))
                            }
                            Text(
                                text = if (successfulRefreshLinkCount.collectAsStateWithLifecycle().value == 0 && RefreshLinksWorker.totalLinksCount.intValue == 0) LocalizedStrings.workManagerDesc.value else "${successfulRefreshLinkCount.collectAsStateWithLifecycle().value} " + LocalizedStrings.of.value + " ${RefreshLinksWorker.totalLinksCount.intValue} " + LocalizedStrings.linksRefreshed.value,
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(
                                    start = 15.dp,
                                    end = 15.dp
                                ),
                                lineHeight = 18.sp
                            )*/
                            Card(
                                border = BorderStroke(
                                    1.dp, contentColorFor(MaterialTheme.colorScheme.surface)
                                ),
                                colors = CardDefaults.cardColors(containerColor = AlertDialogDefaults.containerColor),
                                modifier = Modifier.fillMaxWidth().padding(
                                    start = 15.dp, end = 15.dp, top = 20.dp
                                )
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(
                                        top = 10.dp, bottom = 10.dp
                                    ), verticalAlignment = Alignment.CenterVertically
                                ) {

                                    Icon(
                                        imageVector = Icons.Outlined.Info,
                                        contentDescription = null,
                                        modifier = Modifier.padding(
                                            start = 10.dp, end = 10.dp
                                        )
                                    )
                                    Text(
                                        text = "LocalizedStrings.refreshingLinksInfo.value",
                                        style = MaterialTheme.typography.titleSmall,
                                        lineHeight = 18.sp,
                                        modifier = Modifier.padding(end = 15.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Spacer(modifier = Modifier.height(100.dp))
            }
        }
        ServerManagementBottomSheet(
            serverManagementViewModel = serverManagementViewModel,
            sheetState = serverInfoBtmSheetState,
            isVisible = shouldServerInfoBtmSheetBeVisible,
            navController = navController
        )
    }
}