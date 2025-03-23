package com.sakethh.linkora.data.remote.repository

import androidx.datastore.preferences.core.longPreferencesKey
import com.sakethh.linkora.common.network.Network
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.asWebSocketUrl
import com.sakethh.linkora.common.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.common.utils.forceSaveWithoutRetrieving
import com.sakethh.linkora.common.utils.isSameAsCurrentClient
import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.common.utils.postFlow
import com.sakethh.linkora.common.utils.updateLastSyncedWithServerTimeStamp
import com.sakethh.linkora.common.utils.wrappedResultFlow
import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.asAddFolderDTO
import com.sakethh.linkora.domain.asAddLinkDTO
import com.sakethh.linkora.domain.dto.server.AllTablesDTO
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.Correlation
import com.sakethh.linkora.domain.dto.server.DeleteEverythingDTO
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.MoveItemsDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.TombstoneDTO
import com.sakethh.linkora.domain.dto.server.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.server.folder.FolderDTO
import com.sakethh.linkora.domain.dto.server.folder.MarkSelectedFoldersAsRootDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.dto.server.link.AddLinkDTO
import com.sakethh.linkora.domain.dto.server.link.DeleteDuplicateLinksDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateNoteOfALinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateTitleOfTheLinkDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.AddANewPanelFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.DeleteAFolderFromAPanelDTO
import com.sakethh.linkora.domain.dto.server.panel.PanelDTO
import com.sakethh.linkora.domain.dto.server.panel.PanelFolderDTO
import com.sakethh.linkora.domain.dto.server.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalMultiActionRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteFoldersRepo
import com.sakethh.linkora.domain.repository.remote.RemoteLinksRepo
import com.sakethh.linkora.domain.repository.remote.RemotePanelsRepo
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
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
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.encodeToJsonElement
import java.util.UUID

class RemoteSyncRepoImpl(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val authToken: () -> String,
    private val baseUrl: () -> String,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val remoteFoldersRepo: RemoteFoldersRepo,
    private val remoteLinksRepo: RemoteLinksRepo,
    private val remotePanelsRepo: RemotePanelsRepo,
    private val preferencesRepository: PreferencesRepository,
    private val localMultiActionRepo: LocalMultiActionRepo
) : RemoteSyncRepo {
    private val json = Json {
        this.ignoreUnknownKeys = true
        this.encodeDefaults = true
        this.isLenient = true
        this.prettyPrint = true
    }

    override suspend fun readSocketEvents(currentCorrelation: Correlation): Flow<Result<Unit>> {
        return wrappedResultFlow {
            Network.client.webSocket(urlString = baseUrl().asWebSocketUrl() + "events", request = {
                bearerAuth(authToken())
                parameter(key = "correlation", value = Json.encodeToString(currentCorrelation))
            }) {
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

    private suspend fun <T> Flow<Result<T>>.collectAndUpdateTimestamp(eventTimestamp: Long) {
        this.collectLatest {
            it.onSuccess {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(eventTimestamp)
            }
        }
    }

    private suspend inline fun Flow<Result<TimeStampBasedResponse>>.removeQueueItemAndSyncTimestamp(
        queueId: Long
    ) {
        this.collectLatest {
            it.onSuccess {
                pendingSyncQueueRepo.removeFromQueue(queueId)
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.data.eventTimestamp)
            }
        }
    }

    override suspend fun <T> SendChannel<Result<T>>.pushPendingSyncQueueToServer(): Flow<Result<Unit>> {
        return wrappedResultFlow {
            send(Result.Loading(message = "[SYNC] Starting to push pending sync queue to server"))

            send(Result.Loading(message = "[QUEUE] Fetching all items from the pending sync queue"))
            pendingSyncQueueRepo.getAllItemsFromQueue().forEach { queueItem ->
                send(Result.Loading(message = "[QUEUE] Processing queue item (ID: ${queueItem.id}, Operation: ${queueItem.operation})"))

                when (queueItem.operation) {
                    RemoteRoute.Folder.CREATE_FOLDER.name -> {
                        send(Result.Loading(message = "[FOLDER] Creating folder from queue item (ID: ${queueItem.id})"))
                        val addFolderDTO = Json.decodeFromString<AddFolderDTO>(queueItem.payload)
                        remoteFoldersRepo.createFolder(addFolderDTO.let {
                            it.copy(
                                parentFolderId = if (it.parentFolderId != null) localFoldersRepo.getRemoteIdOfAFolder(
                                    it.parentFolderId
                                ) else null
                            )
                        }).collectLatest {
                            it.onSuccess { remoteResponse ->
                                send(Result.Loading(message = "[FOLDER] Successfully created folder on server (Remote ID: ${remoteResponse.data.id})"))
                                localFoldersRepo.getThisFolderData(addFolderDTO.offlineSyncItemId)
                                    .collectLatest {
                                        it.onSuccess {
                                            send(Result.Loading(message = "[FOLDER] Updating local folder data (Local ID: ${it.data.localId}, Remote ID: ${remoteResponse.data.id})"))
                                            localFoldersRepo.updateLocalFolderData(
                                                it.data.copy(
                                                    remoteId = remoteResponse.data.id,
                                                )
                                            ).collect()
                                            preferencesRepository.updateLastSyncedWithServerTimeStamp(
                                                remoteResponse.data.timeStampBasedResponse.eventTimestamp
                                            )
                                            pendingSyncQueueRepo.removeFromQueue(queueItem.id)
                                            send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after successful sync"))
                                        }
                                    }
                            }
                        }
                    }

                    RemoteRoute.Folder.DELETE_FOLDER.name -> {
                        send(Result.Loading(message = "[FOLDER] Deleting folder from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        remoteFoldersRepo.deleteFolder(idBasedDTO)
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after deleting folder"))
                    }

                    RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name -> {
                        send(Result.Loading(message = "[FOLDER] Marking folder as archive from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteId = localFoldersRepo.getRemoteIdOfAFolder(idBasedDTO.id)!!
                        remoteFoldersRepo.markAsArchive(idBasedDTO.copy(id = remoteId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after marking folder as archive"))
                    }

                    RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name -> {
                        send(Result.Loading(message = "[FOLDER] Marking folder as regular from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteId = localFoldersRepo.getRemoteIdOfAFolder(idBasedDTO.id)!!
                        remoteFoldersRepo.markAsRegularFolder(idBasedDTO.copy(id = remoteId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after marking folder as regular"))
                    }

                    RemoteRoute.Folder.UPDATE_FOLDER_NAME.name -> {
                        send(Result.Loading(message = "[FOLDER] Updating folder name from queue item (ID: ${queueItem.id})"))
                        val updateFolderNameDTO =
                            Json.decodeFromString<UpdateFolderNameDTO>(queueItem.payload)
                        val remoteId =
                            localFoldersRepo.getRemoteIdOfAFolder(updateFolderNameDTO.folderId)!!
                        remoteFoldersRepo.updateFolderName(
                            updateFolderNameDTO.copy(folderId = remoteId)
                        ).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after updating folder name"))
                    }

                    RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name -> {
                        send(Result.Loading(message = "[FOLDER] Updating folder note from queue item (ID: ${queueItem.id})"))
                        val updateFolderNoteDTO =
                            Json.decodeFromString<UpdateFolderNoteDTO>(queueItem.payload)
                        val remoteId =
                            localFoldersRepo.getRemoteIdOfAFolder(updateFolderNoteDTO.folderId)!!
                        remoteFoldersRepo.updateFolderNote(
                            updateFolderNoteDTO.copy(folderId = remoteId)
                        ).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after updating folder note"))
                    }

                    RemoteRoute.Folder.DELETE_FOLDER_NOTE.name -> {
                        send(Result.Loading(message = "[FOLDER] Deleting folder note from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteId = localFoldersRepo.getRemoteIdOfAFolder(idBasedDTO.id)!!
                        remoteFoldersRepo.deleteFolderNote(idBasedDTO.copy(id = remoteId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after deleting folder note"))
                    }

                    RemoteRoute.Link.UPDATE_LINK_TITLE.name -> {
                        send(Result.Loading(message = "[LINK] Updating link title from queue item (ID: ${queueItem.id})"))
                        val linkDTO = Json.decodeFromString<LinkDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(linkDTO.id)!!
                        remoteLinksRepo.updateLink(
                            linkDTO.copy(
                                id = remoteLinkId
                            )
                        ).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after updating link title"))
                    }

                    RemoteRoute.Link.UPDATE_LINK_NOTE.name -> {
                        send(Result.Loading(message = "[LINK] Updating link note from queue item (ID: ${queueItem.id})"))
                        val linkDTO = Json.decodeFromString<LinkDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(linkDTO.id)!!
                        remoteLinksRepo.updateLink(linkDTO.copy(id = remoteLinkId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after updating link note"))
                    }

                    RemoteRoute.Link.DELETE_A_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Deleting link from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        remoteLinksRepo.deleteALink(idBasedDTO)
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after deleting link"))
                    }

                    RemoteRoute.Link.ARCHIVE_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Archiving link from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(idBasedDTO.id)!!
                        remoteLinksRepo.archiveALink(idBasedDTO.copy(id = remoteLinkId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after archiving link"))
                    }

                    RemoteRoute.Link.UNARCHIVE_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Unarchiving link from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(idBasedDTO.id)!!
                        remoteLinksRepo.unArchiveALink(idBasedDTO.copy(id = remoteLinkId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after unarchiving link"))
                    }

                    RemoteRoute.Link.UPDATE_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Updating link from queue item (ID: ${queueItem.id})"))
                        val linkDTO = Json.decodeFromString<LinkDTO>(queueItem.payload)
                        val remoteId = localLinksRepo.getRemoteLinkId(linkDTO.id)!!
                        remoteLinksRepo.updateLink(linkDTO.copy(id = remoteId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after updating link"))
                    }

                    RemoteRoute.Link.CREATE_A_NEW_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Creating new link from queue item (ID: ${queueItem.id})"))
                        val addLinkDTO = Json.decodeFromString<AddLinkDTO>(queueItem.payload)
                        remoteLinksRepo.addANewLink(
                            addLinkDTO.copy(
                                idOfLinkedFolder = if (addLinkDTO.idOfLinkedFolder != null) localFoldersRepo.getRemoteIdOfAFolder(
                                    addLinkDTO.idOfLinkedFolder
                                ) else null
                            )
                        ).collectLatest {
                            it.onSuccess { remoteResponse ->
                                send(Result.Loading(message = "[LINK] Successfully created link on server (Remote ID: ${remoteResponse.data.id})"))
                                val updatedLink =
                                    localLinksRepo.getALink(addLinkDTO.offlineSyncItemId)
                                        .copy(remoteId = remoteResponse.data.id)
                                localLinksRepo.updateALink(updatedLink, viaSocket = true).collect()
                                preferencesRepository.updateLastSyncedWithServerTimeStamp(
                                    remoteResponse.data.timeStampBasedResponse.eventTimestamp
                                )
                                pendingSyncQueueRepo.removeFromQueue(queueItem.id)
                                send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after creating link"))
                            }
                        }
                    }

                    RemoteRoute.Panel.ADD_A_NEW_PANEL.name -> {
                        send(Result.Loading(message = "[PANEL] Adding new panel from queue item (ID: ${queueItem.id})"))
                        val addANewPanelDTO =
                            Json.decodeFromString<AddANewPanelDTO>(queueItem.payload)
                        remotePanelsRepo.addANewPanel(addANewPanelDTO).collectLatest {
                            it.onSuccess { remoteResponse ->
                                send(Result.Loading(message = "[PANEL] Successfully added panel on server (Remote ID: ${remoteResponse.data.id})"))
                                val updatedPanel =
                                    localPanelsRepo.getPanel(addANewPanelDTO.offlineSyncItemId)
                                        .copy(remoteId = remoteResponse.data.id)
                                localPanelsRepo.updatePanel(updatedPanel)
                                preferencesRepository.updateLastSyncedWithServerTimeStamp(
                                    remoteResponse.data.timeStampBasedResponse.eventTimestamp
                                )
                                pendingSyncQueueRepo.removeFromQueue(queueItem.id)
                                send(Result.Loading(message = "[PANEL] Removed queue item (ID: ${queueItem.id}) after adding panel"))
                            }
                        }
                    }

                    RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name -> {
                        send(Result.Loading(message = "[PANEL] Adding new folder in panel from queue item (ID: ${queueItem.id})"))
                        val addANewPanelFolderDTO =
                            Json.decodeFromString<AddANewPanelFolderDTO>(queueItem.payload)
                        val remoteFolderId =
                            localFoldersRepo.getRemoteIdOfAFolder(addANewPanelFolderDTO.folderId)!!
                        val remoteConnectedPanelId =
                            localPanelsRepo.getRemotePanelId(addANewPanelFolderDTO.connectedPanelId)!!
                        remotePanelsRepo.addANewFolderInAPanel(
                            addANewPanelFolderDTO.copy(
                                folderId = remoteFolderId, connectedPanelId = remoteConnectedPanelId
                            )
                        ).collectLatest {
                            it.onSuccess { remoteResponse ->
                                send(Result.Loading(message = "[PANEL] Successfully added folder in panel on server (Remote ID: ${remoteResponse.data.id})"))
                                val updatedPanel =
                                    localPanelsRepo.getPanel(addANewPanelFolderDTO.offlineSyncItemId)
                                        .copy(remoteId = remoteResponse.data.id)
                                preferencesRepository.updateLastSyncedWithServerTimeStamp(
                                    remoteResponse.data.timeStampBasedResponse.eventTimestamp
                                )
                                localPanelsRepo.updatePanel(updatedPanel)
                                pendingSyncQueueRepo.removeFromQueue(queueItem.id)
                                send(Result.Loading(message = "[PANEL] Removed queue item (ID: ${queueItem.id}) after adding folder in panel"))
                            }
                        }
                    }

                    RemoteRoute.Panel.DELETE_A_PANEL.name -> {
                        send(Result.Loading(message = "[PANEL] Deleting panel from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        remotePanelsRepo.deleteAPanel(idBasedDTO)
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[PANEL] Removed queue item (ID: ${queueItem.id}) after deleting panel"))
                    }

                    RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name -> {
                        send(Result.Loading(message = "[PANEL] Updating panel name from queue item (ID: ${queueItem.id})"))
                        val updatePanelNameDTO =
                            Json.decodeFromString<UpdatePanelNameDTO>(queueItem.payload)
                        val remotePanelId =
                            localPanelsRepo.getRemotePanelId(updatePanelNameDTO.panelId)!!
                        remotePanelsRepo.updateAPanelName(updatePanelNameDTO.copy(panelId = remotePanelId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[PANEL] Removed queue item (ID: ${queueItem.id}) after updating panel name"))
                    }

                    RemoteRoute.Panel.DELETE_A_FOLDER_FROM_ALL_PANELS.name -> {
                        send(Result.Loading(message = "[PANEL] Deleting folder from all panels from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO = Json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteFolderId = localFoldersRepo.getRemoteIdOfAFolder(idBasedDTO.id)!!
                        remotePanelsRepo.deleteAFolderFromAllPanels(idBasedDTO.copy(id = remoteFolderId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[PANEL] Removed queue item (ID: ${queueItem.id}) after deleting folder from all panels"))
                    }

                    RemoteRoute.Panel.DELETE_A_FOLDER_FROM_A_PANEL.name -> {
                        send(Result.Loading(message = "[PANEL] Deleting folder from a panel from queue item (ID: ${queueItem.id})"))
                        val deleteAFolderFromAPanelDTO =
                            Json.decodeFromString<DeleteAFolderFromAPanelDTO>(queueItem.payload)
                        remotePanelsRepo.deleteAFolderFromAPanel(
                            deleteAFolderFromAPanelDTO
                        ).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[PANEL] Removed queue item (ID: ${queueItem.id}) after deleting folder from panel"))
                    }

                    RemoteRoute.Link.DELETE_DUPLICATE_LINKS.name -> {
                        send(Result.Loading(message = "Decoding duplicate links"))
                        val deleteDuplicateLinksDTO =
                            Json.decodeFromString<DeleteDuplicateLinksDTO>(queueItem.payload)

                        send(Result.Loading(message = "Mapping local link IDs to remote IDs"))
                        remoteLinksRepo.deleteDuplicateLinks(deleteDuplicateLinksDTO).also {
                            send(Result.Loading(message = "Deleting duplicate links from remote repository"))
                        }.removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.Folder.MARK_FOLDERS_AS_ROOT.name -> {
                        val markSelectedFoldersAsRootDTO =
                            Json.decodeFromString<MarkSelectedFoldersAsRootDTO>(queueItem.payload)
                        remoteFoldersRepo.markSelectedFoldersAsRoot(markSelectedFoldersAsRootDTO.run {
                            copy(folderIds = this.folderIds.map {
                                localFoldersRepo.getRemoteIdOfAFolder(it) ?: -45454
                            })
                        })
                            .removeQueueItemAndSyncTimestamp(queueItem.id)
                    }
                }
            }
            send(Result.Loading(message = "[SYNC] Completed pushing pending sync queue to server"))
        }
    }

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


        return channelFlow {
            send(Result.Loading("Fetching updates from server..."))
            Network.client.get(baseUrl() + RemoteRoute.SyncInLocalRoute.GET_UPDATES.name) {
                bearerAuth(authToken())
                contentType(ContentType.Application.Json)
                parameter("eventTimestamp", timeStampAfter)
            }.body<AllTablesDTO>().let { remoteResponse ->
                send(Result.Loading("Received updates from server. Processing folders..."))

                remoteResponse.folders.forEach {
                    send(Result.Loading("Processing folder with id: ${it.id}"))
                    applyFolderUpdates(it)
                }

                coroutineScope {
                    awaitAll(async {
                        send(Result.Loading("Processing links..."))
                        remoteResponse.links.forEach { remoteLinkDTO ->
                            send(Result.Loading("Processing link with id: ${remoteLinkDTO.id}"))
                            val localId = localLinksRepo.getLocalLinkId(remoteLinkDTO.id)
                            if (localId == null) {
                                send(Result.Loading("Creating new link with id: ${remoteLinkDTO.id}"))
                                updateLocalDBAccordingToEvent(
                                    WebSocketEvent(
                                        operation = RemoteRoute.Link.CREATE_A_NEW_LINK.name,
                                        payload = json.encodeToJsonElement(
                                            remoteLinkDTO.copy(correlation = randomCorrelation)
                                        )
                                    )
                                )
                            } else {
                                send(Result.Loading("Updating existing link with id: ${remoteLinkDTO.id}"))
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
                        send(Result.Loading("Processing panels..."))
                        remoteResponse.panels.forEach { remotePanelDTO ->
                            send(Result.Loading("Processing panel with id: ${remotePanelDTO.panelId}"))
                            val localId =
                                localPanelsRepo.getLocalPanelId(remotePanelDTO.panelId)
                            if (localId == null) {
                                send(Result.Loading("Creating new panel with id: ${remotePanelDTO.panelId}"))
                                updateLocalDBAccordingToEvent(
                                    WebSocketEvent(
                                        operation = RemoteRoute.Panel.ADD_A_NEW_PANEL.name,
                                        payload = json.encodeToJsonElement(
                                            remotePanelDTO.copy(correlation = randomCorrelation)
                                        )
                                    )
                                )
                            } else {
                                send(Result.Loading("Updating existing panel name for id: ${remotePanelDTO.panelId}"))
                                updateLocalDBAccordingToEvent(
                                    WebSocketEvent(
                                        operation = RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name,
                                        payload = json.encodeToJsonElement(
                                            UpdatePanelNameDTO(
                                                newName = remotePanelDTO.panelName,
                                                panelId = remotePanelDTO.panelId,
                                                correlation = randomCorrelation,
                                                eventTimestamp = remotePanelDTO.eventTimestamp
                                            )
                                        )
                                    )
                                )
                            }
                        }

                        send(Result.Loading("Processing panel folders..."))
                        remoteResponse.panelFolders.forEach { remotePanelFolder ->
                            send(Result.Loading("Adding folder to panel with id: ${remotePanelFolder.id}"))
                            // a panel_folder can only be added with this endpoint response because if it got deleted, it will be caught in the tombstones response, not here
                            if (localPanelsRepo.getLocalPanelFolderId(remotePanelFolder.id) == null) {
                                updateLocalDBAccordingToEvent(
                                    WebSocketEvent(
                                        operation = RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name,
                                        payload = json.encodeToJsonElement(
                                            remotePanelFolder.copy(correlation = randomCorrelation)
                                        )
                                    )
                                )
                            }
                        }
                    })
                }
            }
            send(Result.Success(Unit))
        }.catchAsThrowableAndEmitFailure()
    }

    /**
    parent folders (if any) get inserted first, so there's no need to search and insert parent folders first. also, the server returns from oldest to newest, so we get the advantage of knowing that root folders/parent folders are always inserted first.
     * */
    private suspend fun applyFolderUpdates(
        folderDTO: FolderDTO
    ) {
        val localIdOfCurrentFolder = localFoldersRepo.getLocalIdOfAFolder(folderDTO.id)
        if (localIdOfCurrentFolder != null) {
            localFoldersRepo.updateLocalFolderData(
                Folder(
                    name = folderDTO.name,
                    note = folderDTO.note,
                    parentFolderId = if (folderDTO.parentFolderId != null) localFoldersRepo.getLocalIdOfAFolder(
                        folderDTO.parentFolderId
                    ) else null,
                    localId = localIdOfCurrentFolder,
                    remoteId = folderDTO.id,
                    isArchived = folderDTO.isArchived,
                    lastModified = folderDTO.eventTimestamp
                )
            ).collectAndUpdateTimestamp(folderDTO.eventTimestamp)
        } else {
            localFoldersRepo.insertANewFolder(
                folder = Folder(
                    name = folderDTO.name,
                    note = folderDTO.note,
                    parentFolderId = if (folderDTO.parentFolderId != null) localFoldersRepo.getLocalIdOfAFolder(
                        folderDTO.parentFolderId
                    ) else null,
                    remoteId = folderDTO.id,
                    isArchived = folderDTO.isArchived,
                    lastModified = folderDTO.eventTimestamp
                ), ignoreFolderAlreadyExistsException = true, viaSocket = true
            ).collectAndUpdateTimestamp(folderDTO.eventTimestamp)
        }
    }

    override suspend fun applyUpdatesBasedOnRemoteTombstones(timeStampAfter: Long): Flow<Result<Unit>> {
        return wrappedResultFlow {
            Network.client.get(baseUrl() + RemoteRoute.SyncInLocalRoute.GET_TOMBSTONES.name) {
                bearerAuth(authToken())
                contentType(ContentType.Application.Json)
                parameter("eventTimestamp", timeStampAfter)
            }.body<List<TombstoneDTO>>().map {
                WebSocketEvent(
                    operation = it.operation, payload = it.payload
                )
            }.forEach {
                updateLocalDBAccordingToEvent(it)
            }
        }
    }


    private suspend fun updateLocalDBAccordingToEvent(
        deserializedWebSocketEvent: WebSocketEvent
    ) {
        when (deserializedWebSocketEvent.operation) {

            RemoteRoute.SyncInLocalRoute.DELETE_EVERYTHING.name -> {
                val deleteEverythingDTO =
                    Json.decodeFromJsonElement<DeleteEverythingDTO>(deserializedWebSocketEvent.payload)
                if (deleteEverythingDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        deleteEverythingDTO.eventTimestamp
                    )
                    return
                }
                deleteEverything(deleteOnRemote = false).collectAndUpdateTimestamp(
                    deleteEverythingDTO.eventTimestamp
                )
            }

            RemoteRoute.MultiAction.DELETE_MULTIPLE_ITEMS.name -> {
                val deleteMultipleItemsDTO =
                    Json.decodeFromJsonElement<DeleteMultipleItemsDTO>(deserializedWebSocketEvent.payload)

                if (deleteMultipleItemsDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        deleteMultipleItemsDTO.eventTimestamp
                    )
                    return
                }

                localMultiActionRepo.deleteMultipleItems(linkIds = deleteMultipleItemsDTO.linkIds.map {
                    localLinksRepo.getLocalLinkId(it) ?: -45454
                }, folderIds = deleteMultipleItemsDTO.folderIds.map {
                    localFoldersRepo.getLocalIdOfAFolder(it) ?: -45454
                }, viaSocket = true).collectAndUpdateTimestamp(deleteMultipleItemsDTO.eventTimestamp)
            }

            RemoteRoute.MultiAction.MOVE_EXISTING_ITEMS.name -> {
                val moveItemsDTO =
                    Json.decodeFromJsonElement<MoveItemsDTO>(deserializedWebSocketEvent.payload)

                if (moveItemsDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        moveItemsDTO.eventTimestamp
                    )
                    return
                }

                localMultiActionRepo.moveMultipleItems(
                    viaSocket = true,
                    linkIds = moveItemsDTO.linkIds.map {
                        localLinksRepo.getLocalLinkId(it) ?: -45454
                    },
                    folderIds = moveItemsDTO.folderIds.map {
                        localFoldersRepo.getLocalIdOfAFolder(it) ?: -45454
                    },
                    linkType = moveItemsDTO.linkType,
                    newParentFolderId = localFoldersRepo.getLocalIdOfAFolder(moveItemsDTO.newParentFolderId)
                        ?: -45454
                ).collectAndUpdateTimestamp(moveItemsDTO.eventTimestamp)
            }

            RemoteRoute.MultiAction.ARCHIVE_MULTIPLE_ITEMS.name -> {
                val archiveMoveItemsDTO =
                    Json.decodeFromJsonElement<ArchiveMultipleItemsDTO>(deserializedWebSocketEvent.payload)

                if (archiveMoveItemsDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        archiveMoveItemsDTO.eventTimestamp
                    )
                    return
                }

                localMultiActionRepo.archiveMultipleItems(linkIds = archiveMoveItemsDTO.linkIds.map {
                    localLinksRepo.getLocalLinkId(it) ?: -45454
                }, folderIds = archiveMoveItemsDTO.folderIds.map {
                    localFoldersRepo.getLocalIdOfAFolder(it) ?: -45454
                }, viaSocket = true).collectAndUpdateTimestamp(archiveMoveItemsDTO.eventTimestamp)
            }

            RemoteRoute.Folder.MARK_FOLDERS_AS_ROOT.name -> {
                val markSelectedFoldersAsRootDTO =
                    Json.decodeFromJsonElement<MarkSelectedFoldersAsRootDTO>(
                        deserializedWebSocketEvent.payload
                    )
                if (markSelectedFoldersAsRootDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        markSelectedFoldersAsRootDTO.eventTimestamp
                    )
                    return
                }
                localFoldersRepo.markFoldersAsRoot(
                    folderIDs = markSelectedFoldersAsRootDTO.folderIds.map {
                        localFoldersRepo.getLocalIdOfAFolder(it) ?: -45454
                    }, viaSocket = true
                )
                    .collectAndUpdateTimestamp(markSelectedFoldersAsRootDTO.eventTimestamp)
            }

            RemoteRoute.Link.DELETE_DUPLICATE_LINKS.name -> {
                val deleteDuplicateLinksDTO =
                    Json.decodeFromJsonElement<DeleteDuplicateLinksDTO>(deserializedWebSocketEvent.payload)

                if (deleteDuplicateLinksDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        deleteDuplicateLinksDTO.eventTimestamp
                    )
                    return
                }

                localLinksRepo.deleteLinksLocally(deleteDuplicateLinksDTO.linkIds.map {
                    localLinksRepo.getLocalLinkId(it) ?: -45454
                }).collectAndUpdateTimestamp(deleteDuplicateLinksDTO.eventTimestamp)
            }

            // folders:

            RemoteRoute.Folder.CREATE_FOLDER.name -> {
                val folderDto = json.decodeFromJsonElement<FolderDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (folderDto.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(folderDto.eventTimestamp)
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
                        isArchived = folderDto.isArchived,
                        lastModified = folderDto.eventTimestamp
                    ), ignoreFolderAlreadyExistsException = true, viaSocket = true
                ).collectAndUpdateTimestamp(folderDto.eventTimestamp)
            }

            RemoteRoute.Folder.DELETE_FOLDER.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (folderId != null) {
                    localFoldersRepo.deleteAFolder(folderId, viaSocket = true)
                        .collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (folderId != null) {
                    localFoldersRepo.markFolderAsArchive(
                        folderId, viaSocket = true
                    ).collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (folderId != null) {
                    localFoldersRepo.markFolderAsRegularFolder(
                        folderId, viaSocket = true
                    ).collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Folder.UPDATE_FOLDER_NAME.name -> {
                val updateFolderNameDTO = json.decodeFromJsonElement<UpdateFolderNameDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (updateFolderNameDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(updateFolderNameDTO.eventTimestamp)
                    return
                }

                val localFolderId =
                    localFoldersRepo.getLocalIdOfAFolder(updateFolderNameDTO.folderId)
                if (localFolderId != null) {
                    localFoldersRepo.getThisFolderData(localFolderId).collectLatest {
                        it.onSuccess {
                            localFoldersRepo.updateLocalFolderData(
                                it.data.copy(
                                    localId = localFolderId,
                                    name = updateFolderNameDTO.newFolderName
                                )
                            ).collect()
                            preferencesRepository.updateLastSyncedWithServerTimeStamp(
                                updateFolderNameDTO.eventTimestamp
                            )
                        }
                    }
                }
            }

            RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name -> {
                val updateFolderNoteDTO = json.decodeFromJsonElement<UpdateFolderNoteDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (updateFolderNoteDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(updateFolderNoteDTO.eventTimestamp)
                    return
                }

                val localFolderId =
                    localFoldersRepo.getLocalIdOfAFolder(updateFolderNoteDTO.folderId)
                if (localFolderId != null) {
                    localFoldersRepo.getThisFolderData(localFolderId).collectLatest {
                        it.onSuccess {
                            localFoldersRepo.updateLocalFolderData(
                                it.data.copy(
                                    localId = localFolderId, note = updateFolderNoteDTO.newNote
                                )
                            ).collect()
                            preferencesRepository.updateLastSyncedWithServerTimeStamp(
                                updateFolderNoteDTO.eventTimestamp
                            )
                        }
                    }
                }
            }

            RemoteRoute.Folder.DELETE_FOLDER_NOTE.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )
                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localFolderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (localFolderId != null) {
                    localFoldersRepo.getThisFolderData(localFolderId).collectLatest {
                        it.onSuccess {
                            localFoldersRepo.deleteAFolderNote(
                                localFolderId, viaSocket = true
                            ).collect()
                            preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                        }
                    }
                }
            }


            // links:

            RemoteRoute.Link.UPDATE_LINK_TITLE.name -> {
                val updateTitleOfTheLinkDTO = json.decodeFromJsonElement<UpdateTitleOfTheLinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (updateTitleOfTheLinkDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        updateTitleOfTheLinkDTO.eventTimestamp
                    )
                    return
                }

                val localLinkId = localLinksRepo.getLocalLinkId(updateTitleOfTheLinkDTO.linkId)
                if (localLinkId != null) {
                    localLinksRepo.updateLinkTitle(
                        localLinkId, updateTitleOfTheLinkDTO.newTitleOfTheLink, viaSocket = true
                    ).collectAndUpdateTimestamp(updateTitleOfTheLinkDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.UPDATE_LINK_NOTE.name -> {
                val updateNoteOfALinkDTO = json.decodeFromJsonElement<UpdateNoteOfALinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (updateNoteOfALinkDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(updateNoteOfALinkDTO.eventTimestamp)
                    return
                }

                val localLinkId = localLinksRepo.getLocalLinkId(updateNoteOfALinkDTO.linkId)
                if (localLinkId != null) {
                    localLinksRepo.updateLinkNote(
                        localLinkId, updateNoteOfALinkDTO.newNote, viaSocket = true
                    ).collectAndUpdateTimestamp(updateNoteOfALinkDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.DELETE_A_LINK.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    localLinksRepo.deleteALink(localLinkId, viaSocket = true)
                        .collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.ARCHIVE_LINK.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    localLinksRepo.archiveALink(localLinkId, viaSocket = true)
                        .collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.UNARCHIVE_LINK.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    val link = localLinksRepo.getALink(localLinkId)
                    localLinksRepo.updateALink(
                        link.copy(linkType = LinkType.SAVED_LINK), viaSocket = true
                    ).collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.MARK_AS_IMP.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    val link = localLinksRepo.getALink(localLinkId)
                    localLinksRepo.updateALink(
                        link.copy(markedAsImportant = true), viaSocket = true
                    ).collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.UNMARK_AS_IMP.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localLinkId = localLinksRepo.getLocalLinkId(idBasedDTO.id)
                if (localLinkId != null) {
                    val link = localLinksRepo.getALink(localLinkId)
                    localLinksRepo.updateALink(
                        link.copy(markedAsImportant = false), viaSocket = true
                    ).collectAndUpdateTimestamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.UPDATE_LINK.name -> {
                val linkDTO = json.decodeFromJsonElement<LinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (linkDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(linkDTO.eventTimestamp)
                    return
                }

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
                    ).collectAndUpdateTimestamp(linkDTO.eventTimestamp)
                }
            }

            RemoteRoute.Link.CREATE_A_NEW_LINK.name -> {
                val linkDTO = json.decodeFromJsonElement<LinkDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (linkDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(linkDTO.eventTimestamp)
                    return
                }

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
                        mediaType = linkDTO.mediaType,
                        lastModified = linkDTO.eventTimestamp
                    ), linkSaveConfig = forceSaveWithoutRetrieving(), viaSocket = true
                ).collectAndUpdateTimestamp(linkDTO.eventTimestamp)
            }


            // panels:

            RemoteRoute.Panel.ADD_A_NEW_PANEL.name -> {
                val panelDTO =
                    json.decodeFromJsonElement<PanelDTO>(deserializedWebSocketEvent.payload)

                if (panelDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(panelDTO.eventTimestamp)
                    return
                }

                localPanelsRepo.addaNewPanel(
                    Panel(
                        panelName = panelDTO.panelName,
                        remoteId = panelDTO.panelId,
                        lastModified = panelDTO.eventTimestamp
                    ), viaSocket = true
                ).collectAndUpdateTimestamp(panelDTO.eventTimestamp)
            }

            RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name -> {
                val panelFolderDTO = json.decodeFromJsonElement<PanelFolderDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (panelFolderDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(panelFolderDTO.eventTimestamp)
                    return
                }

                val localFolderId = localFoldersRepo.getLocalIdOfAFolder(panelFolderDTO.folderId)
                val localPanelsId = localPanelsRepo.getLocalPanelId(panelFolderDTO.connectedPanelId)
                if (localPanelsId != null && localFolderId != null) {
                    localPanelsRepo.addANewFolderInAPanel(
                        PanelFolder(
                            folderId = localFolderId,
                            panelPosition = panelFolderDTO.panelPosition,
                            folderName = panelFolderDTO.folderName,
                            connectedPanelId = localPanelsId,
                            remoteId = panelFolderDTO.id,
                            lastModified = panelFolderDTO.eventTimestamp
                        ), viaSocket = true
                    ).collectAndUpdateTimestamp(panelFolderDTO.eventTimestamp)
                }
            }

            RemoteRoute.Panel.DELETE_A_PANEL.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localPanelId = localPanelsRepo.getLocalPanelId(idBasedDTO.id)
                if (localPanelId != null) {
                    localPanelsRepo.deleteAPanel(localPanelId, viaSocket = true)
                        .collectAndUpdateTimestamp(
                            idBasedDTO.eventTimestamp
                        )
                }
            }

            RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name -> {
                val updatePanelNameDTO = json.decodeFromJsonElement<UpdatePanelNameDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (updatePanelNameDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(updatePanelNameDTO.eventTimestamp)
                    return
                }

                val localPanelId = localPanelsRepo.getLocalPanelId(updatePanelNameDTO.panelId)
                if (localPanelId != null) {
                    localPanelsRepo.updateAPanelName(
                        newName = updatePanelNameDTO.newName,
                        panelId = localPanelId,
                        viaSocket = true
                    ).collectAndUpdateTimestamp(updatePanelNameDTO.eventTimestamp)
                }
            }

            RemoteRoute.Panel.DELETE_A_FOLDER_FROM_ALL_PANELS.name -> {
                val idBasedDTO = json.decodeFromJsonElement<IDBasedDTO>(
                    deserializedWebSocketEvent.payload
                )

                if (idBasedDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                    return
                }

                val localFolderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                if (localFolderId != null) {
                    localPanelsRepo.deleteAFolderFromAllPanels(localFolderId)
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(idBasedDTO.eventTimestamp)
                }
            }

            RemoteRoute.Panel.DELETE_A_FOLDER_FROM_A_PANEL.name -> {
                val deleteAFolderFromAPanelDTO =
                    json.decodeFromJsonElement<DeleteAFolderFromAPanelDTO>(
                        deserializedWebSocketEvent.payload
                    )

                if (deleteAFolderFromAPanelDTO.correlation.isSameAsCurrentClient()) {
                    preferencesRepository.updateLastSyncedWithServerTimeStamp(
                        deleteAFolderFromAPanelDTO.eventTimestamp
                    )
                    return
                }

                val localFolderId =
                    localFoldersRepo.getLocalIdOfAFolder(deleteAFolderFromAPanelDTO.folderID)
                val localPanelId =
                    localPanelsRepo.getLocalPanelId(deleteAFolderFromAPanelDTO.panelId)
                if (localFolderId != null && localPanelId != null) {
                    localPanelsRepo.deleteAFolderFromAPanel(
                        localPanelId, localFolderId, viaSocket = true
                    ).collectAndUpdateTimestamp(deleteAFolderFromAPanelDTO.eventTimestamp)
                }
            }
        }
    }

    override suspend fun <T> SendChannel<Result<T>>.pushNonSyncedDataToServer() {
        send(Result.Loading(message = "[SYNC] Starting non-synced data push to server"))

        send(Result.Loading(message = "[FOLDERS] Fetching unsynced folders"))
        localFoldersRepo.getUnSyncedFolders().forEach { currentFolder ->
            send(Result.Loading(message = "[FOLDERS] Processing folder (ID: ${currentFolder.localId}, Name: ${currentFolder.name})"))
            pendingSyncQueueRepo.addInQueue(
                PendingSyncQueue(
                    operation = RemoteRoute.Folder.CREATE_FOLDER.name,
                    payload = Json.encodeToString(
                        currentFolder.asAddFolderDTO()
                            .copy(offlineSyncItemId = currentFolder.localId)
                    )
                )
            )
            send(Result.Loading(message = "[FOLDERS] Queued folder (ID: ${currentFolder.localId}) for sync"))
        }

        send(Result.Loading(message = "[LINKS] Fetching unsynced links"))
        localLinksRepo.getUnSyncedLinks().forEach { currentLink ->
            send(Result.Loading(message = "[LINKS] Processing link (ID: ${currentLink.localId}, Title: ${currentLink.title})"))
            pendingSyncQueueRepo.addInQueue(
                PendingSyncQueue(
                    operation = RemoteRoute.Link.CREATE_A_NEW_LINK.name,
                    payload = Json.encodeToString(
                        currentLink.asAddLinkDTO().copy(offlineSyncItemId = currentLink.localId)
                    )
                )
            )
            send(Result.Loading(message = "[LINKS] Queued link (ID: ${currentLink.localId}) for sync"))
        }

        send(Result.Loading(message = "[PANELS] Fetching unsynced panels"))
        localPanelsRepo.getUnSyncedPanels().forEach { currentPanel ->
            send(Result.Loading(message = "[PANELS] Processing panel (ID: ${currentPanel.localId}, Name: ${currentPanel.panelName})"))
            pendingSyncQueueRepo.addInQueue(
                PendingSyncQueue(
                    operation = RemoteRoute.Panel.ADD_A_NEW_PANEL.name,
                    payload = Json.encodeToString(
                        AddANewPanelDTO(
                            panelName = currentPanel.panelName,
                            offlineSyncItemId = currentPanel.localId,
                            eventTimestamp = currentPanel.lastModified
                        )
                    )
                )
            )
            send(Result.Loading(message = "[PANELS] Queued panel (ID: ${currentPanel.localId}) for sync"))
        }

        send(Result.Loading(message = "[PANEL FOLDERS] Fetching unsynced panel folders"))
        localPanelsRepo.getUnSyncedPanelFolders().forEach { currentPanelFolder ->
            send(Result.Loading(message = "[PANEL FOLDERS] Processing panel folder (ID: ${currentPanelFolder.localId}, Panel ID: ${currentPanelFolder.connectedPanelId})"))
            pendingSyncQueueRepo.addInQueue(
                PendingSyncQueue(
                    operation = RemoteRoute.Panel.ADD_A_NEW_FOLDER_IN_A_PANEL.name,
                    payload = Json.encodeToString(
                        AddANewPanelFolderDTO(
                            folderId = currentPanelFolder.folderId,
                            panelPosition = currentPanelFolder.panelPosition,
                            folderName = currentPanelFolder.folderName,
                            connectedPanelId = currentPanelFolder.connectedPanelId,
                            offlineSyncItemId = currentPanelFolder.localId,
                            eventTimestamp = currentPanelFolder.lastModified
                        )
                    )
                )
            )
            send(Result.Loading(message = "[PANEL FOLDERS] Queued panel folder (ID: ${currentPanelFolder.localId}) for sync"))
        }

        send(Result.Loading(message = "[SYNC] Pushing queued items to server"))
        pushPendingSyncQueueToServer().collect()
    }

    override suspend fun deleteEverything(deleteOnRemote: Boolean): Flow<Result<Unit>> {
        return performLocalOperationWithRemoteSyncFlow<Unit, DeleteEverythingDTO>(performRemoteOperation = deleteOnRemote,
            remoteOperation = {
                postFlow(
                    httpClient = Network.client,
                    baseUrl = baseUrl,
                    authToken = authToken,
                    endPoint = RemoteRoute.SyncInLocalRoute.DELETE_EVERYTHING.name,
                    body = DeleteEverythingDTO()
                )
            },
            onRemoteOperationFailure = {
                TODO()
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            }) {
            supervisorScope {
                listOf(launch {
                    localLinksRepo.deleteAllLinks()
                }, launch {
                    localFoldersRepo.deleteAllFolders()
                }, launch {
                    localPanelsRepo.deleteAllPanels()
                    preferencesRepository.changePreferenceValue(
                        preferenceKey = longPreferencesKey(
                            AppPreferenceType.LAST_SELECTED_PANEL_ID.name
                        ), newValue = Constants.DEFAULT_PANELS_ID
                    )
                }, launch {
                    localPanelsRepo.deleteAllPanelFolders()
                }, launch {
                    pendingSyncQueueRepo.deleteAllItems()
                })
            }
        }
    }
}