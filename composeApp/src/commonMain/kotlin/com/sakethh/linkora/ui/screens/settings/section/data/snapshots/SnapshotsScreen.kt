package com.sakethh.linkora.ui.screens.settings.section.data.snapshots

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoDelete
import androidx.compose.material.icons.filled.BackupTable
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.PointerIcon
import androidx.compose.ui.input.pointer.pointerHoverIcon
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.addEdgeToEdgeScaffoldPadding
import com.sakethh.linkora.di.linkoraViewModel
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.settings.SettingComponentParam
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingComponent
import com.sakethh.linkora.ui.screens.settings.common.composables.SettingsSectionScaffold
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreenVM
import com.sakethh.linkora.ui.screens.settings.section.data.ExportLocationType
import com.sakethh.linkora.ui.screens.settings.section.data.components.ToggleButton
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.platform
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnapshotsScreen() {

    val backupLocation = rememberSaveable(AppPreferences.currentBackupLocation.value) {
        mutableStateOf(AppPreferences.currentBackupLocation.value)
    }

    val backupAutoDeleteThreshold =
        rememberSaveable(AppPreferences.backupAutoDeleteThreshold.intValue) {
            mutableIntStateOf(AppPreferences.backupAutoDeleteThreshold.intValue)
        }
    val localFocusManager = LocalFocusManager.current

    val isBackupAutoDeletionEnabled =
        rememberSaveable(AppPreferences.isBackupAutoDeletionEnabled.value) {
            mutableStateOf(AppPreferences.isBackupAutoDeletionEnabled.value)
        }
    val platform = platform()
    val navController = LocalNavController.current
    val coroutineScope = rememberCoroutineScope()
    val dataSettingsScreenVM: DataSettingsScreenVM = linkoraViewModel()
    SettingsSectionScaffold(
        topAppBarText = Navigation.Settings.Data.SnapshotsScreen.toString(),
        navController = navController
    ) { paddingValues, topAppBarScrollBehaviour ->
        LazyColumn(
            modifier = Modifier.animateContentSize().fillMaxSize()
                .addEdgeToEdgeScaffoldPadding(paddingValues)
                .nestedScroll(topAppBarScrollBehaviour.nestedScrollConnection),
            verticalArrangement = Arrangement.spacedBy(30.dp)
        ) {
            item {
                Spacer(modifier = Modifier)
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
            }
            if (AppPreferences.areSnapshotsEnabled.value) {
                item {
                    TextField(
                        supportingText = {
                        if (platform is Platform.Android) {
                            Text(
                                text = "If the selected directory is moved or deleted, backup will silently fail. Make sure the selected directory always exists.",
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }, textStyle = MaterialTheme.typography.titleSmall, trailingIcon = {
                        FilledTonalIconButton(
                            modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                                .pulsateEffect().padding(end = 5.dp), onClick = {
                                dataSettingsScreenVM.changeExportLocation(
                                    exportLocation = backupLocation.value,
                                    platform = platform,
                                    exportLocationType = ExportLocationType.SNAPSHOT
                                )
                            }) {
                            Icon(
                                imageVector = if (platform is Platform.Android) Icons.Default.FolderOpen else Icons.Default.Save,
                                contentDescription = null
                            )
                        }
                    }, readOnly = platform is Platform.Android, label = {
                        Text(
                            text = "Current backup location",
                            style = MaterialTheme.typography.titleMedium,
                            textAlign = TextAlign.Start,
                        )
                    }, value = backupLocation.value, onValueChange = {
                        backupLocation.value = it
                    }, modifier = Modifier.padding(
                        start = 15.dp,
                        end = 15.dp,
                    ).fillMaxWidth()
                    )
                }
                item {
                    SettingComponent(
                        SettingComponentParam(
                            isIconNeeded = rememberSaveable { mutableStateOf(true) },
                            title = "Enable Auto-Deletion of Old Snapshots",
                            doesDescriptionExists = true,
                            description = "When enabled, the app will automatically delete the oldest snapshots once they exceed the configured limit.",
                            isSwitchNeeded = true,
                            isSwitchEnabled = isBackupAutoDeletionEnabled,
                            onSwitchStateChange = {
                                dataSettingsScreenVM.updateAutoDeletionBackupsState(it)
                            },
                            icon = Icons.Default.AutoDelete,
                            shouldFilledIconBeUsed = rememberSaveable { mutableStateOf(true) })
                    )
                }

                if (isBackupAutoDeletionEnabled.value) {
                    item {
                        TextField(
                            supportingText = {
                                Text(
                                    text = "File limit is exclusive - checked before creating new backups, not after.",
                                    style = MaterialTheme.typography.titleSmall
                                )
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            textStyle = MaterialTheme.typography.titleSmall,
                            trailingIcon = {
                                FilledTonalIconButton(
                                    modifier = Modifier.pointerHoverIcon(icon = PointerIcon.Hand)
                                        .pulsateEffect().padding(end = 5.dp), onClick = {
                                        dataSettingsScreenVM.updateAutoDeletionBackupsThreshold(
                                            backupAutoDeleteThreshold.intValue
                                        )
                                        localFocusManager.clearFocus(force = true)
                                    }) {
                                    Icon(
                                        imageVector = Icons.Default.Save, contentDescription = null
                                    )
                                }
                            },
                            label = {
                                Text(
                                    text = "Auto-delete if snapshots count exceeds limit:",
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Start,
                                )
                            },
                            value = backupAutoDeleteThreshold.intValue.toString(),
                            onValueChange = {
                                backupAutoDeleteThreshold.intValue = try {
                                    it.toInt()
                                } catch (_: Exception) {
                                    0
                                } catch (_: Error) {
                                    0
                                }
                            },
                            modifier = Modifier.padding(start = 15.dp, end = 15.dp).fillMaxWidth()
                        )
                    }
                }
                item {
                    Column(modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp)) {
                        Text(
                            text = "Export As",
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(15.dp))
                        Row(
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
                }
            }
            item {
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
                Spacer(Modifier.height(150.dp))
            }
        }
    }
}