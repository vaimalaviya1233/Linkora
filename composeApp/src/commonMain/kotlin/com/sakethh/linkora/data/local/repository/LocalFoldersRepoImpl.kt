package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.domain.Message
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.linkoraPlaceHolders
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.remote.RemoteFoldersRepo
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

class LocalFoldersRepoImpl(
    private val foldersDao: FoldersDao,
    private val remoteFoldersRepo: RemoteFoldersRepo,
    private val canPushToServer: () -> Boolean,
    private val localLinksRepo: LocalLinksRepo
) : LocalFoldersRepo {

    private fun <LocalType, RemoteType> executeWithResultFlow(
        performRemoteOperation: Boolean,
        remoteOperation: suspend () -> Flow<Result<RemoteType>> = { emptyFlow() },
        localOperation: suspend () -> LocalType
    ): Flow<Result<LocalType>> {
        return flow {
            emit(Result.Loading())
            val localResult = localOperation()
            Result.Success(localResult).let { success ->
                if (performRemoteOperation && canPushToServer()) {
                    remoteOperation().collect { remoteResult ->
                        remoteResult.onFailure { failureMessage ->
                            success.isRemoteExecutionSuccessful = false
                            success.remoteFailureMessage = failureMessage
                        }
                    }
                }
                emit(success)
            }
        }.catchAsThrowableAndEmitFailure()
    }

    override suspend fun insertANewFolder(
        folder: Folder, ignoreFolderAlreadyExistsException: Boolean
    ): Flow<Result<Message>> {
        return executeWithResultFlow(performRemoteOperation = true, remoteOperation = {
            remoteFoldersRepo.createFolder(folder)
        }, localOperation = {
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
            val newId = foldersDao.insertANewFolder(folder)
            "Folder created successfully with id = $newId"
        })
    }


    override suspend fun duplicateAFolder(
        actualFolderId: Long, parentFolderID: Long?
    ): Flow<Result<Long>> {
        return executeWithResultFlow<Long, Long>(performRemoteOperation = false) {
            foldersDao.duplicateAFolder(actualFolderId, parentFolderID)
        }
    }

    override suspend fun insertMultipleNewFolders(foldersTable: List<Folder>): Flow<Result<Unit>> {
        return executeWithResultFlow<Unit, Unit>(
            performRemoteOperation = false,
            localOperation = {
                foldersDao.insertMultipleNewFolders(foldersTable)
            },
        )
    }

    override fun getAllArchiveFoldersAsFlow(): Flow<Result<List<Folder>>> {
        return foldersDao.getAllArchiveFoldersAsFlow().map {
            Result.Success(it)
        }.onStart {
            Result.Loading<List<Folder>>()
        }.catchAsThrowableAndEmitFailure()
    }

    override suspend fun getAllArchiveFoldersAsList(): Flow<Result<List<Folder>>> {
        return executeWithResultFlow<List<Folder>, Unit>(
            performRemoteOperation = false,
            localOperation = {
            foldersDao.getAllArchiveFoldersAsList()
            },
        )
    }

    override fun getAllRootFoldersAsFlow(): Flow<Result<List<Folder>>> {
        return foldersDao.getAllRootFoldersAsFlow().map { Result.Success(it) }
            .onStart { Result.Loading<List<Folder>>() }
            .catchAsThrowableAndEmitFailure()
    }

    override suspend fun getAllRootFoldersAsList(): Flow<Result<List<Folder>>> {
        return executeWithResultFlow<List<Folder>, Unit>(performRemoteOperation = false) {
            foldersDao.getAllRootFoldersAsList()
        }
    }

    override suspend fun getAllFolders(): Flow<Result<List<Folder>>> {
        return executeWithResultFlow<List<Folder>, Unit>(performRemoteOperation = false) {
            foldersDao.getAllFolders()
        }
    }

    override suspend fun getSizeOfLinksOfThisFolder(folderID: Long): Flow<Result<Int>> {
        return executeWithResultFlow<Int, Unit>(performRemoteOperation = false) {
            foldersDao.getSizeOfLinksOfThisFolder(folderID)
        }
    }

    override suspend fun getThisFolderData(folderID: Long): Flow<Result<Folder>> {
        return executeWithResultFlow<Folder, Unit>(performRemoteOperation = false) {
            foldersDao.getThisFolderData(folderID)
        }
    }

    override suspend fun getLastIDOfFoldersTable(): Flow<Result<Long>> {
        return executeWithResultFlow<Long, Unit>(performRemoteOperation = false) {
            foldersDao.getLastIDOfFoldersTable()
        }
    }

    override suspend fun doesThisChildFolderExists(
        folderName: String, parentFolderID: Long?
    ): Flow<Result<Int>> {
        return executeWithResultFlow<Int, Unit>(performRemoteOperation = false) {
            foldersDao.doesThisChildFolderExists(
                folderName, parentFolderID
            )
        }
    }

    override suspend fun doesThisRootFolderExists(folderName: String): Flow<Result<Boolean>> {
        return executeWithResultFlow<Boolean, Unit>(performRemoteOperation = false) {
            foldersDao.doesThisRootFolderExists(folderName)
        }
    }

    override suspend fun isThisFolderMarkedAsArchive(folderID: Long): Flow<Result<Boolean>> {
        return executeWithResultFlow<Boolean, Unit>(performRemoteOperation = false) {
            foldersDao.isThisFolderMarkedAsArchive(folderID)
        }
    }

    override suspend fun getNewestFolder(): Flow<Result<Folder>> {
        return executeWithResultFlow<Folder, Unit>(performRemoteOperation = false) {
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
        return executeWithResultFlow(performRemoteOperation = true, remoteOperation = {
            remoteFoldersRepo.changeParentFolder(sourceFolderId, targetParentId)
        }) {
            foldersDao.changeTheParentIdOfASpecificFolder(
                sourceFolderId, targetParentId
            )
        }
    }

    override fun getChildFoldersOfThisParentIDAsFlow(parentFolderID: Long?): Flow<Result<List<Folder>>> {
        return foldersDao.getChildFoldersOfThisParentIDAsFlow(parentFolderID)
            .map { Result.Success(it) }
            .onStart {
                Result.Loading<List<Folder>>()
            }.catchAsThrowableAndEmitFailure()
    }

    override suspend fun getChildFoldersOfThisParentIDAsAList(parentFolderID: Long?): Flow<Result<List<Folder>>> {
        return executeWithResultFlow<List<Folder>, Unit>(performRemoteOperation = false) {
            foldersDao.getChildFoldersOfThisParentIDAsAList(parentFolderID)
        }
    }

    override suspend fun getSizeOfChildFoldersOfThisParentID(parentFolderID: Long?): Flow<Result<Int>> {
        return executeWithResultFlow<Int, Unit>(performRemoteOperation = false) {
            foldersDao.getSizeOfChildFoldersOfThisParentID(parentFolderID)
        }
    }

    override suspend fun renameAFolderName(
        folderID: Long,
        existingFolderName: String,
        newFolderName: String,
        ignoreFolderAlreadyExistsException: Boolean
    ): Flow<Result<Unit>> {
        return executeWithResultFlow(performRemoteOperation = true, remoteOperation = {
            remoteFoldersRepo.updateFolderName(folderID, newFolderName)
        }, localOperation = {
            if (newFolderName.isEmpty() || linkoraPlaceHolders()
                    .contains(newFolderName) || existingFolderName == newFolderName
            ) {
                throw Folder.InvalidName(if (newFolderName.isEmpty()) "Folder name cannot be blank." else if (existingFolderName == newFolderName) "Nothing has changed to update." else "\"${newFolderName}\" is reserved.")
            }
            foldersDao.renameAFolderName(folderID, newFolderName)
        })
    }

    override suspend fun markFolderAsArchive(folderID: Long): Flow<Result<Unit>> {
        return executeWithResultFlow(performRemoteOperation = true, remoteOperation = {
            remoteFoldersRepo.markAsArchive(folderID)
        }) {
            foldersDao.markFolderAsArchive(folderID)
        }
    }

    override suspend fun markMultipleFoldersAsArchive(folderIDs: Array<Long>): Flow<Result<Unit>> {
        return executeWithResultFlow<Unit, Unit>(performRemoteOperation = false) {
            foldersDao.markMultipleFoldersAsArchive(folderIDs)
        }
    }

    override suspend fun markFolderAsRegularFolder(folderID: Long): Flow<Result<Unit>> {
        return executeWithResultFlow(performRemoteOperation = true, remoteOperation = {
            remoteFoldersRepo.markAsRegularFolder(folderID)
        }) {
            foldersDao.markFolderAsRegularFolder(folderID)
        }
    }

    override suspend fun renameAFolderNote(folderID: Long, newNote: String): Flow<Result<Unit>> {
        linkoraLog(folderID)
        return executeWithResultFlow(performRemoteOperation = true, remoteOperation = {
            remoteFoldersRepo.updateFolderNote(folderID, newNote)
        }) {
            foldersDao.renameAFolderNote(folderID, newNote)
        }
    }

    override suspend fun updateAFolderData(folder: Folder): Flow<Result<Unit>> {
        return executeWithResultFlow<Unit, Unit>(performRemoteOperation = false) {
            foldersDao.updateAFolderData(folder)
        }
    }

    override suspend fun deleteAFolderNote(folderID: Long): Flow<Result<Unit>> {
        return executeWithResultFlow(performRemoteOperation = true, remoteOperation = {
            remoteFoldersRepo.deleteFolderNote(folderID)
        }) {
            foldersDao.deleteAFolderNote(folderID)
        }
    }

    override suspend fun deleteAFolder(folderID: Long): Flow<Result<Unit>> {
        return executeWithResultFlow(performRemoteOperation = false, remoteOperation = {
            remoteFoldersRepo.deleteFolder(folderID)
        }, localOperation = {
            deleteLocalDataRelatedToTheFolder(folderID)
            localLinksRepo.deleteLinksOfFolder(folderID)
            foldersDao.deleteAFolder(folderID)
        })
    }

    private suspend fun deleteLocalDataRelatedToTheFolder(folderID: Long) {
        foldersDao.getChildFoldersOfThisParentIDAsAList(folderID).forEach {
            foldersDao.deleteAFolder(it.localId)
            localLinksRepo.deleteLinksOfFolder(it.localId)
            deleteLocalDataRelatedToTheFolder(it.localId)
        }
    }

    override suspend fun deleteChildFoldersOfThisParentID(parentFolderId: Long): Flow<Result<Unit>> {
        return executeWithResultFlow<Unit, Unit>(performRemoteOperation = false) {
            foldersDao.deleteChildFoldersOfThisParentID(parentFolderId)
        }
    }

    override suspend fun isFoldersTableEmpty(): Flow<Result<Boolean>> {
        return executeWithResultFlow<Boolean, Unit>(performRemoteOperation = false) {
            foldersDao.isFoldersTableEmpty()
        }
    }

}