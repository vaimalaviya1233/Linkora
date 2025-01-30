package com.sakethh.linkora.ui.screens.home.panels

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.getRemoteOnlyFailureMsg
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.common.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class SpecificPanelManagerScreenVM(
    private val foldersRepo: LocalFoldersRepo, private val localPanelsRepo: LocalPanelsRepo,
    private val preferencesRepository: PreferencesRepository,
    initData: Boolean = true
) : ViewModel() {
    private val _rootFolders = MutableStateFlow(emptyList<Folder>())
    val rootFolders = _rootFolders.asStateFlow()

    private val _specificPanelFolders = MutableStateFlow(emptyList<PanelFolder>())
    val specificPanelFolders = _specificPanelFolders.asStateFlow()

    companion object {
        private val _selectedPanelData = mutableStateOf(Panel(localId = 0L, panelName = ""))
        val selectedPanelData = _selectedPanelData

        fun updateSelectedPanelData(panel: Panel) {
            _selectedPanelData.value = panel
        }
    }

    init {
        if (initData) {
            updateSpecificPanelManagerScreenData()
        }
    }

    private var specificPanelManagerScreenDataJob: Job? = null

    fun updateSpecificPanelManagerScreenData() {

        specificPanelManagerScreenDataJob?.cancel()

        specificPanelManagerScreenDataJob = viewModelScope.launch {
            foldersRepo.getAllFolders().collectLatest { result ->
                result.onSuccess { success ->
                    val rootFilteredFolders = success.data.filterNot { it.isArchived }

                    localPanelsRepo.getAllTheFoldersFromAPanel(_selectedPanelData.value.localId)
                        .collectLatest { panelFolders ->
                            val filteredRootFolders = rootFilteredFolders.filterNot { rootFolder ->
                                panelFolders.any { it.folderName == rootFolder.name }
                            }

                            _specificPanelFolders.emit(panelFolders)
                            _rootFolders.emit(filteredRootFolders)
                        }
                }.pushSnackbarOnFailure()
            }
        }
    }

    fun addANewFolderInAPanel(panelFolder: PanelFolder) {
        viewModelScope.launch {
            localPanelsRepo.addANewFolderInAPanel(panelFolder).collectLatest {
                it.onSuccess {
                    if (it.isRemoteExecutionSuccessful.not()) {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(message = it.getRemoteOnlyFailureMsg()))
                    }
                }
                it.pushSnackbarOnFailure()
            }
        }
    }

    fun addANewAPanel(panel: Panel, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localPanelsRepo.addaNewPanel(panel).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            message = Localization.Key.PanelCreatedSuccessfully.getLocalizedString()
                                .replaceFirstPlaceHolderWith(panel.panelName) + it.getRemoteOnlyFailureMsg()
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun deleteAPanel(panelId: Long, onCompletion: () -> Unit) {
        viewModelScope.launch {
            if (preferencesRepository.readPreferenceValue(longPreferencesKey(AppPreferenceType.LAST_SELECTED_PANEL_ID.name)) == panelId) {
                preferencesRepository.changePreferenceValue(
                    preferenceKey = longPreferencesKey(
                        AppPreferenceType.LAST_SELECTED_PANEL_ID.name
                    ), newValue = Constants.DEFAULT_PANELS_ID
                )
            }
            localPanelsRepo.deleteAPanel(panelId).collectLatest {
                it.onSuccess {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(message = Localization.Key.DeletedPanelSuccessfully.getLocalizedString() + it.getRemoteOnlyFailureMsg()))
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun renameAPanel(panelId: Long, newName: String, onCompletion: () -> Unit) {
        viewModelScope.launch {
            localPanelsRepo.updateAPanelName(newName, panelId).collectLatest {
                it.onSuccess {
                    pushUIEvent(
                        UIEvent.Type.ShowSnackbar(
                            message = Localization.Key.UpdatedThePanelNameSuccessfully.getLocalizedString()
                                .replaceFirstPlaceHolderWith(newName) + it.getRemoteOnlyFailureMsg()
                        )
                    )
                }.pushSnackbarOnFailure()
            }
        }.invokeOnCompletion {
            onCompletion()
        }
    }

    fun removeAFolderFromAPanel(
        panelId: Long,
        folderId: Long
    ) {
        viewModelScope.launch {
            localPanelsRepo.deleteAFolderFromAPanel(panelId, folderId).collectLatest {
                it.onSuccess {
                    if (it.isRemoteExecutionSuccessful.not()) {
                        pushUIEvent(UIEvent.Type.ShowSnackbar(it.getRemoteOnlyFailureMsg()))
                    }
                }
                it.pushSnackbarOnFailure()
            }
        }
    }
}