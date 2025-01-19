package com.sakethh.linkora.data

import com.sakethh.linkora.common.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.common.utils.forceSaveWithoutRetrieving
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.JSONExport
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.repository.ImportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PanelsRepo
import com.sakethh.linkora.utils.LinkoraExports
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.io.File
import java.util.Stack

class ImportDataRepoImpl(
    private val localLinksRepo: LocalLinksRepo,
    private val localFoldersRepo: LocalFoldersRepo,
    private val panelsRepo: PanelsRepo
) : ImportDataRepo {
    override suspend fun importDataFromAJSONFile(file: File): Flow<Result<Unit>> {
        return flow {
            emit(Result.Loading(message = "Starting data import from JSON file: ${file.name}"))

            val deserializedData = file.readText().run {
                emit(Result.Loading(message = "Reading and deserializing JSON file: ${file.name}"))
                Json.decodeFromString<JSONExport>(this)
            }
            emit(Result.Loading(message = "Deserialization completed: Links=${deserializedData.links.size}, Folders=${deserializedData.folders.size}, Panels=${deserializedData.panels.panels.size}, PanelFolders=${deserializedData.panels.panelFolders.size}"))

            emit(Result.Loading(message = "Adding links"))
            localLinksRepo.addMultipleLinks(deserializedData.links)
            emit(Result.Loading(message = "Links added successfully: ${deserializedData.links.size} links"))

            emit(Result.Loading(message = "Adding folders"))
            localFoldersRepo.insertMultipleNewFolders(deserializedData.folders)
            emit(Result.Loading(message = "Folders added successfully: ${deserializedData.folders.size} folders"))

            emit(Result.Loading(message = "Adding panels"))
            panelsRepo.addMultiplePanels(deserializedData.panels.panels)
            emit(Result.Loading(message = "Panels added successfully: ${deserializedData.panels.panels.size} panels"))

            emit(Result.Loading(message = "Adding panel folders"))
            panelsRepo.addMultiplePanelFolders(deserializedData.panels.panelFolders)
            emit(Result.Loading(message = "Panel folders added successfully: ${deserializedData.panels.panelFolders.size} panel folders"))
            emit(Result.Success(Unit))
        }.catchAsThrowableAndEmitFailure()
    }

    override suspend fun importDataFromAHTMLFile(file: File): Flow<Result<Unit>> {
        return channelFlow<Result<Unit>> {
            send(Result.Loading(message = "Starting to import data from HTML file: ${file.name}"))
            retrieveDataFromHTML(Jsoup.parse(file.readText()).body().select("dl").first())
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
                            )
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
                            )
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
                            )
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