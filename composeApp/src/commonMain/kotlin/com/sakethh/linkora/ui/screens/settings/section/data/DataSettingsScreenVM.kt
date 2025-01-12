package com.sakethh.linkora.ui.screens.settings.section.data

import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.common.utils.pushSnackbar
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.onLoading
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class DataSettingsScreenVM(
    private val exportDataRepo: ExportDataRepo
) : ViewModel() {
    val importExportProgressLogs = mutableStateListOf<String>()

    private var importExportJob: Job? = null
    fun exportDataToAFile(exportType: ExportType, onStart: () -> Unit, onCompletion: () -> Unit) {
        importExportJob?.cancel()
        importExportJob = viewModelScope.launch {
            onStart()
            if (exportType == ExportType.JSON) {
                exportDataRepo.exportDataAsJSON()
            } else {
                exportDataRepo.exportDataAsHTMl()
            }.collectLatest {
                it.onLoading { exportLogItem ->
                    importExportProgressLogs.add(exportLogItem)
                }.onSuccess {
                    linkoraLog(it.data)
                }.pushSnackbarOnFailure()
            }
        }
        importExportJob?.invokeOnCompletion { cause ->
            if (cause.isNull()) {
                viewModelScope.pushUIEvent(UIEvent.Type.ShowSnackbar(Localization.Key.ExportedSuccessfully.getLocalizedString()))
            }
            onCompletion()
            cause.pushSnackbar(viewModelScope)
            importExportProgressLogs.clear()
        }
    }

    fun cancelImportExportJob() {
        importExportJob?.cancel()
    }
}