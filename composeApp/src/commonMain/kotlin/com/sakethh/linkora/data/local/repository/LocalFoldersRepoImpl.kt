package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.asAddFolderDTO
import com.sakethh.linkora.domain.asFolderDTO
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.folder.MarkSelectedFoldersAsRootDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.linkoraPlaceHolders
import com.sakethh.linkora.domain.mapToResultFlow
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteFoldersRepo
import com.sakethh.linkora.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.utils.isNotNull
import com.sakethh.linkora.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.utils.updateLastSyncedWithServerTimeStamp
import com.sakethh.linkora.utils.wrappedResultFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.serialization.json.Json
import java.time.Instant

class LocalFoldersRepoImpl(
    private val foldersDao: FoldersDao,
    private val remoteFoldersRepo: RemoteFoldersRepo,
    private val localLinksRepo: LocalLinksRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val preferencesRepository: PreferencesRepository,
) : LocalFoldersRepo {

    override suspend fun insertANewFolder(
        folder: Folder, ignoreFolderAlreadyExistsException: Boolean, viaSocket: Boolean
    ): Flow<Result<Long>> {
        var newLocalId: Long? = null
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (newLocalId == null) return@performLocalOperationWithRemoteSyncFlow emptyFlow()

                if (folder.parentFolderId != null) {
                    val remoteParentFolderId = getRemoteIdOfAFolder(folder.parentFolderId)
                    remoteFoldersRepo.createFolder(
                        folder.asAddFolderDTO().copy(parentFolderId = remoteParentFolderId)
                    )
                } else {
                    remoteFoldersRepo.createFolder(folder.asAddFolderDTO())
                }
            },
            remoteOperationOnSuccess = {
                if (newLocalId == null) return@performLocalOperationWithRemoteSyncFlow

                foldersDao.updateFolder(
                    foldersDao.getThisFolderData(newLocalId).copy(remoteId = it.id)
                )
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.timeStampBasedResponse.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (newLocalId == null) return@performLocalOperationWithRemoteSyncFlow

                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.CREATE_FOLDER.name,
                        payload = Json.encodeToString(
                            folder.asAddFolderDTO().copy(
                                offlineSyncItemId = newLocalId!!
                            )
                        )
                    )
                )
            },
            localOperation = {
                if (folder.name.isEmpty() || linkoraPlaceHolders().contains(folder.name)) {
                    throw Folder.InvalidName(if (folder.name.isEmpty()) "Folder name cannot be blank." else "\"${folder.name}\" is reserved.")
                }
                if (!ignoreFolderAlreadyExistsException) {
                    when (folder.parentFolderId) {
                        null -> {
                            doesThisRootFolderExists(folder.name).first().onSuccess {
                                if (it.data) {
                                    throw Folder.FolderAlreadyExists("Folder named \"${folder.name}\" already exists")
                                }
                            }
                        }

                        else -> {
                            doesThisChildFolderExists(folder.name, folder.parentFolderId).first()
                                .onSuccess {
                                    if (it.data == 1) {
                                        getThisFolderData(folder.parentFolderId).first()
                                            .onSuccess { parentFolder ->
                                                throw Folder.FolderAlreadyExists("A folder named \"${folder.name}\" already exists in ${parentFolder.data.name}.")
                                            }
                                    }
                                }
                        }
                    }
                }
                newLocalId = foldersDao.insertANewFolder(folder.copy(localId = 0))
                newLocalId
            })
    }


    override suspend fun duplicateAFolder(
        actualFolderId: Long, parentFolderID: Long?
    ): Flow<Result<Long>> {
        return performLocalOperationWithRemoteSyncFlow<Long, Long>(performRemoteOperation = false) {
            foldersDao.duplicateAFolder(actualFolderId, parentFolderID)
        }
    }

    override suspend fun insertMultipleNewFolders(foldersTable: List<Folder>): Flow<Result<Unit>> {
        return performLocalOperationWithRemoteSyncFlow<Unit, Unit>(
            performRemoteOperation = false,
            localOperation = {
                foldersDao.insertMultipleNewFolders(foldersTable)
            },
        )
    }

    override suspend fun getAllArchiveFoldersAsList(): Flow<Result<List<Folder>>> {
        return performLocalOperationWithRemoteSyncFlow<List<Folder>, Unit>(
            performRemoteOperation = false,
            localOperation = {
                foldersDao.getAllArchiveFoldersAsList()
            },
        )
    }

    override suspend fun getAllRootFoldersAsList(): List<Folder> {
        return foldersDao.getAllRootFoldersAsList()
    }

    override fun getAllFoldersAsResultList(): Flow<Result<List<Folder>>> {
        return performLocalOperationWithRemoteSyncFlow<List<Folder>, Unit>(performRemoteOperation = false) {
            foldersDao.getAllFoldersAsList()
        }
    }

    override fun getAllFoldersAsFlow(): Flow<List<Folder>> {
        return foldersDao.getAllFoldersAsFlow()
    }

    override suspend fun getSizeOfLinksOfThisFolder(folderID: Long): Flow<Result<Int>> {
        return performLocalOperationWithRemoteSyncFlow<Int, Unit>(performRemoteOperation = false) {
            foldersDao.getSizeOfLinksOfThisFolder(folderID)
        }
    }

    override suspend fun getAllFoldersAsList(): List<Folder> {
        return foldersDao.getAllFoldersAsList()
    }

    override suspend fun getChildFoldersOfThisParentIDAsList(parentFolderID: Long?): List<Folder> {
        return foldersDao.getChildFoldersOfThisParentIDAsAList(parentFolderID)
    }

    override suspend fun getLatestFoldersTableID(): Long {
        return foldersDao.getLatestFoldersTableID()
    }

    override suspend fun getThisFolderData(folderID: Long): Flow<Result<Folder>> {
        return performLocalOperationWithRemoteSyncFlow<Folder, Unit>(performRemoteOperation = false) {
            foldersDao.getThisFolderData(folderID)
        }
    }

    override suspend fun getLastIDOfFoldersTable(): Flow<Result<Long>> {
        return performLocalOperationWithRemoteSyncFlow<Long, Unit>(performRemoteOperation = false) {
            foldersDao.getLastIDOfFoldersTable()
        }
    }

    override suspend fun doesThisChildFolderExists(
        folderName: String, parentFolderID: Long?
    ): Flow<Result<Int>> {
        return performLocalOperationWithRemoteSyncFlow<Int, Unit>(performRemoteOperation = false) {
            foldersDao.doesThisChildFolderExists(
                folderName, parentFolderID
            )
        }
    }

    override suspend fun doesThisRootFolderExists(folderName: String): Flow<Result<Boolean>> {
        return performLocalOperationWithRemoteSyncFlow<Boolean, Unit>(performRemoteOperation = false) {
            foldersDao.doesThisRootFolderExists(folderName)
        }
    }

    override suspend fun isThisFolderMarkedAsArchive(folderID: Long): Flow<Result<Boolean>> {
        return performLocalOperationWithRemoteSyncFlow<Boolean, Unit>(performRemoteOperation = false) {
            foldersDao.isThisFolderMarkedAsArchive(folderID)
        }
    }

    override suspend fun getNewestFolder(): Flow<Result<Folder>> {
        return performLocalOperationWithRemoteSyncFlow<Folder, Unit>(performRemoteOperation = false) {
            foldersDao.getNewestFolder()
        }
    }

    override fun getFoldersCount(): Flow<Result<Int>> {
        return foldersDao.getFoldersCount().map {
            Result.Success(it)
        }.onStart {
            Result.Loading<Int>()
        }.catchAsThrowableAndEmitFailure()
    }

    override suspend fun changeTheParentIdOfASpecificFolder(
        sourceFolderId: Long, targetParentId: Long?
    ): Flow<Result<Unit>> {
        return performLocalOperationWithRemoteSyncFlow<Unit, Unit>(performRemoteOperation = false) {
            foldersDao.changeTheParentIdOfASpecificFolder(
                sourceFolderId, targetParentId
            )
        }
    }

    override suspend fun getRootFolders(sortOption: String): Flow<Result<List<Folder>>> {
        return foldersDao.getRootFolders(sortOption).mapToResultFlow()
    }

    override suspend fun getChildFolders(
        parentFolderId: Long, sortOption: String
    ): Flow<Result<List<Folder>>> {
        return foldersDao.getChildFolders(parentFolderId, sortOption).mapToResultFlow()
    }

    override fun sortFoldersAsNonResultFlow(
        parentFolderId: Long, sortOption: String
    ): Flow<List<Folder>> {
        return foldersDao.getChildFolders(parentFolderId, sortOption)
    }

    override suspend fun getChildFoldersOfThisParentIDAsFlow(parentFolderID: Long?): Flow<Result<List<Folder>>> {
        return foldersDao.getChildFoldersOfThisParentIDAsFlow(parentFolderID).mapToResultFlow()
    }

    override suspend fun getSizeOfChildFoldersOfThisParentID(parentFolderID: Long?): Flow<Result<Int>> {
        return performLocalOperationWithRemoteSyncFlow<Int, Unit>(performRemoteOperation = false) {
            foldersDao.getSizeOfChildFoldersOfThisParentID(parentFolderID)
        }
    }

    override suspend fun renameAFolderName(
        folderID: Long,
        existingFolderName: String,
        newFolderName: String,
        ignoreFolderAlreadyExistsException: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = true,
            remoteOperation = {
                val remoteId = getRemoteIdOfAFolder(folderID)
                if (remoteId.isNotNull()) {
                    remoteFoldersRepo.updateFolderName(
                        UpdateFolderNameDTO(
                            remoteId!!, newFolderName, eventTimestamp
                        )
                    )
                    // folder name in panel_folders gets updated on server-side when the actual folder name changes, so no need to push externally
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                foldersDao.updateFolderTimestamp(it.eventTimestamp, folderID)
            },
            localOperation = {
                if (newFolderName.isEmpty() || linkoraPlaceHolders().contains(newFolderName) || existingFolderName == newFolderName) {
                    throw Folder.InvalidName(if (newFolderName.isEmpty()) "Folder name cannot be blank." else if (existingFolderName == newFolderName) "Nothing has changed to update." else "\"${newFolderName}\" is reserved.")
                }
                foldersDao.renameAFolderName(folderID, newFolderName)
                foldersDao.updateFolderTimestamp(eventTimestamp, folderID)

                localPanelsRepo.updateAFolderName(folderID, newFolderName)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.UPDATE_FOLDER_NAME.name,
                        payload = Json.encodeToString(
                            UpdateFolderNameDTO(
                                folderID, newFolderName, eventTimestamp
                            )
                        )
                    )
                )
            })
    }

    override suspend fun getRemoteIdOfAFolder(localId: Long): Long? {
        return foldersDao.getRemoteIdOfAFolder(localId)
    }

    override suspend fun getLocalIdOfAFolder(remoteId: Long): Long? {
        return foldersDao.getLocalIdOfAFolder(remoteId)
    }

    override suspend fun markFolderAsArchive(
        folderID: Long, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                val remoteId = getRemoteIdOfAFolder(folderID)
                if (remoteId.isNotNull()) {
                    remoteFoldersRepo.markAsArchive(IDBasedDTO(remoteId!!, eventTimestamp))
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                foldersDao.updateFolderTimestamp(it.eventTimestamp, folderID)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name,
                        payload = Json.encodeToString(
                            IDBasedDTO(
                                folderID, eventTimestamp
                            )
                        )
                    )
                )
            }) {
            foldersDao.markFolderAsArchive(folderID)
            foldersDao.updateFolderTimestamp(eventTimestamp, folderID)
        }
    }

    override suspend fun markFolderAsRegularFolder(
        folderID: Long, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                val remoteFolderId = getRemoteIdOfAFolder(folderID)
                if (remoteFolderId.isNotNull()) {
                    remoteFoldersRepo.markAsRegularFolder(
                        IDBasedDTO(
                            remoteFolderId!!, eventTimestamp

                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                foldersDao.updateFolderTimestamp(it.eventTimestamp, folderID)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name,
                        payload = Json.encodeToString(
                            value = IDBasedDTO(
                                folderID, eventTimestamp
                            )
                        )
                    )
                )
            }) {
            foldersDao.markFolderAsRegularFolder(folderID)
            foldersDao.updateFolderTimestamp(eventTimestamp, folderID)
        }
    }

    override suspend fun renameAFolderNote(folderID: Long, newNote: String): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = true,
            remoteOperation = {
                val remoteID = getRemoteIdOfAFolder(folderID)
                if (remoteID.isNotNull()) {
                    remoteFoldersRepo.updateFolderNote(
                        UpdateFolderNoteDTO(
                            remoteID!!, newNote, eventTimestamp
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                foldersDao.updateFolderTimestamp(it.eventTimestamp, folderID)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name,
                        payload = Json.encodeToString(
                            UpdateFolderNoteDTO(
                                folderID, newNote, eventTimestamp
                            )
                        )
                    )
                )
            }) {
            foldersDao.renameAFolderNote(folderID, newNote)
            foldersDao.updateFolderTimestamp(eventTimestamp, folderID)
        }
    }

    override suspend fun updateLocalFolderData(folder: Folder): Flow<Result<Unit>> {
        return performLocalOperationWithRemoteSyncFlow<Unit, Unit>(performRemoteOperation = false) {
            foldersDao.updateFolder(folder.copy(lastModified = Instant.now().epochSecond))
        }
    }

    override suspend fun updateFolder(folder: Folder, viaSocket: Boolean): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        val remoteFolderDTO = if (folder.remoteId != null) {
            folder.asFolderDTO(
                remoteId = folder.remoteId,
                remoteParentFolderId = if (folder.parentFolderId == null) null else foldersDao.getRemoteIdOfAFolder(
                    folder.parentFolderId
                )
            )
        } else {
            null
        }?.copy(eventTimestamp = eventTimestamp)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = !viaSocket,
            remoteOperation = {
                if (remoteFolderDTO != null) {
                    remoteFoldersRepo.updateFolder(remoteFolderDTO)
                } else {
                    return@performLocalOperationWithRemoteSyncFlow emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (remoteFolderDTO == null) return@performLocalOperationWithRemoteSyncFlow

                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.UPDATE_FOLDER.name,
                        payload = Json.encodeToString(
                            value = remoteFolderDTO
                        )
                    )
                )
            },
            localOperation = {
                foldersDao.updateFolder(folder.copy(lastModified = eventTimestamp))
            })
    }

    override suspend fun deleteAFolderNote(
        folderID: Long, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = !viaSocket,
            remoteOperation = {
                val remoteId = getRemoteIdOfAFolder(folderID)
                if (remoteId.isNotNull()) {
                    remoteFoldersRepo.deleteFolderNote(IDBasedDTO(remoteId!!, eventTimestamp))
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.DELETE_FOLDER_NOTE.name,
                        payload = Json.encodeToString(
                            value = IDBasedDTO(
                                folderID, eventTimestamp
                            )
                        )
                    )
                )
            }) {
            foldersDao.deleteAFolderNote(folderID)
        }
    }

    override suspend fun deleteAFolder(
        folderID: Long, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        // we need to hold the id because the local folder gets deleted first, so if we try to search after that, there will be nothing to search
        val remoteFolderId = getRemoteIdOfAFolder(folderID)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteFolderId.isNotNull()) {
                    remoteFoldersRepo.deleteFolder(IDBasedDTO(remoteFolderId!!, eventTimestamp))
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (remoteFolderId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Folder.DELETE_FOLDER.name,
                            payload = Json.encodeToString(
                                value = IDBasedDTO(
                                    remoteFolderId, eventTimestamp
                                )
                            )
                        )
                    )
                }
            },
            localOperation = {
                deleteLocalDataRelatedToTheFolder(folderID)
                localLinksRepo.deleteLinksOfFolder(folderID).collect()
                foldersDao.deleteAFolder(folderID)
            })
    }

    override suspend fun deleteMultipleFolders(
        folderIDs: List<Long>, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        return wrappedResultFlow {
            folderIDs.forEach {
                deleteAFolder(it, viaSocket = true).collect()
            }
        }
    }

    private suspend fun deleteLocalDataRelatedToTheFolder(folderID: Long) {
        localPanelsRepo.deleteAFolderFromAllPanels(folderID)
        foldersDao.getChildFoldersOfThisParentIDAsAList(folderID).forEach {
            localPanelsRepo.deleteAFolderFromAllPanels(it.localId)
            foldersDao.deleteAFolder(it.localId)
            localLinksRepo.deleteLinksOfFolder(it.localId).collect()
            deleteLocalDataRelatedToTheFolder(it.localId)
        }
    }

    override suspend fun deleteChildFoldersOfThisParentID(parentFolderId: Long): Flow<Result<Unit>> {
        return performLocalOperationWithRemoteSyncFlow<Unit, Unit>(performRemoteOperation = false) {
            foldersDao.deleteChildFoldersOfThisParentID(parentFolderId)
        }
    }

    override suspend fun isFoldersTableEmpty(): Flow<Result<Boolean>> {
        return performLocalOperationWithRemoteSyncFlow<Boolean, Unit>(performRemoteOperation = false) {
            foldersDao.isFoldersTableEmpty()
        }
    }

    override fun search(query: String, sortOption: String): Flow<Result<List<Folder>>> {
        return foldersDao.search(query, sortOption).mapToResultFlow()
    }

    override suspend fun deleteAllFolders() {
        foldersDao.deleteAllFolders()
    }

    override suspend fun getUnSyncedFolders(): List<Folder> {
        return foldersDao.getUnSyncedFolders()
    }

    override suspend fun markFoldersAsRoot(
        folderIDs: List<Long>, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                remoteFoldersRepo.markSelectedFoldersAsRoot(
                    MarkSelectedFoldersAsRootDTO(
                        folderIds = folderIDs.map {
                            getRemoteIdOfAFolder(it) ?: -45454
                        }, eventTimestamp = eventTimestamp
                    )
                )
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Folder.MARK_FOLDERS_AS_ROOT.name,
                        payload = Json.encodeToString(
                            MarkSelectedFoldersAsRootDTO(
                                folderIds = folderIDs, eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            foldersDao.markFoldersAsRoot(folderIDs)
        }
    }
}