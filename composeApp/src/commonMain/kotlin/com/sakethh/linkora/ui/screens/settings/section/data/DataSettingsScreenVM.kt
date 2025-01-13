package com.sakethh.linkora.ui.screens.settings.section.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.isStoragePermissionPermittedOnAndroid
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.ifTrue
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.writeRawExportStringToFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DataSettingsScreenVM(
    private val exportDataRepo: ExportDataRepo
) : ViewModel() {
    val importExportProgressLogs = mutableStateListOf<String>()

    private var importExportJob: Job? = null
    fun exportDataToAFile(
        platform: Platform, exportType: ExportType, onStart: () -> Unit, onCompletion: () -> Unit
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
            if (exportType == ExportType.JSON) {
                exportDataRepo.exportDataAsJSON()
            } else {
                exportDataRepo.exportDataAsHTMl()
            }.collectLatest {
                it.onLoading { exportLogItem ->
                    importExportProgressLogs.add(exportLogItem)
                }.onSuccess {
                    try {
                        writeRawExportStringToFile(
                            exportType = exportType, rawExportString = it.data, onCompletion = {
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
}