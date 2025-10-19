package com.sakethh.linkora.data.remote.repository.sync

import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.data.local.dao.TagsDao
import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.CopyItemsDTO
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.MarkItemsRegularDTO
import com.sakethh.linkora.domain.dto.server.MoveItemsDTO
import com.sakethh.linkora.domain.dto.server.TimeStampBasedResponse
import com.sakethh.linkora.domain.dto.server.folder.AddFolderDTO
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
import com.sakethh.linkora.domain.dto.server.panel.UpdatePanelNameDTO
import com.sakethh.linkora.domain.dto.server.tag.CreateTagDTO
import com.sakethh.linkora.domain.dto.server.tag.RenameTagDTO
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteFoldersRepo
import com.sakethh.linkora.domain.repository.remote.RemoteLinksRepo
import com.sakethh.linkora.domain.repository.remote.RemoteMultiActionRepo
import com.sakethh.linkora.domain.repository.remote.RemotePanelsRepo
import com.sakethh.linkora.domain.repository.remote.RemoteTagsRepo
import com.sakethh.linkora.utils.Utils
import com.sakethh.linkora.utils.updateLastSyncedWithServerTimeStamp
import com.sakethh.linkora.utils.wrappedResultFlow
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json

class PendingQueueService(
    private val localFoldersRepo: LocalFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val remoteFoldersRepo: RemoteFoldersRepo,
    private val remoteLinksRepo: RemoteLinksRepo,
    private val remotePanelsRepo: RemotePanelsRepo,
    private val preferencesRepository: PreferencesRepository,
    private val remoteMultiActionRepo: RemoteMultiActionRepo,
    private val remoteTagsRepo: RemoteTagsRepo,
    private val linksDao: LinksDao,
    private val foldersDao: FoldersDao,
    private val tagsDao: TagsDao
) {

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

    fun <T> SendChannel<Result<T>>.pushPendingSyncQueueToServer(): Flow<Result<Unit>> {
        return wrappedResultFlow {
            send(Result.Loading(message = "[SYNC] Starting to push pending sync queue to server"))

            send(Result.Loading(message = "[QUEUE] Fetching all items from the pending sync queue"))
            pendingSyncQueueRepo.getAllItemsFromQueue().forEach { queueItem ->
                send(Result.Loading(message = "[QUEUE] Processing queue item (ID: ${queueItem.id}, Operation: ${queueItem.operation})"))

                when (queueItem.operation) {
                    RemoteRoute.Tag.CREATE_TAG.name -> {
                        val createTagDTO =
                            Utils.json.decodeFromString<CreateTagDTO>(queueItem.payload)
                        remoteTagsRepo.createATag(createTagDTO).collect {
                            it.onSuccess {
                                val remoteTagId = it.data.id
                                tagsDao.updateRemoteId(
                                    localId = createTagDTO.offlineSyncItemId,
                                    newRemoteId = remoteTagId
                                )
                                pendingSyncQueueRepo.removeFromQueue(queueItem.id)
                                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.data.timeStampBasedResponse.eventTimestamp)
                            }
                        }
                    }

                    RemoteRoute.Tag.DELETE_TAG.name -> {
                        val idBasedDTO = Utils.json.decodeFromString<IDBasedDTO>(queueItem.payload)
                        remoteTagsRepo.deleteATag(idBasedDTO)
                            .removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.Tag.RENAME_TAG.name -> {
                        val renameTagDTO =
                            Utils.json.decodeFromString<RenameTagDTO>(queueItem.payload)
                        remoteTagsRepo.renameATag(renameTagDTO)
                            .removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.MultiAction.COPY_EXISTING_ITEMS.name -> {
                        val copiedFoldersDTO =
                            Json.Default.decodeFromString<CopyItemsDTO>(queueItem.payload)
                        remoteMultiActionRepo.copyMultipleItems(copiedFoldersDTO).collect {
                            it.onSuccess {
                                it.data.linkIds.forEach {
                                    linksDao.updateRemoteLinkId(it.key, it.value)
                                }
                                it.data.folders.forEach {
                                    foldersDao.updateARemoteLinkId(
                                        it.currentFolder.localId, it.currentFolder.remoteId
                                    )
                                    it.links.forEach {
                                        linksDao.updateRemoteLinkId(it.localId, it.remoteId)
                                    }
                                }
                                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.data.eventTimestamp)
                            }
                        }
                    }

                    RemoteRoute.MultiAction.UNARCHIVE_MULTIPLE_ITEMS.name -> {
                        val markItemsRegularDTO =
                            Json.Default.decodeFromString<MarkItemsRegularDTO>(queueItem.payload)
                        remoteMultiActionRepo.markItemsAsRegular(markItemsRegularDTO.run {
                            copy(foldersIds = foldersIds.map {
                                localFoldersRepo.getRemoteIdOfAFolder(it) ?: -45454
                            }, linkIds = linkIds.map {
                                localLinksRepo.getLocalLinkId(it) ?: -45454
                            })
                        }).removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.MultiAction.DELETE_MULTIPLE_ITEMS.name -> {
                        val deleteMultipleItemsDTO =
                            Json.Default.decodeFromString<DeleteMultipleItemsDTO>(queueItem.payload)
                        remoteMultiActionRepo.deleteMultipleItems(deleteMultipleItemsDTO)
                            .removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.MultiAction.ARCHIVE_MULTIPLE_ITEMS.name -> {
                        val archiveMultipleItemsDTO =
                            Json.Default.decodeFromString<ArchiveMultipleItemsDTO>(queueItem.payload)
                        remoteMultiActionRepo.archiveMultipleItems(archiveMultipleItemsDTO.run {
                            copy(linkIds = linkIds.map {
                                localLinksRepo.getRemoteLinkId(it) ?: -45454
                            }, folderIds = folderIds.map {
                                localFoldersRepo.getRemoteIdOfAFolder(it) ?: -45454
                            })
                        }).removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.MultiAction.MOVE_EXISTING_ITEMS.name -> {
                        val moveItemsDTO =
                            Json.Default.decodeFromString<MoveItemsDTO>(queueItem.payload)
                        remoteMultiActionRepo.moveMultipleItems(moveItemsDTO.run {
                            this.copy(
                                folderIds = folderIds.map {
                                localFoldersRepo.getRemoteIdOfAFolder(it) ?: -45454
                            }, linkIds = linkIds.map {
                                localLinksRepo.getRemoteLinkId(it) ?: -45454
                            }, newParentFolderId = localFoldersRepo.getRemoteIdOfAFolder(
                                newParentFolderId
                            ) ?: -45454
                            )
                        }).removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.Folder.CREATE_FOLDER.name -> {
                        send(Result.Loading(message = "[FOLDER] Creating folder from queue item (ID: ${queueItem.id})"))
                        val addFolderDTO =
                            Json.Default.decodeFromString<AddFolderDTO>(queueItem.payload)
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
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
                        remoteFoldersRepo.deleteFolder(idBasedDTO).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after deleting folder"))
                    }

                    RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name -> {
                        send(Result.Loading(message = "[FOLDER] Marking folder as archive from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteId = localFoldersRepo.getRemoteIdOfAFolder(idBasedDTO.id)!!
                        remoteFoldersRepo.markAsArchive(idBasedDTO.copy(id = remoteId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after marking folder as archive"))
                    }

                    RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name -> {
                        send(Result.Loading(message = "[FOLDER] Marking folder as regular from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
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
                            Json.Default.decodeFromString<UpdateFolderNameDTO>(queueItem.payload)
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
                            Json.Default.decodeFromString<UpdateFolderNoteDTO>(queueItem.payload)
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
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteId = localFoldersRepo.getRemoteIdOfAFolder(idBasedDTO.id)!!
                        remoteFoldersRepo.deleteFolderNote(idBasedDTO.copy(id = remoteId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[FOLDER] Removed queue item (ID: ${queueItem.id}) after deleting folder note"))
                    }

                    RemoteRoute.Link.UPDATE_LINK_TITLE.name -> {
                        send(Result.Loading(message = "[LINK] Updating link title from queue item (ID: ${queueItem.id})"))
                        val updateTitleOfTheLinkDTO = Json.Default.decodeFromString<UpdateTitleOfTheLinkDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(updateTitleOfTheLinkDTO.linkId)!!
                        remoteLinksRepo.updateLinkTitle(
                            updateTitleOfTheLinkDTO.copy(linkId = remoteLinkId)
                        ).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after updating link title"))
                    }

                    RemoteRoute.Link.UPDATE_LINK_NOTE.name -> {
                        send(Result.Loading(message = "[LINK] Updating link note from queue item (ID: ${queueItem.id})"))
                        val updateNoteOfALinkDTO = Json.Default.decodeFromString<UpdateNoteOfALinkDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(updateNoteOfALinkDTO.linkId)!!
                        remoteLinksRepo.updateALinkNote(updateNoteOfALinkDTO.copy(linkId =  remoteLinkId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after updating link note"))
                    }

                    RemoteRoute.Link.DELETE_A_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Deleting link from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
                        remoteLinksRepo.deleteALink(idBasedDTO).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after deleting link"))
                    }

                    RemoteRoute.Link.ARCHIVE_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Archiving link from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(idBasedDTO.id)!!
                        remoteLinksRepo.archiveALink(idBasedDTO.copy(id = remoteLinkId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after archiving link"))
                    }

                    RemoteRoute.Link.UNARCHIVE_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Unarchiving link from queue item (ID: ${queueItem.id})"))
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
                        val remoteLinkId = localLinksRepo.getRemoteLinkId(idBasedDTO.id)!!
                        remoteLinksRepo.unArchiveALink(idBasedDTO.copy(id = remoteLinkId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after unarchiving link"))
                    }

                    RemoteRoute.Link.UPDATE_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Updating link from queue item (ID: ${queueItem.id})"))
                        val linkDTO = Json.Default.decodeFromString<LinkDTO>(queueItem.payload)
                        val remoteId = localLinksRepo.getRemoteLinkId(linkDTO.id)!!
                        remoteLinksRepo.updateLink(linkDTO.copy(id = remoteId))
                            .removeQueueItemAndSyncTimestamp(
                                queueItem.id
                            )
                        send(Result.Loading(message = "[LINK] Removed queue item (ID: ${queueItem.id}) after updating link"))
                    }

                    RemoteRoute.Link.CREATE_A_NEW_LINK.name -> {
                        send(Result.Loading(message = "[LINK] Creating new link from queue item (ID: ${queueItem.id})"))
                        val addLinkDTO =
                            Json.Default.decodeFromString<AddLinkDTO>(queueItem.payload)
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
                                localLinksRepo.updateALink(
                                    updatedLink, updatedLinkTagsPair = null, viaSocket = true
                                ).collect()
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
                            Json.Default.decodeFromString<AddANewPanelDTO>(queueItem.payload)
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
                            Json.Default.decodeFromString<AddANewPanelFolderDTO>(queueItem.payload)
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
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
                        remotePanelsRepo.deleteAPanel(idBasedDTO).removeQueueItemAndSyncTimestamp(
                            queueItem.id
                        )
                        send(Result.Loading(message = "[PANEL] Removed queue item (ID: ${queueItem.id}) after deleting panel"))
                    }

                    RemoteRoute.Panel.UPDATE_A_PANEL_NAME.name -> {
                        send(Result.Loading(message = "[PANEL] Updating panel name from queue item (ID: ${queueItem.id})"))
                        val updatePanelNameDTO =
                            Json.Default.decodeFromString<UpdatePanelNameDTO>(queueItem.payload)
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
                        val idBasedDTO =
                            Json.Default.decodeFromString<IDBasedDTO>(queueItem.payload)
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
                            Json.Default.decodeFromString<DeleteAFolderFromAPanelDTO>(queueItem.payload)
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
                            Json.Default.decodeFromString<DeleteDuplicateLinksDTO>(queueItem.payload)

                        send(Result.Loading(message = "Mapping local link IDs to remote IDs"))
                        remoteLinksRepo.deleteDuplicateLinks(deleteDuplicateLinksDTO).also {
                            send(Result.Loading(message = "Deleting duplicate links from remote repository"))
                        }.removeQueueItemAndSyncTimestamp(queueItem.id)
                    }

                    RemoteRoute.Folder.MARK_FOLDERS_AS_ROOT.name -> {
                        val markSelectedFoldersAsRootDTO =
                            Json.Default.decodeFromString<MarkSelectedFoldersAsRootDTO>(queueItem.payload)
                        remoteFoldersRepo.markSelectedFoldersAsRoot(markSelectedFoldersAsRootDTO.run {
                            copy(folderIds = this.folderIds.map {
                                localFoldersRepo.getRemoteIdOfAFolder(it) ?: -45454
                            })
                        }).removeQueueItemAndSyncTimestamp(queueItem.id)
                    }
                }
            }
            send(Result.Loading(message = "[SYNC] Completed pushing pending sync queue to server"))
        }
    }

}