package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.common.utils.updateLastSyncedWithServerTimeStamp
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.domain.repository.remote.RemoteMultiActionRepo
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.repository.local.LocalMultiActionRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

class LocalMultiActionRepoImpl(
    private val linksDao: LinksDao,
    private val foldersDao: FoldersDao,
    private val preferencesRepository: PreferencesRepository,
    private val remoteMultiActionRepo: RemoteMultiActionRepo,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo
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
}