package com.sakethh.linkora.ui.screens.home

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.Localization
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.LocalTagsRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.domain.model.LinkTagsPair
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.home.state.ProcessedPanelFolders
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.utils.isNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.Calendar

class HomeScreenVM(
    localFoldersRepo: LocalFoldersRepo,
    localLinksRepo: LocalLinksRepo,
    localTagsRepo: LocalTagsRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val preferencesRepository: PreferencesRepository,
    triggerCollectionOfPanels: Boolean = true,
    private val triggerCollectionOfPanelFolders: Boolean = true,
    platform: Platform
) : CollectionsScreenVM(
    localFoldersRepo = localFoldersRepo,
    localLinksRepo = localLinksRepo,
    loadNonArchivedRootFoldersOnInit = false,
    loadArchivedRootFoldersOnInit = false,
    localTagsRepo = localTagsRepo,
    platform = platform
) {
    val currentPhaseOfTheDay = mutableStateOf("")

    private val _createdPanels = MutableStateFlow(emptyList<Panel>())
    val createdPanels = _createdPanels.asStateFlow()

    // holds metadata kinda thing that connects a folder to a panel
    private val _activePanelAssociatedPanelFolders = MutableStateFlow(emptyList<PanelFolder>())
    val activePanelAssociatedPanelFolders = _activePanelAssociatedPanelFolders.asStateFlow()


    // holds folders data
    private val _activePanelAssociatedFolders = MutableStateFlow(emptyList<List<Folder>>())
    val activePanelAssociatedFolders = _activePanelAssociatedFolders.asStateFlow()

    // holds links data
    private val _activePanelAssociatedFolderLinks =
        MutableStateFlow(emptyList<List<LinkTagsPair>>())
    val activePanelAssociatedFolderLinks = _activePanelAssociatedFolderLinks.asStateFlow()

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

    private var _activePanelAssociatedFoldersJob: Job? = null

    private val appPreferencesCombined = combine(snapshotFlow {
        AppPreferences.forceShuffleLinks.value
    }, snapshotFlow {
        AppPreferences.selectedSortingTypeType.value
    }) { shuffleLinks, sortingType ->
        Pair(shuffleLinks, sortingType)
    }

    private fun getLinksWithTags(
        nestedLinks: List<List<Link>>,
    ): Flow<List<List<LinkTagsPair>>> {
        val allLinks = nestedLinks.flatten()

        if (allLinks.isEmpty()) {
            return flowOf(nestedLinks.map { emptyList() })
        }

        val allLinkIds = allLinks.map { it.localId }

        return localTagsRepo.getTagsForLinks(allLinkIds).map { tagsMap ->
                nestedLinks.map { sublist ->
                    sublist.map { link ->
                        LinkTagsPair(
                            link = link, tags = tagsMap[link.localId] ?: emptyList()
                        )
                    }
                }
            }
    }

    fun updatePanelFolders(panelId: Long) {
        _activePanelAssociatedFoldersJob?.cancel()

        _activePanelAssociatedFoldersJob = viewModelScope.launch(Dispatchers.Default) {

            launch {
                preferencesRepository.changePreferenceValue(
                    preferenceKey = longPreferencesKey(
                        AppPreferenceType.LAST_SELECTED_PANEL_ID.name
                    ), newValue = panelId
                )
            }

            appPreferencesCombined.flatMapLatest { (shuffleLinks, sortingType) ->
                val processedFoldersFlow = if (panelId == Constants.DEFAULT_PANELS_ID) {
                    combine(
                        localLinksRepo.sortLinksAsNonResultFlow(
                            linkType = LinkType.SAVED_LINK, sortOption = sortingType
                        ), localLinksRepo.sortLinksAsNonResultFlow(
                            linkType = LinkType.IMPORTANT_LINK, sortOption = sortingType
                        )
                    ) { savedLinks, impLinks ->
                        ProcessedPanelFolders(
                            panelFolders = defaultPanelFolders, links = listOf(
                                if (shuffleLinks) savedLinks.shuffled() else savedLinks,
                                if (shuffleLinks) impLinks.shuffled() else impLinks
                            ), folders = listOf(emptyList(), emptyList())
                        )
                    }
                } else {
                    localPanelsRepo.getAllTheFoldersFromAPanel(panelId)
                        .flatMapLatest { panelFolders ->
                            if (panelFolders.isEmpty()) {
                                _activePanelAssociatedPanelFolders.emit(emptyList())
                            }
                            val childFolders = combine(panelFolders.map {
                                localFoldersRepo.sortFoldersAsNonResultFlow(
                                    parentFolderId = it.folderId, sortOption = sortingType
                                )
                            }) {
                                it.toList()
                            }

                            val links = combine(panelFolders.map {
                                localLinksRepo.sortLinksAsNonResultFlow(
                                    linkType = LinkType.FOLDER_LINK,
                                    parentFolderId = it.folderId,
                                    sortOption = sortingType
                                ).map { if (shuffleLinks) it.shuffled() else it }
                            }) {
                                it.toList()
                            }

                            combine(childFolders, links) { childFolders, links ->
                                ProcessedPanelFolders(
                                    panelFolders = panelFolders,
                                    links = links,
                                    folders = childFolders
                                )
                            }
                        }
                }

                processedFoldersFlow.flatMapLatest { processedData ->
                    val nestedLinks = processedData.links
                    val allLinks = nestedLinks.flatten()
                    if (allLinks.isEmpty()) {
                        flowOf(
                            Triple(
                                processedData.panelFolders,
                                processedData.folders,
                                emptyList()
                            )
                        )
                    } else {
                        val linkIds = allLinks.map { it.localId }
                        localTagsRepo.getTagsForLinks(linkIds).map { tagsMap ->
                                val linksWithTags = nestedLinks.map { sublist ->
                                    sublist.map { link ->
                                        LinkTagsPair(
                                            link = link, tags = tagsMap[link.localId] ?: emptyList()
                                        )
                                    }
                                }
                                Triple(
                                    processedData.panelFolders,
                                    processedData.folders,
                                    linksWithTags
                                )
                            }
                    }
                }
            }.collectLatest { (panelFolders, folders, linksWithTags) ->
                _activePanelAssociatedPanelFolders.emit(panelFolders.distinctBy { it.folderId })
                _activePanelAssociatedFolders.emit(folders)
                _activePanelAssociatedFolderLinks.emit(linksWithTags)
            }
        }
    }

    val selectedPanelData = mutableStateOf<Panel?>(null)

    init {
        if (triggerCollectionOfPanels) {
            viewModelScope.launch {
                localPanelsRepo.getAllThePanels().collectLatest {
                    _createdPanels.emit(listOf(defaultPanel()) + it)
                }
            }
        }
        refreshPanelsData()
    }

    private fun refreshPanelsData() {
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
            if (triggerCollectionOfPanelFolders) {
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