package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.utils.updateLastSyncedWithServerTimeStamp
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
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalMultiActionRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteMultiActionRepo
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class LocalMultiActionRepoImpl(
    private val linksDao: LinksDao,
    private val foldersDao: FoldersDao,
    private val preferencesRepository: PreferencesRepository,
    private val remoteMultiActionRepo: RemoteMultiActionRepo,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val localFoldersRepo: LocalFoldersRepo
) : LocalMultiActionRepo {
    override suspend fun archiveMultipleItems(
        linkIds: List<Long>, folderIds: List<Long>, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                remoteMultiActionRepo.archiveMultipleItems(ArchiveMultipleItemsDTO(linkIds = linkIds.map {
                    linksDao.getRemoteIdOfLocalLink(it) ?: -45454
                }, folderIds = folderIds.map {
                    foldersDao.getRemoteIdOfAFolder(it) ?: -45454
                }.run {
                    linkoraLog(this)
                    this
                }, eventTimestamp = eventTimestamp))
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
        val eventTimestamp = Instant.now().epochSecond
        val remoteLinkIds = linkIds.map {
            linksDao.getRemoteIdOfLocalLink(it) ?: -45454
        }
        val remoteFolderIds = folderIds.map {
            foldersDao.getRemoteIdOfAFolder(it) ?: -45454
        }
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
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
        val eventTimeStamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                remoteMultiActionRepo.moveMultipleItems(
                    MoveItemsDTO(
                        folderIds = folderIds.map {
                            foldersDao.getRemoteIdOfAFolder(it) ?: -45454
                        },
                        linkIds = linkIds.map {
                            linksDao.getRemoteIdOfLocalLink(it) ?: -45454
                        },
                        linkType = linkType,
                        newParentFolderId = foldersDao.getRemoteIdOfAFolder(newParentFolderId)
                            ?: -45454,
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
        links: List<Link>,
        folders: List<Folder>,
        linkType: LinkType,
        newParentFolderId: Long,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimeStamp = Instant.now().epochSecond

        val copiedLinksIds = mutableMapOf<Long, Long>()

        lateinit var copiedFolders: List<CopyFolderDTO>

        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                remoteMultiActionRepo.copyMultipleItems(
                    CopyItemsDTO(
                        folders = copiedFolders, linkIds = copiedLinksIds.toMap(),
                        linkType = linkType,
                        newParentFolderId = foldersDao.getRemoteIdOfAFolder(newParentFolderId)
                            ?: -45454,
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
                        payload = Json.encodeToString(CopyItemsDTO(
                            folders = copiedFolders, linkIds = copiedLinksIds.toMap(),
                            linkType = linkType,
                            newParentFolderId = foldersDao.getRemoteIdOfAFolder(newParentFolderId)
                                ?: -45454,
                            eventTimestamp = eventTimeStamp
                        ))
                    )
                )
            }) {
            coroutineScope {
                awaitAll(async {
                    links.map {
                        it.copy(
                            idOfLinkedFolder = newParentFolderId,
                            linkType = linkType,
                            localId = 0, remoteId = null, lastModified = eventTimeStamp
                        )
                    }.let {
                        linksDao.addMultipleLinks(it).forEachIndexed { index, id ->
                            copiedLinksIds.put(id, links[index].remoteId ?: -45454)
                        }
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
    ): List<CopyFolderDTO> {
        val copiedFolders = mutableListOf<CopyFolderDTO>()
        folders.forEach { folder ->
            foldersDao.insertANewFolder(
                folder.copy(
                    parentFolderId = parentFolderId,
                    remoteId = null,
                    localId = 0,
                    lastModified = eventTimestamp
                )
            ).let { newFolderId ->
                val linksOfCurrentFolder = linksDao.getLinksOfThisFolderAsList(
                    folder.localId
                )
                linksDao.addMultipleLinks(
                    linksOfCurrentFolder.map {
                        it.copy(
                            idOfLinkedFolder = newFolderId,
                            localId = 0,
                            lastModified = eventTimestamp
                        )
                    }).let { linksIdsOfCopiedFolder ->
                    copiedFolders.add(
                        CopyFolderDTO(
                            currentFolder = CurrentFolder(
                                localId = newFolderId, remoteId = folder.remoteId ?: -45454
                            ),
                            links = linksIdsOfCopiedFolder.zip(linksOfCurrentFolder) { id, link ->
                                FolderLink(
                                    localId = id, remoteId = link.remoteId ?: -45454
                                )
                            },
                            childFolders = copyFolders(
                                parentFolderId = newFolderId,
                                folders = foldersDao.getChildFoldersOfThisParentIDAsAList(folder.localId),
                                eventTimestamp
                            )
                        )
                    )
                }
            }
        }
        return copiedFolders
    }


    override suspend fun unArchiveMultipleItems(
        localLinkIds: List<Long>, localFolderIds: List<Long>, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                remoteMultiActionRepo.markItemsAsRegular(MarkItemsRegularDTO(foldersIds = localFolderIds.map {
                    foldersDao.getRemoteIdOfAFolder(it) ?: -45454
                }, linkIds = localLinkIds.map {
                    linksDao.getRemoteIdOfLocalLink(it) ?: -45454
                }, eventTimestamp = eventTimestamp))
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                foldersDao.updateFoldersTimestamp(
                    timestamp = it.eventTimestamp, localFolderIDs = localFolderIds
                )
                linksDao.updateLinksTimestamp(
                    timestamp = it.eventTimestamp, localLinkIds = localLinkIds
                )
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.MultiAction.UNARCHIVE_MULTIPLE_ITEMS.name,
                        payload = Json.encodeToString(
                            MarkItemsRegularDTO(
                                foldersIds = localFolderIds,
                                linkIds = localLinkIds,
                                eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            foldersDao.markMultipleFoldersAsRegular(
                eventTimestamp = eventTimestamp,
                folderIDs = localFolderIds
            )
            linksDao.unarchiveLinks(linksIds = localLinkIds, eventTimestamp = eventTimestamp)
        }
    }
}