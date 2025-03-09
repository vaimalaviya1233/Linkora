package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.common.utils.updateLastSyncedWithServerTimeStamp
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.domain.DeleteMultipleItemsDTO
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.CopyItemsDTO
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
                    linksDao.archiveMultipleLinks(linkIds)
                }, async {
                    foldersDao.markMultipleFoldersAsArchive(folderIds)
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
                    foldersDao.moveFolders(newParentFolderId, folderIds)
                }, async {
                    linksDao.moveLinks(newParentFolderId, linkType, linkIds)
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
        val remoteLinksIds = links.map { it.remoteId }
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                val copiedLinksIds = linksDao.getIdsOfCopiedLinks(
                    eventTimestamp = eventTimeStamp,
                    parentFolderId = newParentFolderId,
                    linkType = linkType
                )
                remoteMultiActionRepo.copyMultipleItems(
                    CopyItemsDTO(
                        folderIds = folders.associate {
                            it.localId to (it.remoteId ?: -45454)
                        }, linkIds = copiedLinksIds.mapIndexed { index, id ->
                            id to (remoteLinksIds[index] ?: -45454)
                        }.toMap(),
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
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                // TODO
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
                        linksDao.addMultipleLinks(it)
                    }
                }, async {
                    copyFolders(parentFolderId = newParentFolderId, folders = folders)
                })
            }
        }
    }

    private suspend fun copyFolders(
        parentFolderId: Long, folders: List<Folder>
    ) {
        folders.forEach { folder ->
            foldersDao.insertANewFolder(
                folder.copy(
                    parentFolderId = parentFolderId, remoteId = null, localId = 0
                )
            ).let { newFolderId ->
                linksDao.addMultipleLinks(
                    linksDao.getLinksOfThisFolderAsList(
                        folder.localId
                    ).map {
                        it.copy(idOfLinkedFolder = newFolderId, remoteId = null, localId = 0)
                    })
                copyFolders(
                    parentFolderId = newFolderId,
                    folders = foldersDao.getChildFoldersOfThisParentIDAsAList(folder.localId)
                )
            }
        }
    }
}