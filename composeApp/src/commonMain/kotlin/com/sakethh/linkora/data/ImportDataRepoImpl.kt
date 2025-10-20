package com.sakethh.linkora.data

import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.asJSONExportSchema
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.JSONExportSchema
import com.sakethh.linkora.domain.model.PanelForJSONExportSchema
import com.sakethh.linkora.domain.model.legacy.LegacyExportSchema
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.model.tag.LinkTag
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.ImportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import com.sakethh.linkora.domain.repository.local.LocalTagsRepo
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import com.sakethh.linkora.utils.LinkoraExports
import com.sakethh.linkora.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.utils.isNull
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.util.Stack

class ImportDataRepoImpl(
    private val localLinksRepo: LocalLinksRepo,
    private val localFoldersRepo: LocalFoldersRepo,
    private val localPanelsRepo: LocalPanelsRepo,
    private val localTagsRepo: LocalTagsRepo,
    private val remoteSyncRepo: RemoteSyncRepo,
    private val canPushToServer: () -> Boolean,
) : ImportDataRepo {
    private val json = Json {
        ignoreUnknownKeys = true
    }

    override suspend fun importDataFromAJSONFile(file: File): Flow<Result<Unit>> {
        return channelFlow<Result<Unit>> {
            send(Result.Loading(message = "Starting data import from JSON file: ${file.name}"))

            send(Result.Loading(message = "Reading and deserializing JSON file: ${file.name}"))
            val rawImportString = file.readText()
            val basedOnNewExportSchema = try {
                json.parseToJsonElement(rawImportString).jsonObject["schemaVersion"].toString()
                    .toLong().let {
                        it > 11
                    }
            } catch (_: Exception) {
                false
            }
            val deserializedData = if (basedOnNewExportSchema.not()) {
                Json.decodeFromString<LegacyExportSchema>(rawImportString).asJSONExportSchema()
            } else rawImportString.run {
                json.decodeFromString<JSONExportSchema>(this)
            }.run {
                JSONExportSchema(
                    schemaVersion = schemaVersion,
                    links = links.map {
                        it.copy(remoteId = null, lastModified = 0)
                    },
                    folders = folders.map {
                        it.copy(remoteId = null, lastModified = 0)
                    },
                    panels = PanelForJSONExportSchema(panels = panels.panels.map {
                        it.copy(remoteId = null, lastModified = 0)
                    }, panelFolders = panels.panelFolders.map {
                        it.copy(remoteId = null, lastModified = 0)
                    }),
                    tags = tags.map { it.copy(remoteId = null, lastModified = 0) },
                    linkTags = linkTags.map { it.copy(remoteId = null, lastModified = 0) })
            }

            send(
                Result.Loading(
                    message = if (basedOnNewExportSchema.not()) {
                        "This JSON file is based on the legacy export schema (v${deserializedData.schemaVersion})."
                    } else {
                        "This JSON file is based on schema version ${deserializedData.schemaVersion}."
                    }
                )
            )

            send(Result.Loading(message = "Deserialization completed: Links=${deserializedData.links.size}, Folders=${deserializedData.folders.size}, Panels=${deserializedData.panels.panels.size}, PanelFolders=${deserializedData.panels.panelFolders.size}"))

            send(Result.Loading(message = "Filtering non-folder linked links."))
            val srcNonFolderLinks = deserializedData.links.filter {
                it.linkType != LinkType.FOLDER_LINK
            }
            val destNonFolderLinks = srcNonFolderLinks.map {
                it.copy(localId = 0)
            }
            val updatedLinkTags = mutableListOf<LinkTag>()

            fun mapLinkTagsToNewLinkIds(
                previousLinkIds: List<Long>, newLinkIds: List<Long>
            ): List<LinkTag> {
                require(previousLinkIds.size == newLinkIds.size)

                val linkIdPair = previousLinkIds.mapIndexed { index, previousId ->
                    previousId to newLinkIds[index]
                }

                return deserializedData.linkTags.filter {
                    it.linkId in previousLinkIds
                }.map { filteredLinkTag ->
                    filteredLinkTag.copy(linkId = linkIdPair.find {
                        it.first == filteredLinkTag.linkId
                    }?.second ?: -54544)
                }
            }

            send(Result.Loading(message = "Adding non-folder linked links to local repository."))
            localLinksRepo.addMultipleLinks(destNonFolderLinks).let { newLinkIds ->
                updatedLinkTags.addAll(
                    mapLinkTagsToNewLinkIds(
                        previousLinkIds = srcNonFolderLinks.map { it.localId },
                        newLinkIds = newLinkIds
                    )
                )
            }
            var latestFolderId = localFoldersRepo.getLatestFoldersTableID()
            send(Result.Loading(message = "Retrieved latest folder ID: $latestFolderId"))
            val updatedPanelFolders = mutableListOf<PanelFolder>()

            deserializedData.folders.filter {
                it.parentFolderId == null
            }.forEach { currentFolder ->
                val newParentFolderId = ++latestFolderId

                localFoldersRepo.insertANewFolder(
                    currentFolder.copy(localId = newParentFolderId),
                    ignoreFolderAlreadyExistsException = true,
                    viaSocket = true
                ).collectLatest { it ->
                    it.onFailure {
                        throw Throwable(it)
                    }
                }

                suspend fun insertChildFolders(childFolders: List<Folder>, parentFolderId: Long) {
                    childFolders.forEach { childFolder ->
                        val newChildFolderId = ++latestFolderId
                        localFoldersRepo.insertANewFolder(
                            childFolder.copy(
                                localId = newChildFolderId, parentFolderId = parentFolderId
                            ), ignoreFolderAlreadyExistsException = true, viaSocket = true
                        ).collectLatest { it ->
                            it.onFailure {
                                throw Throwable(it)
                            }
                        }

                        val srcFolderLinks = deserializedData.links.filter {
                            it.idOfLinkedFolder == childFolder.localId && it.linkType == LinkType.FOLDER_LINK
                        }

                        val destFolderLinks = srcFolderLinks.map {
                            it.copy(localId = 0, idOfLinkedFolder = newChildFolderId)
                        }

                        localLinksRepo.addMultipleLinks(destFolderLinks).let { newLinkIds ->
                            updatedLinkTags.addAll(
                                mapLinkTagsToNewLinkIds(
                                    previousLinkIds = srcFolderLinks.map { it.localId },
                                    newLinkIds = newLinkIds
                                )
                            )
                        }

                        updatedPanelFolders.addAll(deserializedData.panels.panelFolders.filter {
                            it.folderId == childFolder.localId
                        }.map { it.copy(folderId = newChildFolderId) })

                        insertChildFolders(deserializedData.folders.filter {
                            it.parentFolderId == childFolder.localId
                        }, newChildFolderId)
                    }
                }

                insertChildFolders(deserializedData.folders.filter {
                    it.parentFolderId == currentFolder.localId
                }, newParentFolderId)

                val srcFolderLinks = deserializedData.links.filter {
                    it.idOfLinkedFolder == currentFolder.localId && it.linkType == LinkType.FOLDER_LINK
                }
                val destFolderLinks = srcFolderLinks.map {
                    it.copy(localId = 0, idOfLinkedFolder = newParentFolderId)
                }

                localLinksRepo.addMultipleLinks(destFolderLinks).let { newLinkIds ->
                    updatedLinkTags.addAll(
                        mapLinkTagsToNewLinkIds(
                            previousLinkIds = srcFolderLinks.map { it.localId },
                            newLinkIds = newLinkIds
                        )
                    )
                }

                updatedPanelFolders.addAll(deserializedData.panels.panelFolders.filter {
                    it.folderId == currentFolder.localId
                }.map { it.copy(folderId = newParentFolderId) })
            }

            var latestPanelId = localPanelsRepo.getLatestPanelID()
            send(Result.Loading(message = "Retrieved latest panel ID: $latestPanelId"))

            deserializedData.panels.panels.forEach { currentPanel ->
                ++latestPanelId
                send(Result.Loading(message = "Assigned new ID=$latestPanelId to panel: ${currentPanel.panelName}"))

                send(Result.Loading(message = "Inserting panel: ${currentPanel.panelName} with new ID=$latestPanelId"))
                val updatedPanelData = currentPanel.copy(localId = latestPanelId)
                localPanelsRepo.addaNewPanel(updatedPanelData).collectLatest {
                    it.onSuccess {
                        send(Result.Loading(message = "Inserted panel: ${currentPanel.panelName} with new ID=$latestPanelId"))
                    }
                }

                send(Result.Loading(message = "Adding panel folder associations for panel: ${currentPanel.panelName}"))
                localPanelsRepo.addMultiplePanelFolders(updatedPanelFolders.filter {
                    it.connectedPanelId == currentPanel.localId
                }.map {
                    it.copy(connectedPanelId = latestPanelId, localId = 0)
                })
            }

            deserializedData.tags.forEach { currentTag ->
                localTagsRepo.createATag(currentTag.copy(localId = 0)).collectLatest {
                    it.onSuccess { currTagNewId ->
                        localTagsRepo.createLinkTags(updatedLinkTags.filter {
                            it.tagId == currentTag.localId
                        }.map {
                            it.copy(tagId = currTagNewId.data)
                        })
                    }
                }
            }

            if (canPushToServer()) {
                send(Result.Loading(message = "Server is configured. Initiating push."))
                with(remoteSyncRepo) {
                    this@channelFlow.pushNonSyncedDataToServer()
                }
            } else {
                send(Result.Loading(message = "Server is not configured. Skipping push."))
            }
            send(Result.Success(Unit))
        }.catchAsThrowableAndEmitFailure()
    }

    override suspend fun importDataFromAHTMLFile(file: File): Flow<Result<Unit>> {
        return channelFlow<Result<Unit>> {
            send(Result.Loading(message = "Starting to import data from HTML file: ${file.name}"))
            retrieveDataFromHTML(Jsoup.parse(file.readText()).body().select("dl").first())
            if (canPushToServer()) {
                send(Result.Loading(message = "Server is configured. Initiating push."))
                with(remoteSyncRepo) {
                    this@channelFlow.pushNonSyncedDataToServer()
                }
            } else {
                send(Result.Loading(message = "Server is not configured. Skipping push."))
            }
            send(Result.Success(Unit))
        }.catchAsThrowableAndEmitFailure()
    }

    private val foldersIdStackForRetrievingDataFromHTML = Stack<Long>()
    private val foldersNameStackForRetrievingDataFromHTML = Stack<String>()

    private suspend fun <T> SendChannel<Result<T>>.retrieveDataFromHTML(element: Element?) {
        if (element.isNull()) {
            send(Result.Loading(message = "No HTML element to process"))
            return
        }

        // `element!!` is safe here because we've already checked `element.isNull()` and won't reach this point if it's null

        element!!.children().filter { child -> child.`is`("dt") }.forEach { filteredDtElement ->
            filteredDtElement.children().forEach { filteredDtChildElement ->
                when {
                    filteredDtChildElement.`is`("a") -> {
                        val linkAddress = filteredDtChildElement.attribute("href").value
                        val linkTitle = filteredDtChildElement.text()

                        val parentFolderId =
                            if (foldersIdStackForRetrievingDataFromHTML.isNotEmpty()) {
                                foldersIdStackForRetrievingDataFromHTML.peek()
                            } else -1

                        send(Result.Loading(message = "Found link: Title = $linkTitle, Address = $linkAddress, Parent Folder ID = $parentFolderId"))

                        if (foldersNameStackForRetrievingDataFromHTML.isNotEmpty() && foldersNameStackForRetrievingDataFromHTML.peek() in listOf(
                                LinkoraExports.IMPORTANT_LINKS__LINKORA_EXPORT.name,
                                LinkoraExports.HISTORY_LINKS__LINKORA_EXPORT.name,
                                LinkoraExports.ARCHIVED_LINKS__LINKORA_EXPORT.name
                            )
                        ) {
                            send(Result.Loading(message = "Link is part of (Important, History, Archived)"))
                            try {
                                localLinksRepo.addANewLink(
                                    link = Link(
                                        linkType = when (foldersNameStackForRetrievingDataFromHTML.peek()) {
                                            LinkoraExports.IMPORTANT_LINKS__LINKORA_EXPORT.name -> LinkType.IMPORTANT_LINK
                                            LinkoraExports.HISTORY_LINKS__LINKORA_EXPORT.name -> LinkType.HISTORY_LINK
                                            LinkoraExports.ARCHIVED_LINKS__LINKORA_EXPORT.name -> LinkType.ARCHIVE_LINK
                                            else -> return
                                        },
                                        title = linkTitle,
                                        url = linkAddress,
                                        imgURL = "",
                                        note = "",
                                        idOfLinkedFolder = if (parentFolderId == (-1).toLong()) null else parentFolderId,
                                    ),
                                    linkSaveConfig = LinkSaveConfig.forceSaveWithoutRetrieving(),
                                    selectedTagIds = null
                                ).collect()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        } else {
                            send(Result.Loading(message = "Link is part of saved links or folder links"))
                            try {
                                localLinksRepo.addANewLink(
                                    link = Link(
                                        linkType = if (parentFolderId == (-1).toLong()) LinkType.SAVED_LINK else LinkType.FOLDER_LINK,
                                        title = linkTitle,
                                        url = linkAddress,
                                        imgURL = "",
                                        note = "",
                                        idOfLinkedFolder = if (parentFolderId == (-1).toLong()) null else parentFolderId,
                                    ),
                                    linkSaveConfig = LinkSaveConfig.forceSaveWithoutRetrieving(),
                                    selectedTagIds = null
                                ).collect()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }

                    filteredDtChildElement.`is`("dl") -> {
                        val folderName = filteredDtChildElement.siblingElements().first()?.text()
                        val parentFolder =
                            if (foldersIdStackForRetrievingDataFromHTML.isNotEmpty()) {
                                foldersIdStackForRetrievingDataFromHTML.peek()
                            } else -1

                        send(Result.Loading(message = "Found folder: Name = $folderName, Parent Folder ID = $parentFolder"))

                        if (!LinkoraExports.entries.map { it.name }.contains(folderName)) {
                            send(Result.Loading(message = "Folder does not exist, inserting new folder: $folderName"))
                            try {
                                localFoldersRepo.insertANewFolder(
                                    folder = Folder(
                                        name = folderName.toString(),
                                        note = "",
                                        parentFolderId = if (parentFolder == (-1).toLong()) null else parentFolder,
                                        isArchived = foldersNameStackForRetrievingDataFromHTML.isNotEmpty() && foldersNameStackForRetrievingDataFromHTML.peek() == LinkoraExports.ARCHIVED_FOLDERS__LINKORA_EXPORT.name
                                    ), ignoreFolderAlreadyExistsException = true
                                ).collect()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                            foldersIdStackForRetrievingDataFromHTML.push(localFoldersRepo.getLatestFoldersTableID())
                        }

                        foldersNameStackForRetrievingDataFromHTML.push(folderName)

                        retrieveDataFromHTML(filteredDtChildElement)
                        send(Result.Loading(message = "Returning from folder processing: $folderName"))

                        if (foldersIdStackForRetrievingDataFromHTML.isNotEmpty()) {
                            foldersIdStackForRetrievingDataFromHTML.pop()
                        }
                        if (foldersNameStackForRetrievingDataFromHTML.isNotEmpty()) {
                            foldersNameStackForRetrievingDataFromHTML.pop()
                        }
                    }

                    else -> Unit
                }
            }
        }
    }
}