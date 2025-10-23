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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.rememberNavController
import com.sakethh.linkora.data.local.repository.SnapshotRepoService
import com.sakethh.linkora.di.CollectionScreenVMAssistedFactory
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.di.LinkoraSDK
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.LocalTagsRepo
import com.sakethh.linkora.platform.FileManager
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.components.AddANewLinkDialogBox
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.theme.AndroidTypography
import com.sakethh.linkora.ui.theme.DarkColors
import com.sakethh.linkora.ui.theme.LightColors
import com.sakethh.linkora.ui.theme.LinkoraTheme
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.utils.ifNot
import com.sakethh.linkora.utils.isTablet
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

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
                        fileManager = LinkoraSDK.getInstance().fileManager,
                        localTagsRepo = DependencyContainer.localTagsRepo
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
                var showUI by rememberSaveable {
                    mutableStateOf(true)
                }
                LaunchedEffect(showUI) {
                    showUI.ifNot {
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
                        onDismiss = {
                            showUI = false
                        },
                        screenType = ScreenType.ROOT_SCREEN,
                        currentFolder = null,
                        collectionsScreenVM = viewModel(factory = CollectionScreenVMAssistedFactory.createForIntentActivity()),
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
    private val localTagsRepo: LocalTagsRepo,
    private val exportDataRepo: ExportDataRepo,
    private val fileManager: FileManager
) : ViewModel() {
    private val snapshotRepoService = SnapshotRepoService(
        linksRepo = localLinksRepo,
        foldersRepo = localFoldersRepo,
        localPanelsRepo = localPanelsRepo,
        exportDataRepo = exportDataRepo,
        localTagsRepo = localTagsRepo,
        fileManager = fileManager,
        coroutineScope = viewModelScope
    )

    fun createADataSnapshot(onCompletion: () -> Unit) {
        if (AppPreferences.isBackupAutoDeletionEnabled.value) {
            viewModelScope.launch {
                val allLinks = async { localLinksRepo.getAllLinks() }
                val allFolders = async { localFoldersRepo.getAllFoldersAsList() }
                val allPanels = async { localPanelsRepo.getAllThePanelsAsAList() }
                val allPanelFolders = async { localPanelsRepo.getAllThePanelFoldersAsAList() }
                val allTags = async { localTagsRepo.getAllTagsAsList() }
                val allLinkTagsPairs = async { localTagsRepo.getAllLinkTagsAsList() }
                snapshotRepoService.createAManualSnapshot(
                    allLinks = allLinks.await(),
                    allFolders = allFolders.await(),
                    allPanels = allPanels.await(),
                    allPanelFolders = allPanelFolders.await(),
                    allTags = allTags.await(),
                    allLinkTagsPairs = allLinkTagsPairs.await(),
                    onCompletion = onCompletion
                )
            }
        }
    }
}