package com.sakethh.linkora.ui.screens.settings.section.data

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BackupTable
import androidx.compose.material.icons.filled.BrokenImage
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CloudSync
import androidx.compose.material.icons.filled.DataObject
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SwitchLeft
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.WbCloudy
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.lifecycle.viewmodel.compose.viewModel
import coil3.ImageLoader
import coil3.compose.LocalPlatformContext
import com.sakethh.PlatformSpecificBackHandler
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.currentSavedServerConfig
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.components.DeleteDialogBox
import com.sakethh.linkora.ui.components.DeleteDialogBoxParam
import com.sakethh.linkora.ui.components.DeleteDialogBoxType
import com.sakethh.linkora.ui.components.InfoCard
import com.sakethh.linkora.ui.domain.ImportFileSelectionMethod
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.screens.settings.section.data.components.ToggleButton
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerManagementBottomSheet
import com.sakethh.linkora.ui.screens.settings.section.data.sync.ServerManagementViewModel
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.platform
import com.sakethh.poppinsFontFamily
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataSettingsScreen() {
    val navController = LocalNavController.current
    val serverManagementViewModel =
        viewModel<ServerManagementViewModel>(factory = genericViewModelFactory {
            ServerManagementViewModel(
                DependencyContainer.networkRepo.value,
                DependencyContainer.preferencesRepo.value,
                DependencyContainer.remoteSyncRepo.value
            )
        })
    val dataSettingsScreenVM: DataSettingsScreenVM = viewModel(factory = genericViewModelFactory {
        DataSettingsScreenVM(
            exportDataRepo = DependencyContainer.exportDataRepo.value,
            importDataRepo = DependencyContainer.importDataRepo.value,
            linksRepo = DependencyContainer.localLinksRepo.value,
            foldersRepo = DependencyContainer.localFoldersRepo.value,
            localPanelsRepo = DependencyContainer.localPanelsRepo.value,
            preferencesRepository = DependencyContainer.preferencesRepo.value,
            pendingSyncQueueRepo = DependencyContainer.pendingSyncQueueRepo.value,
            remoteSyncRepo = DependencyContainer.remoteSyncRepo.value
        )
    })
    val isImportExportProgressUIVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val isForcePushAndPullProgressUIVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val shouldDeleteEntireDialogBoxAppear = rememberSaveable { mutableStateOf(false) }

    val dataOperationTitle = rememberSaveable {
        mutableStateOf("")
    }
    val shouldServerInfoBtmSheetBeVisible = rememberSaveable {
        mutableStateOf(false)
    }
    val serverInfoBtmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()
    val platform = platform()
    val coilPlatformContext = LocalPlatformContext.current
    val importFileSelectionMethod = rememberSaveable {
        mutableStateOf(ImportFileSelectionMethod.FilePicker.name)
    }
    val showFileLocationPickerDialog = rememberSaveable {
        mutableStateOf(false)
    }
    val selectedImportFormat = rememberSaveable {
        mutableStateOf(ImportFileType.JSON.name)
    }
    val showDuplicateDeleteDialogBox = rememberSaveable {
        mutableStateOf(false)
    }
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.DataSettingsScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection), verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                Spacer(modifier = Modifier)
            }
            item {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.ImportLabel),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                )
                if (platform() is Platform.Desktop) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 5.dp)
                    ) {
                        Text(
                            text = Localization.Key.ImportMethodLabel.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = if (importFileSelectionMethod.value == ImportFileSelectionMethod.FileLocationString.name) Localization.Key.FileLocationLabel.rememberLocalizedString() else Localization.Key.FilePickerLabel.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleLarge
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        FilledTonalIconButton(modifier = Modifier.size(24.dp), onClick = {
                            if (importFileSelectionMethod.value == ImportFileSelectionMethod.FileLocationString.name) importFileSelectionMethod.value =
                                ImportFileSelectionMethod.FilePicker.name else importFileSelectionMethod.value =
                                ImportFileSelectionMethod.FileLocationString.name
                        }) {
                            Icon(
                                imageVector = Icons.Default.SwitchLeft,
                                contentDescription = null,
                                modifier = Modifier.rotate(if (importFileSelectionMethod.value == ImportFileSelectionMethod.FileLocationString.name) 0f else 180f)
                            )
                        }
                    }
                }
                if (AppPreferences.isServerConfigured()) {
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.ImportLabelDesc),
                        style = MaterialTheme.typography.titleSmall,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 5.dp),
                    )
                }
            }

            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = Localization.rememberLocalizedString(Localization.Key.ImportUsingJsonFile),
                        doesDescriptionExists = true,
                        description = Localization.rememberLocalizedString(Localization.Key.ImportUsingJsonFileDesc),
                        isSwitchNeeded = false,
                        isSwitchEnabled = rememberSaveable { mutableStateOf(false) },
                        onSwitchStateChange = {
                            if (importFileSelectionMethod.value == ImportFileSelectionMethod.FileLocationString.name) {
                                selectedImportFormat.value = ImportFileType.JSON.name
                                showFileLocationPickerDialog.value = true
                                return@SettingComponentParam
                            }
                            dataOperationTitle.value =
                                Localization.getLocalizedString(Localization.Key.ImportUsingJsonFile)
                            dataSettingsScreenVM.importDataFromAFile(
                                importFileType = ImportFileType.JSON,
                                onStart = {
                                    isImportExportProgressUIVisible.value = true
                                },
                                onCompletion = {
                                    isImportExportProgressUIVisible.value = false
                                },
                                importFileSelectionMethod = ImportFileSelectionMethod.FilePicker to ""
                            )
                        },
                        icon = Icons.Default.DataObject,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = Localization.rememberLocalizedString(Localization.Key.ImportDataFromHtmlFile),
                        doesDescriptionExists = true,
                        description = Localization.rememberLocalizedString(Localization.Key.ImportDataFromHtmlFileDesc),
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {
                            if (importFileSelectionMethod.value == ImportFileSelectionMethod.FileLocationString.name) {
                                selectedImportFormat.value = ImportFileType.HTML.name
                                showFileLocationPickerDialog.value = true
                                return@SettingComponentParam
                            }
                            dataOperationTitle.value =
                                Localization.getLocalizedString(Localization.Key.ImportDataFromHtmlFile)
                            dataSettingsScreenVM.importDataFromAFile(
                                importFileType = ImportFileType.HTML,
                                onStart = {
                                    isImportExportProgressUIVisible.value = true
                                },
                                onCompletion = {
                                    isImportExportProgressUIVisible.value = false
                                },
                                importFileSelectionMethod = ImportFileSelectionMethod.FilePicker to ""
                            )
                        },
                        icon = Icons.Default.Html,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.ExportLabel),
                    style = MaterialTheme.typography.titleMedium,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                )
                if (AppPreferences.isServerConfigured()) {
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.ExportLabelDesc),
                        style = MaterialTheme.typography.titleSmall,
                        lineHeight = 20.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.padding(start = 15.dp, end = 15.dp, top = 5.dp),
                    )
                }
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = Localization.rememberLocalizedString(Localization.Key.ExportDataAsJson),
                        doesDescriptionExists = true,
                        description = Localization.rememberLocalizedString(Localization.Key.ExportDataAsJsonDesc),
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {
                            dataOperationTitle.value =
                                Localization.Key.ExportingDataToJSON.getLocalizedString()
                            dataSettingsScreenVM.exportDataToAFile(
                                platform = platform,
                                exportFileType = ExportFileType.JSON,
                                onStart = {
                                    isImportExportProgressUIVisible.value = true
                                },
                                onCompletion = {
                                    isImportExportProgressUIVisible.value = false
                                })
                        },
                        icon = Icons.Default.DataObject,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                SettingComponent(
                    SettingComponentParam(
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = Localization.rememberLocalizedString(Localization.Key.ExportDataAsHtml),
                        doesDescriptionExists = true,
                        description = Localization.rememberLocalizedString(Localization.Key.ExportDataAsHtmlDesc),
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {
                            dataOperationTitle.value =
                                Localization.Key.ExportingDataToHTML.getLocalizedString()
                            dataSettingsScreenVM.exportDataToAFile(
                                platform = platform,
                                exportFileType = ExportFileType.HTML,
                                onStart = {
                                    isImportExportProgressUIVisible.value = true
                                },
                                onCompletion = {
                                    isImportExportProgressUIVisible.value = false
                                })
                        },
                        icon = Icons.Default.Html,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
            }
            item {
                Text(
                    text = "Snapshots",
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
                        title = "Use snapshots",
                        doesDescriptionExists = true,
                        description = "Links, folders, panels, and panel folders will be auto-exported in your chosen format.",
                        isSwitchNeeded = true,
                        isSwitchEnabled = AppPreferences.areSnapshotsEnabled,
                        onSwitchStateChange = {
                            var isStorageAccessPermitted = false
                            coroutineScope.launch {
                                isStorageAccessPermitted =
                                    com.sakethh.isStorageAccessPermittedOnAndroid()
                            }.invokeOnCompletion { _ ->
                                if (isStorageAccessPermitted.not() && platform is Platform.Android) return@invokeOnCompletion

                                AppPreferences.areSnapshotsEnabled.value = it
                                dataSettingsScreenVM.changeSettingPreferenceValue(
                                    preferenceKey = booleanPreferencesKey(
                                        AppPreferenceType.USE_SNAPSHOTS.name
                                    ), newValue = it
                                )
                            }
                        },
                        icon = Icons.Default.BackupTable,
                        shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                )
                if (AppPreferences.areSnapshotsEnabled.value) {
                    Column(
                        modifier = Modifier.fillMaxWidth().padding(15.dp)
                    ) {
                        Text(
                            text = "Export As:",
                            style = MaterialTheme.typography.titleSmall,
                        )
                        Row(
                            modifier = Modifier.padding(top = 10.dp),
                            horizontalArrangement = Arrangement.spacedBy(5.dp)
                        ) {
                            remember {
                                ExportFileType.entries.map { it.name }
                                    .filter { it != "CER" } + "Both"
                            }.let {
                                it.forEachIndexed { index, exportType ->
                                    val checked =
                                        exportType == AppPreferences.snapshotsExportType.value
                                    ToggleButton(
                                        shape = when (index) {
                                            0 -> RoundedCornerShape(
                                                topStart = 15.dp,
                                                bottomStart = 15.dp,
                                                topEnd = 5.dp,
                                                bottomEnd = 5.dp
                                            )

                                            it.lastIndex -> RoundedCornerShape(
                                                topStart = 5.dp,
                                                bottomStart = 5.dp,
                                                topEnd = 15.dp,
                                                bottomEnd = 15.dp
                                            )

                                            else -> RoundedCornerShape(5.dp)
                                        }, checked = checked, onCheckedChange = {
                                            AppPreferences.snapshotsExportType.value = exportType
                                            dataSettingsScreenVM.changeSettingPreferenceValue(
                                                preferenceKey = stringPreferencesKey(
                                                    AppPreferenceType.SNAPSHOTS_EXPORT_TYPE.name
                                                ), newValue = exportType
                                            )
                                        }) {
                                        Text(
                                            text = exportType,
                                            style = if (checked) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall,
                                            color = if (checked) MaterialTheme.colorScheme.onPrimary else LocalContentColor.current
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Spacer(modifier = Modifier.height(15.dp))
                }
                Text(
                    text = if (platform !is Platform.Android) "Each create, update, or delete action on a link, folder, panel, or panel folder triggers an export. Progress is shown in the side navigation rail." else "Any time you add, edit, or delete a link, folder, panel, or panel folder, Linkora auto-exports in the background.",
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 14.sp,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Start,
                    modifier = Modifier.padding(start = 15.dp, end = 15.dp),
                )
            }
            item {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.Sync),
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
                            title = Localization.rememberLocalizedString(Localization.Key.ConnectToALinkoraServer),
                            doesDescriptionExists = true,
                            description = Localization.rememberLocalizedString(Localization.Key.ConnectToALinkoraServerDesc),
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
                            title = Localization.rememberLocalizedString(Localization.Key.ManageConnectedServer),
                            doesDescriptionExists = true,
                            description = Localization.rememberLocalizedString(Localization.Key.ManageConnectedServerDesc),
                            isSwitchNeeded = false,
                            isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                            onSwitchStateChange = {
                                shouldServerInfoBtmSheetBeVisible.value = true
                                coroutineScope.launch {
                                    serverInfoBtmSheetState.expand()
                                }
                            },
                            icon = Icons.Default.WbCloudy,
                            shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                    )

                }
            }

            if (AppPreferences.isServerConfigured()) {
                item {
                    SettingComponent(
                        SettingComponentParam(
                            isIconNeeded = rememberSaveable { mutableStateOf(true) },
                            title = Localization.Key.InitiateManualSync.rememberLocalizedString(),
                            doesDescriptionExists = true,
                            description = Localization.Key.InitiateManualSyncDesc.rememberLocalizedString(),
                            isSwitchNeeded = false,
                            isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                            onSwitchStateChange = {
                                serverManagementViewModel.saveServerConnectionAndSync(
                                    serverConnection = currentSavedServerConfig(),
                                    timeStampAfter = {
                                        AppPreferences.lastSyncedLocally(serverManagementViewModel.preferencesRepository)
                                    },
                                    onSyncStart = {
                                        isForcePushAndPullProgressUIVisible.value = true
                                    },
                                    onCompletion = {
                                        isForcePushAndPullProgressUIVisible.value = false
                                    })
                            },
                            icon = Icons.Default.Sync,
                            shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
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
                        title = Localization.rememberLocalizedString(Localization.Key.DeleteDuplicateLinksFromAllCollections),
                        doesDescriptionExists = true,
                        description = Localization.rememberLocalizedString(Localization.Key.DeleteDuplicateLinksFromAllCollectionsDesc),
                        isSwitchNeeded = false,
                        isSwitchEnabled = AppPreferences.shouldUseAmoledTheme,
                        onSwitchStateChange = {
                            dataSettingsScreenVM.deleteDuplicates(onStart = {
                                showDuplicateDeleteDialogBox.value = true
                            }, onCompletion = {
                                showDuplicateDeleteDialogBox.value = false
                            })
                        },
                        icon = Icons.Default.Delete,
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
                        isIconNeeded = rememberSaveable { mutableStateOf(true) },
                        title = Localization.rememberLocalizedString(Localization.Key.DeleteEntireDataPermanently),
                        doesDescriptionExists = true,
                        description = Localization.rememberLocalizedString(Localization.Key.DeleteEntireDataPermanentlyDesc),
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
                        title = Localization.rememberLocalizedString(Localization.Key.ClearImageCache),
                        doesDescriptionExists = true,
                        description = Localization.rememberLocalizedString(Localization.Key.ClearImageCacheDesc),
                        isSwitchNeeded = false,
                        isIconNeeded = rememberSaveable {
                            mutableStateOf(true)
                        },
                        icon = Icons.Default.BrokenImage,
                        isSwitchEnabled = rememberSaveable {
                            mutableStateOf(false)
                        },
                        onSwitchStateChange = {
                            ImageLoader(coilPlatformContext).let {
                                it.diskCache?.clear()
                                it.memoryCache?.clear()
                            }
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
                        bottom = if (DataSettingsScreenVM.refreshLinksState.value.isInRefreshingState || dataSettingsScreenVM.isAnyRefreshingScheduledOnAndroid.value) 0.dp else 30.dp
                    ), color = DividerDefaults.color.copy(0.5f)
                )
                Box(
                    modifier = Modifier.fillMaxWidth().wrapContentHeight().animateContentSize()
                ) {
                    if (DataSettingsScreenVM.refreshLinksState.value.isInRefreshingState.not()) {
                        if (platform is Platform.Android && dataSettingsScreenVM.isAnyRefreshingScheduledOnAndroid.value) return@Box
                        SettingComponent(
                            SettingComponentParam(
                                title = Localization.rememberLocalizedString(Localization.Key.RefreshAllLinksTitlesAndImages),
                                doesDescriptionExists = true,
                                description = Localization.rememberLocalizedString(Localization.Key.RefreshAllLinksTitlesAndImagesDesc),
                                isSwitchNeeded = false,
                                isIconNeeded = rememberSaveable {
                                    mutableStateOf(true)
                                },
                                icon = Icons.Default.Refresh,
                                isSwitchEnabled = rememberSaveable {
                                    mutableStateOf(false)
                                },
                                onSwitchStateChange = {
                                    dataSettingsScreenVM.refreshAllLinks()
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
                    if (dataSettingsScreenVM.isAnyRefreshingScheduledOnAndroid.value) {
                        InfoCard(
                            info = Localization.Key.WorkManagerDesc.rememberLocalizedString(),
                            paddingValues = PaddingValues(start = 20.dp, end = 20.dp)
                        )
                    }
                    if (DataSettingsScreenVM.refreshLinksState.value.isInRefreshingState) {
                        Column(
                            modifier = Modifier.fillMaxWidth().wrapContentHeight()
                        ) {
                            Text(
                                text = Localization.rememberLocalizedString(Localization.Key.RefreshingLinks),
                                style = MaterialTheme.typography.titleMedium,
                                modifier = Modifier.padding(
                                    start = 15.dp, end = 15.dp
                                )
                            )
                            if (DataSettingsScreenVM.refreshLinksState.value.currentIteration != 0) {
                                Row(
                                    modifier = Modifier.fillMaxWidth().padding(start = 15.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    LinearProgressIndicator(
                                        modifier = Modifier.fillMaxWidth(0.85f), progress = {
                                            DataSettingsScreenVM.refreshLinksState.value.currentIteration.toFloat() / DataSettingsScreenVM.totalLinksForRefresh.value
                                        })
                                    IconButton(onClick = {
                                        dataSettingsScreenVM.cancelRefreshingAllLinks()
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Cancel,
                                            contentDescription = ""
                                        )
                                    }
                                }
                            }
                            Text(
                                text = Localization.Key.NoOfLinksRefreshed.rememberLocalizedString()
                                    .replace(
                                        LinkoraPlaceHolder.First.value,
                                        DataSettingsScreenVM.refreshLinksState.value.currentIteration.toString()
                                    ).replace(
                                        LinkoraPlaceHolder.Second.value,
                                        DataSettingsScreenVM.totalLinksForRefresh.value.toString()
                                    ),
                                style = MaterialTheme.typography.titleSmall,
                                modifier = Modifier.padding(
                                    start = 15.dp, end = 15.dp
                                ),
                                lineHeight = 18.sp
                            )
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
                                        text = if (platform is Platform.Android) Localization.Key.RefreshingLinksAndroidDesc.rememberLocalizedString() else Localization.Key.RefreshingLinksDesktopDesc.rememberLocalizedString(),
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
    }
    if (showFileLocationPickerDialog.value) {
        val fileLocation = rememberSaveable {
            mutableStateOf("")
        }
        AlertDialog(onDismissRequest = {
            showFileLocationPickerDialog.value = false
        }, confirmButton = {
            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                dataSettingsScreenVM.importDataFromAFile(
                    importFileType = ImportFileType.valueOf(
                    selectedImportFormat.value
                ),
                    onStart = {
                        showFileLocationPickerDialog.value = false
                        isImportExportProgressUIVisible.value = true
                    },
                    onCompletion = {
                        isImportExportProgressUIVisible.value = false
                        showFileLocationPickerDialog.value = false
                    },
                    importFileSelectionMethod = ImportFileSelectionMethod.FileLocationString to fileLocation.value
                )
            }) {
                Text(
                    text = Localization.Key.ImportLabel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }, dismissButton = {
            OutlinedButton(modifier = Modifier.fillMaxWidth(), onClick = {
                showFileLocationPickerDialog.value = false
            }) {
                Text(
                    text = Localization.Key.Cancel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }, text = {
            OutlinedTextField(
                label = {
                Text(
                    text = Localization.Key.FileLocationLabel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1
                )
            },
                value = fileLocation.value,
                onValueChange = {
                    fileLocation.value = it
                },
                textStyle = TextStyle(fontFamily = poppinsFontFamily),
                modifier = Modifier.fillMaxWidth()
            )
        }, title = {
            Text(
                text = Localization.Key.ProvideAValidFileLocation.rememberLocalizedString(),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 18.sp
            )
        })
    }
    ServerManagementBottomSheet(
        serverManagementViewModel = serverManagementViewModel,
        sheetState = serverInfoBtmSheetState,
        isVisible = shouldServerInfoBtmSheetBeVisible,
        navController = navController
    )
    LogsScreen(
        isVisible = isImportExportProgressUIVisible,
        onCancel = {
            dataSettingsScreenVM.cancelImportExportJob()
        },
        logs = dataSettingsScreenVM.importExportProgressLogs,
        operationTitle = dataOperationTitle.value,
        operationDesc = Localization.Key.ImportExportScreenTopAppBarDesc.rememberLocalizedString()
    )
    LogsScreen(
        isVisible = isForcePushAndPullProgressUIVisible,
        onCancel = {
            serverManagementViewModel.cancelServerConnectionAndSync(removeConnection = false)
            isForcePushAndPullProgressUIVisible.value = false
        },
        logs = serverManagementViewModel.dataSyncLogs,
        operationTitle = Localization.Key.SyncingDataLabel.rememberLocalizedString(),
        operationDesc = Localization.Key.InitiateManualSyncDescAlt.rememberLocalizedString()
    )
    DeleteDialogBox(
        deleteDialogBoxParam = DeleteDialogBoxParam(
            shouldDialogBoxAppear = shouldDeleteEntireDialogBoxAppear,
            deleteDialogBoxType = DeleteDialogBoxType.REMOVE_ENTIRE_DATA,
            onDeleteClick = { onCompletion, deleteEverythingFromRemote ->
                dataSettingsScreenVM.deleteEntireDatabase(deleteEverythingFromRemote, onCompletion)
            })
    )
    if (showDuplicateDeleteDialogBox.value) {
        AlertDialog(onDismissRequest = {}, content = {
            Column(
                modifier = Modifier.clip(AlertDialogDefaults.shape)
                    .background(AlertDialogDefaults.containerColor).padding(15.dp),
                verticalArrangement = Arrangement.spacedBy(15.dp)
            ) {
                Text(
                    text = Localization.Key.DeletingDuplicatesLabel.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleLarge,
                    fontSize = 18.sp
                )
                LinearProgressIndicator()
            }
        })
    }
    PlatformSpecificBackHandler {
        if (isImportExportProgressUIVisible.value || showDuplicateDeleteDialogBox.value || shouldDeleteEntireDialogBoxAppear.value) {
            return@PlatformSpecificBackHandler
        } else {
            navController.navigateUp()
        }
    }
}