package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.CopyFolderDTO
import com.sakethh.linkora.domain.dto.server.CopyItemsDTO
import com.sakethh.linkora.domain.dto.server.CurrentFolder
import com.sakethh.linkora.domain.dto.server.FolderLink
import com.sakethh.linkora.domain.dto.server.MarkItemsRegularDTO
import com.sakethh.linkora.domain.dto.server.MoveItemsDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.model.tag.LinkTag
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalMultiActionRepo
import com.sakethh.linkora.domain.repository.local.LocalTagsRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteMultiActionRepo
import com.sakethh.linkora.ui.domain.model.LinkTagsPair
import com.sakethh.linkora.utils.getSystemEpochSeconds
import com.sakethh.linkora.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.utils.updateLastSyncedWithServerTimeStamp
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.json.Json

class LocalMultiActionRepoImpl(
    private val linksDao: LinksDao,
    private val foldersDao: FoldersDao,
    private val preferencesRepository: PreferencesRepository,
    private val remoteMultiActionRepo: RemoteMultiActionRepo,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val localFoldersRepo: LocalFoldersRepo,
    private val localTagsRepo: LocalTagsRepo
) : LocalMultiActionRepo {
    override suspend fun archiveMultipleItems(
        linkIds: List<Long>, folderIds: List<Long>, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = getSystemEpochSeconds()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                val remoteLinkIds = linksDao.getRemoteIds(linkIds)
                val remoteFolderIds = foldersDao.getRemoteIds(folderIds)
                require(remoteFolderIds != null && remoteLinkIds != null)

                remoteMultiActionRepo.archiveMultipleItems(
                    ArchiveMultipleItemsDTO(
                        linkIds = remoteLinkIds,
                        folderIds = remoteFolderIds,
                        eventTimestamp = eventTimestamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.MultiAction.ARCHIVE_MULTIPLE_ITEMS.name,
                        payload = Json.encodeToString(
                            ArchiveMultipleItemsDTO(
                                linkIds = linkIds,
                                folderIds = folderIds,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            coroutineScope {
                awaitAll(async {
                    linksDao.archiveMultipleLinks(linkIds, eventTimestamp)
                }, async {
                    foldersDao.markMultipleFoldersAsArchive(folderIds, eventTimestamp)
                })
            }
        }
    }

    override suspend fun deleteMultipleItems(
        linkIds: List<Long>, folderIds: List<Long>, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = getSystemEpochSeconds()
        val remoteLinkIds = linksDao.getRemoteIds(linkIds)
        val remoteFolderIds = foldersDao.getRemoteIds(folderIds)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                require(remoteFolderIds != null && remoteLinkIds != null)
                remoteMultiActionRepo.deleteMultipleItems(
                    DeleteMultipleItemsDTO(
                        linkIds = remoteLinkIds,
                        folderIds = remoteFolderIds,
                        eventTimestamp = eventTimestamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (remoteFolderIds != null && remoteLinkIds != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.MultiAction.DELETE_MULTIPLE_ITEMS.name,
                            payload = Json.encodeToString(
                                DeleteMultipleItemsDTO(
                                    linkIds = remoteLinkIds,
                                    folderIds = remoteFolderIds,
                                    eventTimestamp = eventTimestamp
                                )
                            )
                        )
                    )
                }
            }) {
            coroutineScope {
                awaitAll(async {
                    linksDao.deleteMultipleLinks(linkIds)
                }, async {
                    localFoldersRepo.deleteMultipleFolders(folderIds, viaSocket = true).collect()
                })
            }
        }
    }

    override suspend fun moveMultipleItems(
        linkIds: List<Long>,
        folderIds: List<Long>,
        linkType: LinkType,
        newParentFolderId: Long,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimeStamp = getSystemEpochSeconds()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                val remoteLinkIds = linksDao.getRemoteIds(linkIds)
                val remoteFolderIds = foldersDao.getRemoteIds(folderIds)
                val remoteParentFolderId = foldersDao.getRemoteFolderId(newParentFolderId)

                require(remoteFolderIds != null && remoteLinkIds != null && remoteParentFolderId != null)

                remoteMultiActionRepo.moveMultipleItems(
                    MoveItemsDTO(
                        folderIds = remoteFolderIds,
                        linkIds = remoteLinkIds,
                        linkType = linkType,
                        newParentFolderId = remoteParentFolderId,
                        eventTimestamp = eventTimeStamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.MultiAction.MOVE_EXISTING_ITEMS.name,
                        payload = Json.encodeToString(
                            MoveItemsDTO(
                                folderIds = folderIds,
                                linkIds = linkIds,
                                linkType = linkType,
                                newParentFolderId = newParentFolderId,
                                eventTimestamp = eventTimeStamp
                            )
                        )
                    )
                )
            }) {
            coroutineScope {
                awaitAll(async {
                    foldersDao.moveFolders(newParentFolderId, folderIds, eventTimeStamp)
                }, async {
                    linksDao.moveLinks(newParentFolderId, linkType, linkIds, eventTimeStamp)
                })
            }
        }
    }


    override suspend fun copyMultipleItems(
        linkTagsPairs: List<LinkTagsPair>,
        folders: List<Folder>,
        linkType: LinkType,
        newParentFolderId: Long,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimeStamp = getSystemEpochSeconds()

        val copiedLinksIds = mutableMapOf<Long, Long>()

        lateinit var copiedFolders: List<CopyFolderDTO>

        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = !viaSocket,
            remoteOperation = {
                val remoteParentFolderId = foldersDao.getRemoteFolderId(newParentFolderId)
                require(remoteParentFolderId != null)
                remoteMultiActionRepo.copyMultipleItems(
                    CopyItemsDTO(
                        folders = copiedFolders,
                        linkIds = copiedLinksIds.toMap(),
                        linkType = linkType,
                        newParentFolderId = remoteParentFolderId,
                        eventTimestamp = eventTimeStamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                it.linkIds.forEach {
                    linksDao.updateRemoteLinkId(it.key, it.value)
                }
                it.folders.forEach {
                    foldersDao.updateARemoteLinkId(
                        it.currentFolder.localId, it.currentFolder.remoteId
                    )
                    it.links.forEach {
                        linksDao.updateRemoteLinkId(it.localId, it.remoteId)
                    }
                }
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.MultiAction.COPY_EXISTING_ITEMS.name,
                        payload = Json.encodeToString(
                            CopyItemsDTO(
                                folders = copiedFolders,
                                linkIds = copiedLinksIds.toMap(),
                                linkType = linkType,
                                newParentFolderId = newParentFolderId,
                                eventTimestamp = eventTimeStamp
                            )
                        )
                    )
                )
            }) {
            coroutineScope {
                awaitAll(async {
                    val newLinks = linkTagsPairs.map {
                        it.link.copy(
                            idOfLinkedFolder = newParentFolderId,
                            linkType = linkType,
                            localId = 0,
                            remoteId = null,
                            lastModified = eventTimeStamp
                        )
                    }
                    linksDao.addMultipleLinks(newLinks).forEachIndexed { index, newLinkId ->
                        copiedLinksIds.put(
                            newLinkId, linkTagsPairs[index].link.remoteId ?: -45454
                        )
                        val newLinkTags = linkTagsPairs[index].tags.map {
                            LinkTag(linkId = newLinkId, tagId = it.localId)
                        }
                        localTagsRepo.createLinkTags(newLinkTags)
                    }
                }, async {
                    copiedFolders = copyFolders(
                        parentFolderId = newParentFolderId, folders = folders, eventTimeStamp
                    )
                })
            }
        }
    }

    private suspend fun copyFolders(
        parentFolderId: Long, folders: List<Folder>, eventTimestamp: Long
    ): List<CopyFolderDTO> = coroutineScope {
        val deferredCopiedFolders = folders.map { folder ->
            async {
                val newFolderId = foldersDao.insertANewFolder(
                    folder.copy(
                        parentFolderId = parentFolderId,
                        remoteId = null,
                        localId = 0,
                        lastModified = eventTimestamp
                    )
                )
                val linksOfCurrentFolder = linksDao.getLinksOfThisFolderAsList(
                    folder.localId
                )
                val linksIdsOfCopiedFolder = linksDao.addMultipleLinks(
                    linksOfCurrentFolder.map {
                        it.copy(
                            idOfLinkedFolder = newFolderId,
                            localId = 0,
                            lastModified = eventTimestamp
                        )
                    })

                if (linksOfCurrentFolder.isNotEmpty()) {
                    val tagsForCurrentLinksBatch =
                        localTagsRepo.getTagsForLinksAsMap(linksOfCurrentFolder.map { it.localId })


                    val linkIdsMap =
                        linksOfCurrentFolder.map { it.localId }.zip(linksIdsOfCopiedFolder).toMap()

                    val newLinkTags = tagsForCurrentLinksBatch.flatMap { (oldLinkId, tags) ->
                        val newLinkId = linkIdsMap[oldLinkId]
                        if (newLinkId != null) {
                            tags.map {
                                LinkTag(linkId = newLinkId, tagId = it.localId)
                            }
                        } else {
                            emptyList()
                        }
                    }
                    localTagsRepo.createLinkTags(newLinkTags)
                }
                if (folder.remoteId != null) {
                    CopyFolderDTO(
                        currentFolder = CurrentFolder(
                            localId = newFolderId, remoteId = folder.remoteId
                        ), links = linksIdsOfCopiedFolder.zip(linksOfCurrentFolder) { id, link ->
                            if (link.remoteId != null) {
                                FolderLink(
                                    localId = id, remoteId = link.remoteId
                                )
                            } else {
                                null
                            }
                        }.filterNotNull(), childFolders = copyFolders(
                            parentFolderId = newFolderId,
                            folders = foldersDao.getChildFoldersAsList(folder.localId),
                            eventTimestamp
                        )
                    )
                } else {
                    null
                }
            }
        }
        deferredCopiedFolders.awaitAll().filterNotNull()
    }


    override suspend fun unArchiveMultipleItems(
        linkIds: List<Long>, folderIds: List<Long>, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = getSystemEpochSeconds()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = !viaSocket,
            remoteOperation = {
                val remoteLinkIds = linksDao.getRemoteIds(linkIds)
                val remoteFolderIds = foldersDao.getRemoteIds(folderIds)
                require(remoteLinkIds != null && remoteFolderIds != null)

                remoteMultiActionRepo.markItemsAsRegular(
                    MarkItemsRegularDTO(
                        foldersIds = remoteFolderIds,
                        linkIds = remoteLinkIds,
                        eventTimestamp = eventTimestamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                foldersDao.updateFoldersTimestamp(
                    timestamp = it.eventTimestamp, localFolderIDs = folderIds
                )
                linksDao.updateLinksTimestamp(
                    timestamp = it.eventTimestamp, localLinkIds = linkIds
                )
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.MultiAction.UNARCHIVE_MULTIPLE_ITEMS.name,
                        payload = Json.encodeToString(
                            MarkItemsRegularDTO(
                                foldersIds = folderIds,
                                linkIds = linkIds,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            foldersDao.markMultipleFoldersAsRegular(
                eventTimestamp = eventTimestamp, folderIDs = folderIds
            )
            linksDao.unarchiveLinks(linksIds = linkIds, eventTimestamp = eventTimestamp)
        }
    }
}