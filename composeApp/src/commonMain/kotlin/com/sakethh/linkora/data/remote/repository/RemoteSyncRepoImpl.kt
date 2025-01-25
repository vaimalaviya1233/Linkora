package com.sakethh.linkora.data.remote.repository

import com.sakethh.linkora.common.network.Network
import com.sakethh.linkora.common.utils.asWebSocketUrl
import com.sakethh.linkora.common.utils.forceSaveWithoutRetrieving
import com.sakethh.linkora.common.utils.isSameAsCurrentClient
import com.sakethh.linkora.common.utils.wrappedResultFlow
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.AllTablesDTO
import com.sakethh.linkora.domain.dto.server.Correlation
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.TombstoneDTO
import com.sakethh.linkora.domain.dto.server.folder.FolderDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateNoteOfALinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateTitleOfTheLinkDTO
import com.sakethh.linkora.domain.dto.server.panel.DeleteAPanelFromAFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.PanelDTO
import com.sakethh.linkora.domain.dto.server.panel.PanelFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import com.sakethh.linkora.ui.utils.linkoraLog
import io.ktor.client.call.body
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.websocket.Frame
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.util.UUID

class RemoteSyncRepoImpl(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val authToken: () -> String,
    private val baseUrl: () -> String
) : RemoteSyncRepo {
    private val json = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true
        this.isLenient = true
        this.prettyPrint = true
    }
    override suspend fun readSocketEvents(): Flow<Result<Unit>> {
        return wrappedResultFlow {
            Network.client.webSocket(baseUrl().asWebSocketUrl() + "events") {
                this.incoming.consumeAsFlow().collectLatest {
                    if (it is Frame.Text) {
                        val deserializedWebSocketEvent =
                            json.decodeFromString<WebSocketEvent>((it.data).decodeToString())
                        updateLocalDBAccordingToEvent(deserializedWebSocketEvent)
                    }
                }
            }
        }
    }

    private lateinit var deserializedUpdatableFolders: List<FolderDTO>
    override suspend fun applyUpdatesFromRemote(timeStampAfter: Long): Flow<Result<Unit>> {


        /**
        `updateLocalDBAccordingToEvent` is supposed to handle "events" from the socket.

        i didn't feel like writing it all from scratch for this function (`applyUpdatesFromRemote`)
        so i just mocked it like, "oh, that’s not me, must be some other client 🤓☝️" and force update respectively.

        and that's why `randomCorrelation` exists.
         **/
        val randomCorrelation = Correlation(
            id = UUID.randomUUID().toString(), clientName = "🤨📸"
        )


        return wrappedResultFlow {
            Network.client.get(baseUrl() + RemoteRoute.SyncInLocalRoute.GET_UPDATES.name) {
                bearerAuth(authToken())
                contentType(ContentType.Application.Json)
                parameter("timestamp", timeStampAfter)
            }.body<AllTablesDTO>().let { remoteResponse ->
                coroutineScope {
                        awaitAll(async {
                            remoteResponse.links.forEach { remoteLinkDTO ->
                                val localId = localLinksRepo.getLocalLinkId(remoteLinkDTO.id)
                                if (localId == null) {
                                    updateLocalDBAccordingToEvent(
                                        WebSocketEvent(
                                            operation = RemoteRoute.Link.CREATE_A_NEW_LINK.name,
                                            payload = json.encodeToJsonElement(
                                                remoteLinkDTO.copy(correlation = randomCorrelation)
                                            )
                                        )
                                    )
                                } else {
                                    updateLocalDBAccordingToEvent(
                                        WebSocketEvent(
                                            operation = RemoteRoute.Link.UPDATE_LINK.name,
                                            payload = json.encodeToJsonElement(
                                                remoteLinkDTO.copy(correlation = randomCorrelation)
                                            )
                                        )
                                    )
                                }
                            }
                        }, async {
                            remoteResponse.panels.forEach { remotePanelDTO ->
                                val localId =
                                    localPanelsRepo.getLocalPanelId(remotePanelDTO.panelId)
                                if (localId == null) {
                                    updateLocalDBAccordingToEvent(
                                        WebSocketEvent(
                                            operation = RemoteRoute.Panel.ADD_A_NEW_PANEL.name,
                                            payload = json.encodeToJsonElement(
                                                remotePanelDTO.copy(correlation = randomCorrelation)
                                            )
                                        )
                                    )
                                } else {
                                    updateLocalDBAccordingToEvent(
                                        WebSocketEvent(
                                            operation = RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name,
                                            payload = json.encodeToJsonElement(
                                                UpdatePanelNameDTO(
                                                    newName = remotePanelDTO.panelName,
                                                    panelId = remotePanelDTO.panelId,
                                                    correlation = randomCorrelation
                                                )
                                            )
                                        )
                                    )
                                }
                            }
                            remoteResponse.panelFolders.forEach { remotePanelFolder ->
                                // a panel_folder can only be added with this endpoint response because if it got deleted, it will be caught in the tombstones response, not here
                                updateLocalDBAccordingToEvent(
                                    WebSocketEvent(
                                        operation = RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name,
                                        payload = json.encodeToJsonElement(
                                            remotePanelFolder.copy(correlation = randomCorrelation)
                                        )
                                    )
                                )
                            }
                        })
                }
            }
        }
    }

    private suspend fun insertFolder(
        folderDTO: FolderDTO, processedFolder: MutableList<Long> = mutableListOf()
    ) {
        if (processedFolder.contains(folderDTO.id)) return
        processedFolder.add(folderDTO.id)
        val localId = localFoldersRepo.getLocalIdOfAFolder(folderDTO.id)
        val parentFolderId: Long? = if (folderDTO.parentFolderId == null) {
            null
        } else {
            localFoldersRepo.getLocalIdOfAFolder(folderDTO.parentFolderId).let {
                var requiredParentId = it
                if (requiredParentId == null) {/*
                if the folder that's supposed to be the parent ain't in the local DB
                we gotta insert that first and then handle the rest
                */
                    val parentFolder = deserializedUpdatableFolders.firstOrNull {
                        it.id == folderDTO.parentFolderId
                    }
                    if (parentFolder != null) {
                        insertFolder(parentFolder, processedFolder)
                        requiredParentId =
                            localFoldersRepo.getLocalIdOfAFolder(folderDTO.parentFolderId)
                    }
                }
                requiredParentId
            }
        }
        if (localId != null) {
            localFoldersRepo.updateAFolderData(
                Folder(
                    name = folderDTO.name,
                    note = folderDTO.note,
                    parentFolderId = parentFolderId,
                    localId = localId,
                    remoteId = folderDTO.id,
                    isArchived = folderDTO.isArchived
                )
            ).collect()
        } else {
            localFoldersRepo.insertANewFolder(
                folder = Folder(
                    name = folderDTO.name,
                    note = folderDTO.note,
                    parentFolderId = parentFolderId,
                    remoteId = folderDTO.id,
                    isArchived = folderDTO.isArchived
                ), ignoreFolderAlreadyExistsException = true
            ).collect()
        }
    }

    override suspend fun updateDataBasedOnRemoteTombstones(timeStampAfter: Long): Flow<Result<Unit>> {
        return wrappedResultFlow {
            Network.client.get(baseUrl() + RemoteRoute.SyncInLocalRoute.GET_TOMBSTONES.name) {
                bearerAuth(authToken())
                contentType(ContentType.Application.Json)
                parameter("timestamp", timeStampAfter)
            }.body<List<TombstoneDTO>>().map {
                WebSocketEvent(
                    operation = it.operation,
                    payload = it.payload
                )
            }.forEach {
                linkoraLog("deleting based on ${it.operation} : ${it.payload}")
                updateLocalDBAccordingToEvent(it)
            }
        }
    }


    private suspend fun updateLocalDBAccordingToEvent(deserializedWebSocketEvent: WebSocketEvent) {
        when (deserializedWebSocketEvent.operation) {

            // folders:

            RemoteRoute.Folder.CREATE_FOLDER.name -> {
                val folderDto = json.decodeFromJsonElement<FolderDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (folderDto.correlation.isSameAsCurrentClient()) {
                    return
                }
                localFoldersRepo.insertANewFolder(
                    folder = Folder(
                        name = folderDto.name,
                        note = folderDto.note,
                        parentFolderId = if (folderDto.parentFolderId != null) localFoldersRepo.getLocalIdOfAFolder(
                            folderDto.parentFolderId
                        ) else null,
                        remoteId = folderDto.id,
                        isArchived = folderDto.isArchived
                    ), ignoreFolderAlreadyExistsException = true, viaSocket = true
                ).collect()
            }

            RemoteRoute.Folder.DELETE_FOLDER.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (folderId != null) {
                    localFoldersRepo.deleteAFolder(folderId, viaSocket = true).collect()
                }
            }

            RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (folderId != null) {
                    localFoldersRepo.markFolderAsArchive(
                        folderId, viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (folderId != null) {
                    localFoldersRepo.markFolderAsRegularFolder(
                        folderId, viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Folder.UPDATE_FOLDER_NAME.name -> {
                val updateFolderNameDTO = json.decodeFromJsonElement<UpdateFolderNameDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (updateFolderNameDTO.correlation.isSameAsCurrentClient()) return

                val localFolderId =
                    localFoldersRepo.getLocalIdOfAFolder(updateFolderNameDTO.folderId)
                if (localFolderId != null) {
                    localFoldersRepo.getThisFolderData(localFolderId).collectLatest {
                        it.onSuccess {
                            localFoldersRepo.updateAFolderData(
                                it.data.copy(
                                    localId = localFolderId,
                                    name = updateFolderNameDTO.newFolderName
                                )
                            ).collect()
                        }
                    }
                }
            }

            RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name -> {
                val updateFolderNoteDTO = json.decodeFromJsonElement<UpdateFolderNoteDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (updateFolderNoteDTO.correlation.isSameAsCurrentClient()) return

                val localFolderId =
                    localFoldersRepo.getLocalIdOfAFolder(updateFolderNoteDTO.folderId)
                if (localFolderId != null) {
                    localFoldersRepo.getThisFolderData(localFolderId).collectLatest {
                        it.onSuccess {
                            localFoldersRepo.updateAFolderData(
                                it.data.copy(
                                    localId = localFolderId, note = updateFolderNoteDTO.newNote
                                )
                            ).collect()
                        }
                    }
                }
            }

            RemoteRoute.Folder.DELETE_FOLDER_NOTE.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localFolderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (localFolderId != null) {
                    localFoldersRepo.getThisFolderData(localFolderId).collectLatest {
                        it.onSuccess {
                            localFoldersRepo.deleteAFolderNote(
                                localFolderId, viaSocket = true
                            ).collect()
                        }
                    }
                }
            }


            // links:

            RemoteRoute.Link.UPDATE_LINK_TITLE.name -> {
                val updateTitleOfTheLinkDTO = json.decodeFromJsonElement<UpdateTitleOfTheLinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (updateTitleOfTheLinkDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(updateTitleOfTheLinkDTO.linkId)
                if (localLinkId != null) {
                    localLinksRepo.updateLinkTitle(
                        localLinkId, updateTitleOfTheLinkDTO.newTitleOfTheLink, viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Link.UPDATE_LINK_NOTE.name -> {
                val updateNoteOfALinkDTO = json.decodeFromJsonElement<UpdateNoteOfALinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (updateNoteOfALinkDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(updateNoteOfALinkDTO.linkId)
                if (localLinkId != null) {
                    localLinksRepo.updateLinkNote(
                        localLinkId, updateNoteOfALinkDTO.newNote, viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Link.DELETE_A_LINK.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    localLinksRepo.deleteALink(localLinkId, viaSocket = true).collect()
                }
            }

            RemoteRoute.Link.ARCHIVE_LINK.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    localLinksRepo.archiveALink(localLinkId, viaSocket = true).collect()
                }
            }

            RemoteRoute.Link.UNARCHIVE_LINK.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    val link = localLinksRepo.getALink(localLinkId)
                    localLinksRepo.updateALink(
                        link.copy(linkType = LinkType.SAVED_LINK), viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Link.MARK_AS_IMP.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    val link = localLinksRepo.getALink(localLinkId)
                    localLinksRepo.updateALink(
                        link.copy(markedAsImportant = true), viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Link.UNMARK_AS_IMP.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    val link = localLinksRepo.getALink(localLinkId)
                    localLinksRepo.updateALink(
                        link.copy(markedAsImportant = false), viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Link.UPDATE_LINK.name -> {
                val linkDTO = json.decodeFromJsonElement<LinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (linkDTO.correlation.isSameAsCurrentClient()) return

                val localLinkId = localLinksRepo.getLocalLinkId(linkDTO.id)
                if (localLinkId != null) {
                    val link = localLinksRepo.getALink(localLinkId)
                    localLinksRepo.updateALink(
                        link.copy(
                            linkType = linkDTO.linkType,
                            title = linkDTO.title,
                            url = linkDTO.url,
                            imgURL = linkDTO.imgURL,
                            note = linkDTO.note,
                            idOfLinkedFolder = if (linkDTO.idOfLinkedFolder != null) localFoldersRepo.getLocalIdOfAFolder(
                                linkDTO.idOfLinkedFolder
                            ) else null,
                            userAgent = linkDTO.userAgent,
                            markedAsImportant = linkDTO.markedAsImportant,
                            mediaType = linkDTO.mediaType
                        ), viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Link.CREATE_A_NEW_LINK.name -> {
                val linkDTO = json.decodeFromJsonElement<LinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (linkDTO.correlation.isSameAsCurrentClient()) return

                localLinksRepo.addANewLink(
                    link = Link(
                        linkType = linkDTO.linkType,
                        title = linkDTO.title,
                        url = linkDTO.url,
                        imgURL = linkDTO.imgURL,
                        note = linkDTO.note,
                        idOfLinkedFolder = if (linkDTO.idOfLinkedFolder != null) localFoldersRepo.getLocalIdOfAFolder(
                            linkDTO.idOfLinkedFolder
                        ) else null,
                        remoteId = linkDTO.id,
                        userAgent = linkDTO.userAgent,
                        markedAsImportant = linkDTO.markedAsImportant,
                        mediaType = linkDTO.mediaType
                    ), linkSaveConfig = forceSaveWithoutRetrieving(), viaSocket = true
                ).collect()
            }


            // panels:

            RemoteRoute.Panel.ADD_A_NEW_PANEL.name -> {
                val panelDTO =
                    json.decodeFromJsonElement<PanelDTO>(deserializedWebSocketEvent.payload)

                if (panelDTO.correlation.isSameAsCurrentClient()) return

                localPanelsRepo.addaNewPanel(
                    Panel(
                        panelName = panelDTO.panelName, remoteId = panelDTO.panelId
                    ), viaSocket = true
                ).collect()
            }

            RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name -> {
                val panelFolderDTO = json.decodeFromJsonElement<PanelFolderDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (panelFolderDTO.correlation.isSameAsCurrentClient()) return

                val localFolderId = localFoldersRepo.getLocalIdOfAFolder(panelFolderDTO.folderId)
                val localPanelsId = localPanelsRepo.getLocalPanelId(panelFolderDTO.connectedPanelId)
                if (localPanelsId != null && localFolderId != null) {
                    localPanelsRepo.addANewFolderInAPanel(
                        PanelFolder(
                            folderId = localFolderId,
                            panelPosition = panelFolderDTO.panelPosition,
                            folderName = panelFolderDTO.folderName,
                            connectedPanelId = localPanelsId,
                            remoteId = panelFolderDTO.id
                        ), viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Panel.DELETE_A_PANEL.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localPanelId = localPanelsRepo.getLocalPanelId(idBasedDTO.id)
                if (localPanelId != null) {
                    localPanelsRepo.deleteAPanel(localPanelId, viaSocket = true).collect()
                }
            }

            RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name -> {
                val updatePanelNameDTO = json.decodeFromJsonElement<UpdatePanelNameDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (updatePanelNameDTO.correlation.isSameAsCurrentClient()) return

                val localPanelId = localPanelsRepo.getLocalPanelId(updatePanelNameDTO.panelId)
                if (localPanelId != null) {
                    localPanelsRepo.updateAPanelName(
                        newName = updatePanelNameDTO.newName,
                        panelId = localPanelId,
                        viaSocket = true
                    ).collect()
                }
            }

            RemoteRoute.Panel.DELETE_A_FOLDER_FROM_ALL_PANELS.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) return

                val localFolderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (localFolderId != null) {
                    localPanelsRepo.deleteAFolderFromAllPanels(localFolderId)
                }
            }

            RemoteRoute.Panel.DELETE_A_FOLDER_FROM_A_PANEL.name -> {
                val deleteAPanelFromAFolderDTO =
                    json.decodeFromJsonElement<DeleteAPanelFromAFolderDTO>(
                        deserializedWebSocketEvent.payload
                    )

                if (deleteAPanelFromAFolderDTO.correlation.isSameAsCurrentClient()) return

                val localFolderId =
                    localFoldersRepo.getLocalIdOfAFolder(deleteAPanelFromAFolderDTO.folderID)
                val localPanelId =
                    localPanelsRepo.getLocalPanelId(deleteAPanelFromAFolderDTO.panelId)
                if (localFolderId != null && localPanelId != null) {
                    localPanelsRepo.deleteAFolderFromAPanel(
                        localPanelId, localFolderId, viaSocket = true
                    ).collect()
                }
            }
        }

    }
}