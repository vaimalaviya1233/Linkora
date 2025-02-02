package com.sakethh.linkora.data

import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.LinkoraExports
import com.sakethh.linkora.common.utils.catchAsExceptionAndEmitFailure
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.JSONExportSchema
import com.sakethh.linkora.domain.model.PanelForJSONExportSchema
import com.sakethh.linkora.domain.repository.ExportDataRepo
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.LocalPanelsRepo
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.supervisorScope
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class ExportDataRepoImpl(
    private val localLinksRepo: LocalLinksRepo,
    private val localFoldersRepo: LocalFoldersRepo,
    private val localPanelsRepo: LocalPanelsRepo
) : ExportDataRepo {
    override suspend fun exportDataAsJSON(): Flow<Result<RawExportString>> {
        return flow {
            supervisorScope {
                emit(Result.Loading(message = Localization.Key.PreparingToExportYourData.getLocalizedString()))
                val deferredLinks = async {
                    localLinksRepo.getAllLinks()
                }
                val deferredFolders = async {
                    localFoldersRepo.getAllFoldersAsList()
                }
                val deferredPanels = async {
                    localPanelsRepo.getAllThePanelsAsAList()
                }
                val deferredPanelFolders = async {
                    localPanelsRepo.getAllThePanelFoldersAsAList()
                }

                emit(Result.Loading(message = Localization.Key.CollectingLinksForExport.getLocalizedString()))
                val links = deferredLinks.await()

                emit(Result.Loading(message = Localization.Key.CollectingFoldersForExport.getLocalizedString()))
                val folders = deferredFolders.await()

                emit(Result.Loading(message = Localization.Key.CollectingPanelsForExport.getLocalizedString()))
                val panels = deferredPanels.await()

                emit(
                    Result.Loading(
                        message = Localization.Key.CollectingPanelFoldersForExport.getLocalizedString()
                    )
                )
                val panelFolders = deferredPanelFolders.await()

                emit(Result.Loading(message = Localization.Key.SerializingCollectedDataForExport.getLocalizedString()))

                val exportObject = JSONExportSchema(
                    schemaVersion = Constants.EXPORT_SCHEMA_VERSION,
                    links = links,
                    folders = folders,
                    panels = PanelForJSONExportSchema(
                        panels = panels, panelFolders = panelFolders
                    ),
                )
                val requiredRawExportString = Json.encodeToString(exportObject)
                emit(Result.Success(data = requiredRawExportString))
            }
        }.catchAsExceptionAndEmitFailure()
    }

    override suspend fun exportDataAsHTMl(): Flow<Result<RawExportString>> {
        return channelFlow<Result<RawExportString>> {
            supervisorScope {
                var htmlFileRawText = ""

                send(Result.Loading(message = "Starting export data as HTML"))

                val allLinks = localLinksRepo.getAllLinks()
                send(Result.Loading(message = "Fetched all links, total: ${allLinks.size}"))

                var savedLinksSection = dtH3(LinkoraExports.SAVED_LINKS__LINKORA_EXPORT.name)
                send(Result.Loading(message = "Processing saved links"))

                var savedLinks = ""
                val deferredSavedLinks = async {
                    allLinks.filter { it.linkType == LinkType.SAVED_LINK }.forEach { savedLink ->
                        savedLinks += dtA(linkTitle = savedLink.title, link = savedLink.url)
                        send(Result.Loading(message = "Processed saved link: ${savedLink.title}"))
                    }
                }

                var impLinksSection = dtH3(LinkoraExports.IMPORTANT_LINKS__LINKORA_EXPORT.name)
                send(Result.Loading(message = "Processing important links"))

                var impLinks = ""
                val deferredImpLinks = async {
                    allLinks.filter { it.linkType == LinkType.IMPORTANT_LINK }.forEach { impLink ->
                        impLinks += dtA(linkTitle = impLink.title, link = impLink.url)
                        send(Result.Loading(message = "Processed important link: ${impLink.title}"))
                    }
                }

                send(Result.Loading(message = "Processing regular folders and respective links"))
                val deferredRegularFoldersAndRespectiveLinks = async {
                    dtH3(LinkoraExports.REGULAR_FOLDERS__LINKORA_EXPORT.name) + dlP(
                        foldersSectionInHtml(parentFolderId = null, forArchiveFolders = false)
                    )
                }

                send(Result.Loading(message = "Processing archived folders and respective links"))
                val deferredArchivedFoldersAndRespectiveLinks = async {
                    dtH3(LinkoraExports.ARCHIVED_FOLDERS__LINKORA_EXPORT.name) + dlP(
                        foldersSectionInHtml(parentFolderId = null, forArchiveFolders = true)
                    )
                }

                var historyLinksSection = dtH3(LinkoraExports.HISTORY_LINKS__LINKORA_EXPORT.name)
                send(Result.Loading(message = "Processing history links"))

                var historyLinks = ""
                val deferredHistoryLinks = async {
                    allLinks.filter { it.linkType == LinkType.HISTORY_LINK }
                        .forEach { historyLink ->
                            historyLinks += dtA(
                                linkTitle = historyLink.title,
                                link = historyLink.url
                            )
                            send(Result.Loading(message = "Processed history link: ${historyLink.title}"))
                        }
                }

                var archivedLinksSection = dtH3(LinkoraExports.ARCHIVED_LINKS__LINKORA_EXPORT.name)
                send(Result.Loading(message = "Processing archived links"))

                var archivedLinks = ""
                val deferredArchivedLinks = async {
                    allLinks.filter { it.linkType == LinkType.ARCHIVE_LINK }
                        .forEach { archivedLink ->
                            archivedLinks += dtA(
                                linkTitle = archivedLink.title,
                                link = archivedLink.url
                            )
                            send(Result.Loading(message = "Processed archived link: ${archivedLink.title}"))
                        }
                }

                deferredSavedLinks.await()
                savedLinksSection += dlP(savedLinks)
                htmlFileRawText += savedLinksSection

                deferredImpLinks.await()
                impLinksSection += dlP(impLinks)
                htmlFileRawText += impLinksSection

                htmlFileRawText += deferredRegularFoldersAndRespectiveLinks.await()

                htmlFileRawText += deferredArchivedFoldersAndRespectiveLinks.await()

                deferredHistoryLinks.await()
                historyLinksSection += dlP(historyLinks)
                htmlFileRawText += historyLinksSection

                deferredArchivedLinks.await()
                archivedLinksSection += dlP(archivedLinks)
                htmlFileRawText += archivedLinksSection

                send(Result.Loading(message = "Completed export process, finalizing HTML"))

                send(Result.Success(dlP(htmlFileRawText)))
            }
        }.catchAsExceptionAndEmitFailure()
    }


    private fun dlP(children: String): String {
        return "<DL><p>\n$children</DL><p>\n"
    }

    private fun dtH3(folderName: String): String {
        return "<DT><H3>$folderName</H3>\n"
    }

    private fun dtA(linkTitle: String, link: String): String {
        return "<DT><A HREF=\"$link\">$linkTitle</A>\n"
    }


    private suspend fun <T> SendChannel<Result<T>>.foldersSectionInHtml(
        parentFolderId: Long?, forArchiveFolders: Boolean,
    ): String {

        var foldersSection = ""

        val foldersList = if (parentFolderId == null) {
            send(Result.Loading(message = "Fetching all top-level folders"))
            localFoldersRepo.getAllRootFoldersAsList().filter {
                it.isArchived == forArchiveFolders
            }
        } else {
            send(Result.Loading(message = "Fetching child folders for parent ID: $parentFolderId"))
            localFoldersRepo.getChildFoldersOfThisParentIDAsList(parentFolderId)
        }

        send(Result.Loading(message = "Found ${foldersList.size} folders to process"))

        foldersList.forEach { childFolder ->
            send(Result.Loading(message = "Processing folder: ${childFolder.name} (ID: ${childFolder.localId})"))

            val currentFolderDTH3 = dtH3(childFolder.name)
            var folderLinksDTA = ""

            val linksList = localLinksRepo.getLinksOfThisFolderAsList(childFolder.localId)
            send(Result.Loading(message = "Found ${linksList.size} links in folder: ${childFolder.name}"))

            linksList.forEach { filteredLink ->
                send(Result.Loading(message = "Processing link: ${filteredLink.title} (URL: ${filteredLink.url}) in folder: ${childFolder.name}"))
                folderLinksDTA += dtA(linkTitle = filteredLink.title, link = filteredLink.url)
            }

            send(Result.Loading(message = "Completed links for folder: ${childFolder.name}"))

            val nestedFolderHTML = foldersSectionInHtml(childFolder.localId, forArchiveFolders)
            foldersSection += currentFolderDTH3 + dlP(folderLinksDTA + nestedFolderHTML)

            if (childFolder.parentFolderId == null) {
                if (forArchiveFolders) {
                    send(Result.Loading(message = "Top-level archived folder ${childFolder.name} processed"))
                } else {
                    send(Result.Loading(message = "Top-level non-archived folder ${childFolder.name} processed"))
                }
            }

            send(Result.Loading(message = "Finished processing folder: ${childFolder.name} (ID: ${childFolder.localId})"))
        }

        return foldersSection
    }

}