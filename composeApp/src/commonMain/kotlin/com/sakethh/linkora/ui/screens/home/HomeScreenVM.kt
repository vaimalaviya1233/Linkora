package com.sakethh.linkora.ui.screens.home

import androidx.compose.runtime.mutableStateOf
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeScreenVM(
    val localFoldersRepo: LocalFoldersRepo,
    val localLinksRepo: LocalLinksRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val preferencesRepository: PreferencesRepository,
    triggerCollectionOfPanels: Boolean = true,
    private val triggerCollectionOfPanelFolders: Boolean = true
) : CollectionsScreenVM(
    localFoldersRepo = localFoldersRepo,
    localLinksRepo = localLinksRepo,
    loadNonArchivedRootFoldersOnInit = false,
    loadArchivedRootFoldersOnInit = false
) {
    val currentPhaseOfTheDay = mutableStateOf("")

    private val _panels = MutableStateFlow(emptyList<Panel>())
    val panels = _panels.asStateFlow()

    private val _panelFolders = MutableStateFlow(emptyList<PanelFolder>())
    val panelFolders = _panelFolders.asStateFlow()

    private val defaultPanelFolders = listOf(
        PanelFolder(
            folderId = Constants.SAVED_LINKS_ID,
            folderName = Localization.Key.SavedLinks.getLocalizedString(),
            connectedPanelId = Constants.DEFAULT_PANELS_ID,
            panelPosition = 0
        ),
        PanelFolder(
            folderId = Constants.IMPORTANT_LINKS_ID,
            folderName = Localization.Key.ImportantLinks.getLocalizedString(),
            connectedPanelId = Constants.DEFAULT_PANELS_ID,
            panelPosition = 0
        ),
    )

    private fun defaultPanel(): Panel {
        return Panel(
            panelName = Localization.Key.Default.getLocalizedString(),
            localId = Constants.DEFAULT_PANELS_ID
        )
    }

    private var panelFoldersJob: Job? = null

    fun updatePanelFolders(panelId: Long) {
        panelFoldersJob?.cancel()

        panelFoldersJob = viewModelScope.launch {

            preferencesRepository.changePreferenceValue(
                preferenceKey = longPreferencesKey(
                    AppPreferenceType.LAST_SELECTED_PANEL_ID.name
                ), newValue = panelId
            )

            if (panelId == Constants.DEFAULT_PANELS_ID) {
                _panelFolders.emit(defaultPanelFolders)
                return@launch
            }

            localPanelsRepo.getAllTheFoldersFromAPanel(panelId).cancellable().collectLatest {
                _panelFolders.emit(it)
            }
        }
    }

    val selectedPanelData = mutableStateOf<Panel?>(null)

    init {
        if (triggerCollectionOfPanels) {
            viewModelScope.launch {
                localPanelsRepo.getAllThePanels().collectLatest {
                    _panels.emit(listOf(defaultPanel()) + it)
                }
            }
        }
        refreshPanelsData()
    }

    fun refreshPanelsData() {
        viewModelScope.launch(Dispatchers.Main) {
            selectedPanelData.value = preferencesRepository.readPreferenceValue(
                longPreferencesKey(
                    AppPreferenceType.LAST_SELECTED_PANEL_ID.name
                )
            ).let {
                try {
                    if (it.isNull() || it!! == Constants.DEFAULT_PANELS_ID) throw Exception()
                    localPanelsRepo.getPanel(it)
                } catch (_: Exception) {
                    defaultPanel()
                }
            }
        }
        if (triggerCollectionOfPanelFolders) {
            viewModelScope.launch(Dispatchers.Main) {
                updatePanelFolders(
                    preferencesRepository.readPreferenceValue(
                        longPreferencesKey(
                            AppPreferenceType.LAST_SELECTED_PANEL_ID.name
                        )
                    ) ?: Constants.DEFAULT_PANELS_ID
                )
            }
        }
    }

    init {
        currentPhaseOfTheDay.value = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
            in 0..11 -> {
                Localization.Key.GoodMorning.getLocalizedString()
            }

            in 12..15 -> {
                Localization.Key.GoodAfternoon.getLocalizedString()
            }

            in 16..23 -> {
                Localization.Key.GoodEvening.getLocalizedString()
            }

            else -> {
                Localization.Key.HeyHi.getLocalizedString()
            }
        }
    }
}