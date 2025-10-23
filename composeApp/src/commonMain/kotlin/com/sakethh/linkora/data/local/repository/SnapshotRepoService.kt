package com.sakethh.linkora.data.local.repository

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.FileType
import com.sakethh.linkora.domain.SnapshotFormat
import com.sakethh.linkora.domain.dto.server.AllTablesDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.JSONExportSchema
import com.sakethh.linkora.domain.model.PanelForJSONExportSchema
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.model.tag.LinkTag
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.LocalTagsRepo
import com.sakethh.linkora.platform.FileManager
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.AppVM.Companion.pauseSnapshots
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.linkora.utils.pushSnackbar
import com.sakethh.linkora.utils.septetCombine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json

open class SnapshotRepoService(
    private val linksRepo: LocalLinksRepo,
    private val foldersRepo: LocalFoldersRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val exportDataRepo: ExportDataRepo,
    private val localTagsRepo: LocalTagsRepo,
    private val fileManager: FileManager,
    coroutineScope: CoroutineScope
) {
    private var snapshotsJob: Job? = null

    val isAnySnapshotOngoing = mutableStateOf(false)

    companion object {
        val forceSnapshot = mutableStateOf(false)
    }

    init {
        coroutineScope.launch {
            snapshotFlow {
                AppPreferences.areSnapshotsEnabled.value
            }.debounce(1000).collectLatest {
                if (it) {
                    snapshotsJob = this.launch(Dispatchers.Default) {
                        linkoraLog("data checks for snapshots are now live")
                        septetCombine(
                            linksRepo.getAllLinksAsFlow(),
                            foldersRepo.getAllFoldersAsFlow(),
                            localPanelsRepo.getAllThePanels(),
                            localPanelsRepo.getAllThePanelFoldersAsAFlow(),
                            snapshotFlow {
                                forceSnapshot.value
                            },
                            localTagsRepo.getAllTags(AppPreferences.selectedSortingTypeType.value),
                            localTagsRepo.getAllLinkTags()
                        ) { links, folders, panels, panelFolders, _, tags, linkTags ->
                            AllTablesDTO(
                                links = links,
                                folders = folders,
                                panels = panels,
                                panelFolders = panelFolders,
                                tags = tags,
                                linkTagsPairs = linkTags,
                            )
                        }.cancellable()
                            .drop(1) // ignore the first emission which gets fired when the app launches
                            .debounce(1000).flowOn(Dispatchers.Default).collectLatest {
                                if (pauseSnapshots || (it.links + it.folders + it.panelFolders + it.panels).isEmpty()) return@collectLatest
                                try {
                                    isAnySnapshotOngoing.value = true
                                    createAManualSnapshot(
                                        allLinks = it.links,
                                        allFolders = it.folders,
                                        allPanels = it.panels,
                                        allPanelFolders = it.panelFolders,
                                        allTags = it.tags,
                                        allLinkTagsPairs = it.linkTagsPairs,
                                        onCompletion = {})
                                } catch (e: Exception) {
                                    e.pushSnackbar()
                                } finally {
                                    isAnySnapshotOngoing.value = false
                                }
                            }
                    }
                } else {
                    linkoraLog("cancelled data checks for snapshots")
                    snapshotsJob?.cancel()
                }
            }
        }
    }

    suspend fun createAManualSnapshot(
        allLinks: List<Link>,
        allFolders: List<Folder>,
        allPanels: List<Panel>,
        allPanelFolders: List<PanelFolder>,
        allTags: List<Tag>,
        allLinkTagsPairs: List<LinkTag>,
        onCompletion: () -> Unit
    ) {
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

            val serializedJsonExportString =
                JSONExportSchema(schemaVersion = JSONExportSchema.VERSION, links = allLinks.map {
                    it.copy(
                        remoteId = null, lastModified = 0
                    )
                }, folders = allFolders.map {
                    it.copy(
                        remoteId = null, lastModified = 0
                    )
                }, panels = PanelForJSONExportSchema(panels = allPanels.map {
                    it.copy(
                        remoteId = null, lastModified = 0
                    )
                }, panelFolders = allPanelFolders.map {
                    it.copy(
                        remoteId = null, lastModified = 0
                    )
                }), tags = allTags.map {
                    it.copy(remoteId = null, lastModified = 0)
                }, linkTags = allLinkTagsPairs.map {
                    it.copy(remoteId = null, lastModified = 0)
                }).run {
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
                    links = allLinks, folders = allFolders
                ),
                fileType = ExportFileType.HTML,
                exportLocation = AppPreferences.currentBackupLocation.value,
            )
        }
        onCompletion()
    }
}