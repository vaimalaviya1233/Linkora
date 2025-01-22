package com.sakethh.linkora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.network.Network.client
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.asWebSocketUrl
import com.sakethh.linkora.common.utils.isSameAsCurrentClientID
import com.sakethh.linkora.common.utils.pushSnackbar
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.folder.FolderDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNameDTO
import com.sakethh.linkora.domain.dto.server.folder.UpdateFolderNoteDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import io.ktor.client.plugins.websocket.webSocket
import io.ktor.websocket.Frame
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement

class AppVM(
    private val localFoldersRepo: LocalFoldersRepo
) : ViewModel() {
    init {
        readSocketEvents(wsBaseUrl = AppPreferences.serverBaseUrl.value.asWebSocketUrl())
    }

    private var socketEventJob: Job? = null

    fun shutdownSocketConnection() {
        socketEventJob?.cancel()
    }

    private fun readSocketEvents(wsBaseUrl: String) {
        if (AppPreferences.canPushToServer().not()) return

        socketEventJob = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            throwable.pushSnackbar(viewModelScope)
        }) {
            client.webSocket(wsBaseUrl + "events") {
                this.incoming.consumeAsFlow().collectLatest {
                    if (it is Frame.Text) {

                        val deserializedWebSocketEvent =
                            Json.decodeFromString<WebSocketEvent>((it.data).decodeToString())

                        when (deserializedWebSocketEvent.operation) {

                            RemoteRoute.Folder.CREATE_FOLDER.name -> {
                                val folderDto = Json.decodeFromJsonElement<FolderDTO>(
                                    deserializedWebSocketEvent.payload
                                )
                                if (folderDto.correlationId.isSameAsCurrentClientID()) {
                                    return@collectLatest
                                }
                                localFoldersRepo.insertANewFolder(
                                    folder = Folder(
                                        name = folderDto.name,
                                        note = folderDto.note,
                                        parentFolderId = folderDto.parentFolderId,
                                        remoteId = folderDto.id,
                                        isArchived = folderDto.isArchived
                                    ), ignoreFolderAlreadyExistsException = true, viaChannel = true
                                ).collectLatest {}
                            }

                            RemoteRoute.Folder.DELETE_FOLDER.name -> {
                                val idBasedDTO = Json.decodeFromJsonElement<IDBasedDTO>(
                                    deserializedWebSocketEvent.payload
                                )
                                if (idBasedDTO.correlationId.isSameAsCurrentClientID()) return@collectLatest

                                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                                if (folderId != null) {
                                    localFoldersRepo.deleteAFolder(folderId, viaChannel = true)
                                        .collectLatest {}
                                }
                            }

                            RemoteRoute.Folder.MARK_FOLDER_AS_ARCHIVE.name -> {
                                val idBasedDTO = Json.decodeFromJsonElement<IDBasedDTO>(
                                    deserializedWebSocketEvent.payload
                                )
                                if (idBasedDTO.correlationId.isSameAsCurrentClientID()) return@collectLatest

                                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                                if (folderId != null) {
                                    localFoldersRepo.markFolderAsArchive(
                                        folderId, viaChannel = true
                                    ).collectLatest {}
                                }
                            }

                            RemoteRoute.Folder.MARK_AS_REGULAR_FOLDER.name -> {
                                val idBasedDTO = Json.decodeFromJsonElement<IDBasedDTO>(
                                    deserializedWebSocketEvent.payload
                                )
                                if (idBasedDTO.correlationId.isSameAsCurrentClientID()) return@collectLatest

                                val folderId = localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                                if (folderId != null) {
                                    localFoldersRepo.markFolderAsRegularFolder(
                                        folderId, viaChannel = true
                                    ).collectLatest {}
                                }
                            }

                            RemoteRoute.Folder.UPDATE_FOLDER_NAME.name -> {
                                val updateFolderNameDTO =
                                    Json.decodeFromJsonElement<UpdateFolderNameDTO>(
                                        deserializedWebSocketEvent.payload
                                    )
                                if (updateFolderNameDTO.correlationId.isSameAsCurrentClientID()) return@collectLatest

                                val localFolderId =
                                    localFoldersRepo.getLocalIdOfAFolder(updateFolderNameDTO.folderId)
                                if (localFolderId != null) {
                                    localFoldersRepo.getThisFolderData(localFolderId)
                                        .collectLatest {
                                            it.onSuccess {
                                                localFoldersRepo.updateAFolderData(
                                                    it.data.copy(
                                                        localId = localFolderId,
                                                        name = updateFolderNameDTO.newFolderName
                                                    )
                                                ).collectLatest {}
                                            }
                                        }
                                }
                            }

                            RemoteRoute.Folder.UPDATE_FOLDER_NOTE.name -> {
                                val updateFolderNoteDTO =
                                    Json.decodeFromJsonElement<UpdateFolderNoteDTO>(
                                        deserializedWebSocketEvent.payload
                                    )
                                if (updateFolderNoteDTO.correlationId.isSameAsCurrentClientID()) return@collectLatest

                                val localFolderId =
                                    localFoldersRepo.getLocalIdOfAFolder(updateFolderNoteDTO.folderId)
                                if (localFolderId != null) {
                                    localFoldersRepo.getThisFolderData(localFolderId)
                                        .collectLatest {
                                            it.onSuccess {
                                                localFoldersRepo.updateAFolderData(
                                                    it.data.copy(
                                                        localId = localFolderId,
                                                        note = updateFolderNoteDTO.newNote
                                                    )
                                                ).collectLatest {}
                                            }
                                        }
                                }
                            }

                            RemoteRoute.Folder.DELETE_FOLDER_NOTE.name -> {
                                val idBasedDTO = Json.decodeFromJsonElement<IDBasedDTO>(
                                    deserializedWebSocketEvent.payload
                                )
                                if (idBasedDTO.correlationId.isSameAsCurrentClientID()) return@collectLatest

                                val localFolderId =
                                    localFoldersRepo.getLocalIdOfAFolder(idBasedDTO.id)
                                if (localFolderId != null) {
                                    localFoldersRepo.getThisFolderData(localFolderId)
                                        .collectLatest {
                                            it.onSuccess {
                                                localFoldersRepo.deleteAFolderNote(
                                                    localFolderId, viaChannel = true
                                                ).collectLatest {}
                                            }
                                        }
                                }
                            }
                        }

                    }
                }
            }
        }
    }
}