package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.core.utils.Constants
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.FoldersRepo
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import javax.naming.InvalidNameException

class LocalFoldersRepoImpl(private val foldersDao: FoldersDao) : FoldersRepo {
    override suspend fun insertANewFolder(
        folder: Folder, ignoreFolderAlreadyExistsException: Boolean
    ): Flow<Result<Long>> {
        return flow {
            try {
                emit(Result.Loading())
                if (folder.name.isEmpty() || Constants.placeholders().contains(folder.name)) {
                    throw InvalidNameException(if (folder.name.isEmpty()) "Folder name cannot be blank." else "\"${folder.name}\" is preserved.")
                }
                if (!ignoreFolderAlreadyExistsException) {
                    when (folder.parentFolderId) {
                        null -> {
                            doesThisRootFolderExists(folder.name).first()
                                .onSuccess { folderExists ->
                                    if (folderExists) {
                                        throw Folder.FolderAlreadyExistsException("Folder named \"${folder.name}\" already exists")
                                    }
                                }
                        }

                        else -> {
                            doesThisChildFolderExists(folder.name, folder.parentFolderId).first()
                                .onSuccess { folderExists ->
                                    if (folderExists == 1) {
                                        getThisFolderData(folder.parentFolderId).first()
                                            .onSuccess { parentFolder ->
                                                throw Folder.FolderAlreadyExistsException("A folder named \"${folder.name}\" already exists in ${parentFolder.name}.")
                                            }
                                    }
                                }
                        }
                    }
                }
                val newId = foldersDao.insertANewFolder(folder)
                emit(Result.Success(newId))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun duplicateAFolder(
        actualFolderId: Long, parentFolderID: Long?
    ): Flow<Result<Long>> {
        return flow {
            try {
                emit(Result.Loading())
                val newId = foldersDao.duplicateAFolder(actualFolderId, parentFolderID)
                emit(Result.Success(newId))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun insertMultipleNewFolders(foldersTable: List<Folder>): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                foldersDao.insertMultipleNewFolders(foldersTable)
                emit(Result.Success(Unit))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override fun getAllArchiveFoldersAsFlow(): Flow<Result<Flow<List<Folder>>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getAllArchiveFoldersAsFlow()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getAllArchiveFoldersAsList(): Flow<Result<List<Folder>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getAllArchiveFoldersAsList()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override fun getAllRootFoldersAsFlow(): Flow<Result<Flow<List<Folder>>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getAllRootFoldersAsFlow()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getAllRootFoldersAsList(): Flow<Result<List<Folder>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getAllRootFoldersAsList()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getAllFolders(): Flow<Result<List<Folder>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getAllFolders()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getSizeOfLinksOfThisFolder(folderID: Long): Flow<Result<Int>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getSizeOfLinksOfThisFolder(folderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getThisFolderData(folderID: Long): Flow<Result<Folder>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getThisFolderData(folderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getLastIDOfFoldersTable(): Flow<Result<Long>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getLastIDOfFoldersTable()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun doesThisChildFolderExists(
        folderName: String, parentFolderID: Long?
    ): Flow<Result<Int>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(
                    Result.Success(
                        foldersDao.doesThisChildFolderExists(
                            folderName, parentFolderID
                        )
                    )
                )
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun doesThisRootFolderExists(folderName: String): Flow<Result<Boolean>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.doesThisRootFolderExists(folderName)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun isThisFolderMarkedAsArchive(folderID: Long): Flow<Result<Boolean>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.isThisFolderMarkedAsArchive(folderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getNewestFolder(): Flow<Result<Folder>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getNewestFolder()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override fun getFoldersCount(): Flow<Result<Flow<Int>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getFoldersCount()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun changeTheParentIdOfASpecificFolder(
        sourceFolderId: List<Long>, targetParentId: Long?
    ): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(
                    Result.Success(
                        foldersDao.changeTheParentIdOfASpecificFolder(
                            sourceFolderId, targetParentId
                        )
                    )
                )
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override fun getChildFoldersOfThisParentIDAsFlow(parentFolderID: Long?): Flow<Result<Flow<List<Folder>>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getChildFoldersOfThisParentIDAsFlow(parentFolderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getChildFoldersOfThisParentIDAsAList(parentFolderID: Long?): Flow<Result<List<Folder>>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getChildFoldersOfThisParentIDAsAList(parentFolderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun getSizeOfChildFoldersOfThisParentID(parentFolderID: Long?): Flow<Result<Int>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.getSizeOfChildFoldersOfThisParentID(parentFolderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun renameAFolderName(
        folderID: Long,
        existingFolderName: String,
        newFolderName: String,
        ignoreFolderAlreadyExistsException: Boolean
    ): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                if (newFolderName.isEmpty() || Constants.placeholders()
                        .contains(newFolderName) || existingFolderName == newFolderName
                ) {
                    throw InvalidNameException(if (newFolderName.isEmpty()) "Folder name cannot be blank." else if (existingFolderName == newFolderName) "Nothing has changed to update." else "\"${newFolderName}\" is preserved.")
                }
                emit(Result.Success(foldersDao.renameAFolderName(folderID, newFolderName)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun markFolderAsArchive(folderID: Long): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.markFolderAsArchive(folderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun markMultipleFoldersAsArchive(folderIDs: Array<Long>): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.markMultipleFoldersAsArchive(folderIDs)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun markFolderAsRegularFolder(folderID: Long): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.markFolderAsRegularFolder(folderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun renameAFolderNote(folderID: Long, newNote: String): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.renameAFolderNote(folderID, newNote)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun updateAFolderData(folder: Folder): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.updateAFolderData(folder)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun deleteAFolderNote(folderID: Long): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.deleteAFolderNote(folderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun deleteAFolder(folderID: Long): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.deleteAFolder(folderID)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun deleteChildFoldersOfThisParentID(parentFolderId: Long): Flow<Result<Unit>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.deleteChildFoldersOfThisParentID(parentFolderId)))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

    override suspend fun isFoldersTableEmpty(): Flow<Result<Boolean>> {
        return flow {
            try {
                emit(Result.Loading())
                emit(Result.Success(foldersDao.isFoldersTableEmpty()))
            } catch (e: Exception) {
                emit(Result.Failure(message = e.message.toString()))
            }
        }
    }

}