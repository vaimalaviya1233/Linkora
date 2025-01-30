package com.sakethh.linkora.data

import com.sakethh.linkora.common.utils.LinkoraExports
import com.sakethh.linkora.common.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.common.utils.excludeLocalId
import com.sakethh.linkora.common.utils.forceSaveWithoutRetrieving
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.JSONExport
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.panel.PanelFolder
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.ImportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.util.Stack

class ImportDataRepoImpl(
    private val localLinksRepo: LocalLinksRepo,
    private val localFoldersRepo: LocalFoldersRepo,
    private val localPanelsRepo: LocalPanelsRepo
) : ImportDataRepo {
    override suspend fun importDataFromAJSONFile(file: File): Flow<Result<Unit>> {
        return channelFlow<Result<Unit>> {
            send(Result.Loading(message = "Starting data import from JSON file: ${file.name}"))

            send(Result.Loading(message = "Reading and deserializing JSON file: ${file.name}"))
            val deserializedData = file.readText().run {
                Json.decodeFromString<JSONExport>(this)
            }

            send(Result.Loading(message = "Deserialization completed: Links=${deserializedData.links.size}, Folders=${deserializedData.folders.size}, Panels=${deserializedData.panels.panels.size}, PanelFolders=${deserializedData.panels.panelFolders.size}"))

            send(Result.Loading(message = "Filtering non-folder linked links."))
            val nonFolderLinkedLinks = deserializedData.links.filter {
                it.linkType != LinkType.FOLDER_LINK
            }.map {
                it.excludeLocalId()
            }

            send(Result.Loading(message = "Adding non-folder linked links to local repository."))
            localLinksRepo.addMultipleLinks(nonFolderLinkedLinks)

            var latestFolderId = localFoldersRepo.getLatestFoldersTableID()
            send(Result.Loading(message = "Retrieved latest folder ID: $latestFolderId"))
            val updatedPanelFolders = mutableListOf<PanelFolder>()

            deserializedData.folders.forEach { currentFolder ->
                ++latestFolderId
                send(Result.Loading(message = "Assigned new ID=$latestFolderId to folder: ${currentFolder.name}"))

                send(Result.Loading(message = "Updating links for folder: ${currentFolder.name} with new ID=$latestFolderId"))
                val updatedLinks = deserializedData.links.filter {
                    it.idOfLinkedFolder == currentFolder.localId && it.linkType == LinkType.FOLDER_LINK
                }.map {
                    it.excludeLocalId().copy(idOfLinkedFolder = latestFolderId)
                }

                send(Result.Loading(message = "Inserting folder: ${currentFolder.name} with new ID=$latestFolderId"))
                localFoldersRepo.insertANewFolder(
                    currentFolder.copy(localId = latestFolderId),
                    ignoreFolderAlreadyExistsException = true
                ).collectLatest { it ->
                    it.onFailure {
                        throw Throwable(it)
                    }
                }

                send(Result.Loading(message = "Adding folder links for: ${currentFolder.name}"))
                localLinksRepo.addMultipleLinks(updatedLinks)

                send(Result.Loading(message = "Updating panel folders for folder: ${currentFolder.name}"))
                updatedPanelFolders.addAll(deserializedData.panels.panelFolders.filter {
                    it.folderId == currentFolder.localId
                }.map { it.copy(folderId = latestFolderId) })
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
            send(Result.Success(Unit))
        }.catchAsThrowableAndEmitFailure()
    }

    override suspend fun importDataFromAHTMLFile(file: File): Flow<Result<Unit>> {
        return channelFlow<Result<Unit>> {
            send(Result.Loading(message = "Starting to import data from HTML file: ${file.name}"))
            retrieveDataFromHTML(Jsoup.parse(file.readText()).body().select("dl").first())
            send(Result.Success(Unit))
        }.catchAsThrowableAndEmitFailure()
    }

    private val foldersIdStackForRetrievingDataFromHTML = Stack<Long>()
    private val foldersNameStackForRetrievingDataFromHTML = Stack<String>()

    private suspend fun <T> SendChannel<Result<T>>.retrieveDataFromHTML(element: Element?) {
        if (element.isNull()) {
            send(Result.Loading(message = "Element is null, returning."))
            send(Result.Loading(message = "No HTML element to process"))
            return
        }

        // `element!!` is safe here because we've already checked `element.isNull()` and won't reach this point if it's null

        element!!.children().filter { child -> child.`is`("dt") }.forEach { filteredDtElement ->
            send(Result.Loading(message = "Processing <dt> element: ${filteredDtElement.outerHtml()}"))
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
                                ), linkSaveConfig = forceSaveWithoutRetrieving()
                            ).collect()
                        } else {
                            send(Result.Loading(message = "Link is part of saved links or folder links"))
                            localLinksRepo.addANewLink(
                                link = Link(
                                    linkType = if (parentFolderId == (-1).toLong()) LinkType.SAVED_LINK else LinkType.FOLDER_LINK,
                                    title = linkTitle,
                                    url = linkAddress,
                                    imgURL = "",
                                    note = "",
                                    idOfLinkedFolder = if (parentFolderId == (-1).toLong()) null else parentFolderId,
                                ), linkSaveConfig = forceSaveWithoutRetrieving()
                            ).collect()
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
                            localFoldersRepo.insertANewFolder(
                                folder = Folder(
                                    name = folderName.toString(),
                                    note = "",
                                    parentFolderId = if (parentFolder == (-1).toLong()) null else parentFolder,
                                    isArchived = foldersNameStackForRetrievingDataFromHTML.isNotEmpty() && foldersNameStackForRetrievingDataFromHTML.peek() == LinkoraExports.ARCHIVED_FOLDERS__LINKORA_EXPORT.name
                                ), ignoreFolderAlreadyExistsException = true
                            ).collect()
                            foldersIdStackForRetrievingDataFromHTML.push(localFoldersRepo.getLatestFoldersTableID())
                        } else {
                            send(Result.Loading(message = "Folder exists, skipping creation"))
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