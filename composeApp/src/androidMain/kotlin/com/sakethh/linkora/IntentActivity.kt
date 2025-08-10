package com.sakethh.linkora

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.rememberNavController
import com.sakethh.FileManager
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.di.LinkoraSDKProvider
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.FileType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.SnapshotFormat
import com.sakethh.linkora.domain.model.JSONExportSchema
import com.sakethh.linkora.domain.model.PanelForJSONExportSchema
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.components.AddANewLinkDialogBox
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.linkora.utils.isTablet
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class IntentActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val localConfiguration = LocalConfiguration.current
            val navController = rememberNavController()
            val intentActivityVM = viewModel<IntentActivityVM>(factory = viewModelFactory {
                initializer {
                    IntentActivityVM(
                        localLinksRepo = DependencyContainer.localLinksRepo,
                        localFoldersRepo = DependencyContainer.localFoldersRepo,
                        localPanelsRepo = DependencyContainer.localPanelsRepo,
                        exportDataRepo = DependencyContainer.exportDataRepo,
                        fileManager = LinkoraSDKProvider.getInstance().fileManager
                    )
                }
            })
            CompositionLocalProvider(
                LocalNavController provides navController,
                LocalPlatform provides if (isTablet(localConfiguration)) Platform.Android.Tablet else Platform.Android.Mobile
            ) {
                val context = LocalContext.current
                val darkColors = DarkColors.copy(
                    background = if (AppPreferences.shouldUseAmoledTheme.value) Color(0xFF000000) else DarkColors.background,
                    surface = if (AppPreferences.shouldUseAmoledTheme.value) Color(0xFF000000) else DarkColors.surface
                )
                val shouldUIBeVisible = rememberSaveable {
                    mutableStateOf(true)
                }
                LaunchedEffect(shouldUIBeVisible.value) {
                    shouldUIBeVisible.value.ifNot {
                        if (MainActivity.wasLaunched) {
                            this@IntentActivity.finishAndRemoveTask()
                            return@ifNot
                        }
                        intentActivityVM.createADataSnapshot(onCompletion = {
                            this@IntentActivity.finishAndRemoveTask()
                        })
                    }
                }
                LaunchedEffect(Unit) {
                    UIEvent.uiEvents.collectLatest {
                        if (it is UIEvent.Type.ShowSnackbar) {
                            // Toast isn't supposed to show, but as soon as `shouldUIBeVisible` isn't true, the snackbar won't show either
                            // toast stays on the UI for a while, no matter what happens with the clearance
                            Toast.makeText(context, it.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                val colors = when {
                    AppPreferences.shouldUseDynamicTheming.value && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
                        if (AppPreferences.shouldFollowSystemTheme.value) {
                            if (isSystemInDarkTheme()) dynamicDarkColorScheme(context).copy(
                                background = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(context).background,
                                surface = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(
                                    context
                                ).surface
                            ) else dynamicLightColorScheme(
                                context
                            )
                        } else {
                            if (AppPreferences.shouldUseForceDarkTheme.value) dynamicDarkColorScheme(
                                context
                            ).copy(
                                background = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(context).background,
                                surface = if (AppPreferences.shouldUseAmoledTheme.value) Color(
                                    0xFF000000
                                ) else dynamicDarkColorScheme(
                                    context
                                ).surface
                            ) else dynamicLightColorScheme(context)
                        }
                    }

                    else -> if (AppPreferences.shouldFollowSystemTheme.value) {
                        if (isSystemInDarkTheme()) darkColors else LightColors
                    } else {
                        if (AppPreferences.shouldUseForceDarkTheme.value) darkColors else LightColors
                    }
                }
                LinkoraTheme(
                    typography = AndroidTypography, colorScheme = colors
                ) {
                    AddANewLinkDialogBox(
                        shouldBeVisible = shouldUIBeVisible,
                        screenType = ScreenType.ROOT_SCREEN,
                        currentFolder = null,
                        collectionsScreenVM = CollectionsScreenVM(
                            localFoldersRepo = DependencyContainer.localFoldersRepo,
                            localLinksRepo = DependencyContainer.localLinksRepo,
                            loadNonArchivedRootFoldersOnInit = true,
                            loadArchivedRootFoldersOnInit = false,
                            collectionDetailPaneInfo = null
                        ),
                        url = this@IntentActivity.intent?.getStringExtra(
                            Intent.EXTRA_TEXT
                        ).toString()
                    )
                }
            }
        }
    }
}

class IntentActivityVM(
    private val localLinksRepo: LocalLinksRepo,
    private val localFoldersRepo: LocalFoldersRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val exportDataRepo: ExportDataRepo,
    private val fileManager: FileManager
) : ViewModel() {
    fun createADataSnapshot(onCompletion: () -> Unit) {
        viewModelScope.launch {
            val allLinks = async { localLinksRepo.getAllLinks() }
            val allFolders = async { localFoldersRepo.getAllFoldersAsList() }
            val allPanels = async { localPanelsRepo.getAllThePanelsAsAList() }
            val allPanelFolders = async { localPanelsRepo.getAllThePanelFoldersAsAList() }

            if (AppPreferences.isBackupAutoDeletionEnabled.value) {
                fileManager.deleteAutoBackups(
                    backupLocation = AppPreferences.currentBackupLocation.value,
                    threshold = AppPreferences.backupAutoDeleteThreshold.intValue,
                    onCompletion = {
                        linkoraLog(
                            "Deleted $it snapshot files as the threshold was ${AppPreferences.backupAutoDeleteThreshold.intValue}"
                        )
                    })
            }

            if (AppPreferences.snapshotExportFormatID.value == SnapshotFormat.JSON.id.toString() || AppPreferences.snapshotExportFormatID.value == SnapshotFormat.BOTH.id.toString()) {

                val serializedJsonExportString = JSONExportSchema(
                    schemaVersion = JSONExportSchema.VERSION,
                    links = allLinks.await().map {
                        it.copy(
                            remoteId = null, lastModified = 0
                        )
                    },
                    folders = allFolders.await().map {
                        it.copy(
                            remoteId = null, lastModified = 0
                        )
                    },
                    panels = PanelForJSONExportSchema(panels = allPanels.await().map {
                        it.copy(
                            remoteId = null, lastModified = 0
                        )
                    }, panelFolders = allPanelFolders.await().map {
                        it.copy(
                            remoteId = null, lastModified = 0
                        )
                    }),
                ).run {
                    Json.encodeToString(this)
                }

                fileManager.exportSnapshotData(
                    exportLocation = AppPreferences.currentBackupLocation.value,
                    rawExportString = serializedJsonExportString,
                    fileType = FileType.JSON
                )
            }

            if (AppPreferences.snapshotExportFormatID.value == SnapshotFormat.HTML.id.toString() || AppPreferences.snapshotExportFormatID.value == SnapshotFormat.BOTH.id.toString()) {
                fileManager.exportSnapshotData(
                    rawExportString = exportDataRepo.rawExportDataAsHTML(
                        links = allLinks.await(), folders = allFolders.await()
                    ),
                    fileType = ExportFileType.HTML,
                    exportLocation = AppPreferences.currentBackupLocation.value,
                )
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }
}