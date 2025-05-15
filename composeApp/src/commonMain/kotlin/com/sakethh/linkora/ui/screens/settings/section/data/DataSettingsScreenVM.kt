package com.sakethh.linkora.ui.screens.settings.section.data

import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.cancelRefreshingLinks
import com.sakethh.isAnyRefreshingScheduled
import com.sakethh.isStorageAccessPermittedOnAndroid
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.duplicate
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.getRemoteOnlyFailureMsg
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.ifTrue
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.domain.repository.ImportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import com.sakethh.linkora.ui.domain.ImportFileSelectionMethod
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.onRefreshAllLinks
import com.sakethh.permittedToShowNotification
import com.sakethh.pickAValidFileForImporting
import com.sakethh.writeRawExportStringToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class DataSettingsScreenVM(
    private val exportDataRepo: ExportDataRepo,
    private val importDataRepo: ImportDataRepo,
    private val linksRepo: LocalLinksRepo,
    private val foldersRepo: LocalFoldersRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val preferencesRepository: PreferencesRepository,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val remoteSyncRepo: RemoteSyncRepo
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
        importFileType: ImportFileType,
        onStart: () -> Unit,
        onCompletion: () -> Unit,
        importFileSelectionMethod: Pair<ImportFileSelectionMethod, String>
    ) {
        importExportJob?.cancel()
        importExportJob = viewModelScope.launch(Dispatchers.Default) {
            val file =
                if (importFileSelectionMethod.first == ImportFileSelectionMethod.FileLocationString) {
                    File(importFileSelectionMethod.second).let {
                        if (it.exists() && it.extension.lowercase() == importFileType.name.lowercase()) {
                            onStart()
                            it
                        } else if (it.exists() && it.extension.lowercase() != importFileType.name.lowercase()) {
                            UIEvent.pushUIEvent(
                                UIEvent.Type.ShowSnackbar(
                                    Localization.Key.FileTypeNotSupportedOnDesktopImport.getLocalizedString()
                                        .replace(LinkoraPlaceHolder.First.value, it.extension)
                                        .replace(
                                            LinkoraPlaceHolder.Second.value, importFileType.name
                                        )
                                )
                            )
                            return@launch
                        } else {
                            null
                        }
                    }?.duplicate()
                } else {
                    pickAValidFileForImporting(importFileType, onStart = {
                        onStart()
                        importExportProgressLogs.add(Localization.Key.ReadingFile.getLocalizedString())
                    })
                }
            if (file.isNull()) return@launch
            file as File
            if (importFileType == ImportFileType.JSON) {
                importDataRepo.importDataFromAJSONFile(file)
            } else {
                importDataRepo.importDataFromAHTMLFile(file)
            }.collectLatest {
                it.onLoading { importLogItem ->
                    importExportProgressLogs.add(importLogItem)
                }.onSuccess {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.SuccessfullyImportedTheData.getLocalizedString()))
                    file.delete()
                }.onFailure {
                    file.delete()
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
                    isStorageAccessPermittedOnAndroid().ifTrue {
                        onStart()
                    }.ifNot {
                        importExportJob?.cancel()
                    }
                } else {
                    onStart()
                }
            }
            if (exportFileType == ExportFileType.JSON) {
                exportDataRepo.rawExportDataAsJSON()
            } else {
                exportDataRepo.rawExportDataAsHTML()
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

    fun deleteEntireDatabase(deleteEverythingFromRemote: Boolean, onCompletion: () -> Unit) {
        var remoteOperationFailed: Boolean? = null
        viewModelScope.launch {
            remoteSyncRepo.deleteEverything(deleteOnRemote = deleteEverythingFromRemote).collectLatest {
                it.onFailure {
                    remoteOperationFailed = true
                }
                it.onSuccess {
                    remoteOperationFailed = it.isRemoteExecutionSuccessful == false
                }
            }
        }.invokeOnCompletion {
            viewModelScope.launch {
                pushUIEvent(
                    UIEvent.Type.ShowSnackbar(
                        if (remoteOperationFailed == null || remoteOperationFailed == false) Localization.Key.DeletedEntireDataPermanently.getLocalizedString()
                        else Localization.Key.RemoteDataDeletionFailure.getLocalizedString()
                    )
                )
            }
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
            launch {
                permittedToShowNotification()
            }
            launch {
                onRefreshAllLinks(
                    localLinksRepo = linksRepo, preferencesRepository = preferencesRepository
                )
            }
        }
    }

    fun cancelRefreshingAllLinks() {
        cancelRefreshingLinks()
    }

    fun deleteDuplicates(onStart: () -> Unit, onCompletion: () -> Unit) {
        viewModelScope.launch {
            linksRepo.deleteDuplicateLinks().collectLatest {
                it.onSuccess {
                    onCompletion()
                    pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.DeletedDuplicatedLinksSuccessfully.getLocalizedString() + it.getRemoteOnlyFailureMsg()))
                }
                it.onLoading {
                    onStart()
                }
                it.onFailure {
                    onCompletion()
                    pushUIEvent(UIEvent.Type.ShowSnackbar(it))
                }
            }
        }
    }
}