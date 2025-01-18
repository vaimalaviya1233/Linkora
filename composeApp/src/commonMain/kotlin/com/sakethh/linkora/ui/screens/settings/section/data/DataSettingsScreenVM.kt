package com.sakethh.linkora.ui.screens.settings.section.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.cancelRefreshingLinks
import com.sakethh.isAnyRefreshingScheduled
import com.sakethh.isStoragePermissionPermittedOnAndroid
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.ifTrue
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.domain.repository.ImportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PanelsRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.onRefreshAllLinks
import com.sakethh.pickAValidFileForImporting
import com.sakethh.writeRawExportStringToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DataSettingsScreenVM(
    private val exportDataRepo: ExportDataRepo, private val importDataRepo: ImportDataRepo,
    private val linksRepo: LocalLinksRepo, private val foldersRepo: LocalFoldersRepo,
    private val panelsRepo: PanelsRepo, private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    val importExportProgressLogs = mutableStateListOf<String>()

    private var importExportJob: Job? = null

    val isAnyRefreshingScheduledOnAndroid = mutableStateOf(false)

    init {
        viewModelScope.launch {
            isAnyRefreshingScheduled().collectLatest {
                isAnyRefreshingScheduledOnAndroid.value = it == true
            }
        }
    }

    fun importDataFromAFile(
        importFileType: ImportFileType, onStart: () -> Unit, onCompletion: () -> Unit
    ) {
        importExportJob?.cancel()
        importExportJob = viewModelScope.launch {
            val file = pickAValidFileForImporting(importFileType)
            if (file.isNull()) return@launch
            file as File
            linkoraLog(file.readText())
            if (importFileType == ImportFileType.JSON) {
                importDataRepo.importDataFromAJSONFile(file)
            } else {
                importDataRepo.importDataFromAHTMLFile(file)
            }.onStart {
                onStart()
            }.collectLatest {
                it.onLoading { importLogItem ->
                    importExportProgressLogs.add(importLogItem)
                }.onSuccess {
                    pushUIEvent(UIEvent.Type.ShowSnackbar("Successfully imported the data."))
                }.pushSnackbarOnFailure()
            }
        }
        importExportJob?.invokeOnCompletion { cause ->
            onCompletion()
            cause?.printStackTrace()
            importExportProgressLogs.clear()
        }
    }

    fun exportDataToAFile(
        platform: Platform,
        exportFileType: ExportFileType,
        onStart: () -> Unit,
        onCompletion: () -> Unit
    ) {
        importExportJob?.cancel()
        importExportJob = viewModelScope.launch {
            withContext(Dispatchers.Main) {
                if (platform is Platform.Android) {
                    isStoragePermissionPermittedOnAndroid().ifTrue {
                        onStart()
                    }.ifNot {
                        importExportJob?.cancel()
                    }
                } else {
                    onStart()
                }
            }
            if (exportFileType == ExportFileType.JSON) {
                exportDataRepo.exportDataAsJSON()
            } else {
                exportDataRepo.exportDataAsHTMl()
            }.collectLatest {
                it.onLoading { exportLogItem ->
                    importExportProgressLogs.add(exportLogItem)
                }.onSuccess {
                    try {
                        writeRawExportStringToFile(
                            exportFileType = exportFileType,
                            rawExportString = it.data,
                            onCompletion = {
                                pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.ExportedSuccessfully.getLocalizedString()))
                            })
                    } catch (e: Exception) {
                        e.printStackTrace()
                        pushUIEvent(UIEvent.Type.ShowSnackbar(message = e.message.toString()))
                    }
                }.pushSnackbarOnFailure()
            }
        }
        importExportJob?.invokeOnCompletion { cause ->
            onCompletion()
            cause?.printStackTrace()
            importExportProgressLogs.clear()
        }
    }

    fun cancelImportExportJob() {
        importExportJob?.cancel()
    }

    fun deleteEntireDatabase(onCompletion: () -> Unit) {
        viewModelScope.launch {
            linksRepo.deleteAllLinks()
            foldersRepo.deleteAllFolders()
            panelsRepo.deleteAllPanels()
            panelsRepo.deleteAllPanelFolders()
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    companion object {
        val refreshLinksState = mutableStateOf(
            RefreshLinksState(
                isInRefreshingState = false, currentIteration = 0
            )
        )
        val totalLinksForRefresh = mutableStateOf(0)
    }

    fun refreshAllLinks() {
        viewModelScope.launch {
            onRefreshAllLinks(
                localLinksRepo = linksRepo,
                preferencesRepository = preferencesRepository
            )
        }
    }

    fun cancelRefreshingAllLinks() {
        cancelRefreshingLinks()
    }
}