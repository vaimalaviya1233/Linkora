package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.baseUrl
import com.sakethh.linkora.common.utils.defaultFolderIds
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.isATwitterUrl
import com.sakethh.linkora.common.utils.isAValidLink
import com.sakethh.linkora.common.utils.isNotNullOrNotBlank
import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
import com.sakethh.linkora.common.utils.updateLastSyncedWithServerTimeStamp
import com.sakethh.linkora.common.utils.wrappedResultFlow
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType
import com.sakethh.linkora.domain.RemoteRoute
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.asAddLinkDTO
import com.sakethh.linkora.domain.asLinkDTO
import com.sakethh.linkora.domain.dto.server.IDBasedDTO
import com.sakethh.linkora.domain.dto.server.link.DeleteDuplicateLinksDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateNoteOfALinkDTO
import com.sakethh.linkora.domain.dto.server.link.UpdateTitleOfTheLinkDTO
import com.sakethh.linkora.domain.dto.twitter.TwitterMetaDataDTO
import com.sakethh.linkora.domain.mapToResultFlow
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.model.ScrapedLinkInfo
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteLinksRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup
import java.time.Instant

class LocalLinksRepoImpl(
    private val linksDao: LinksDao,
    private val primaryUserAgent: () -> String,
    private val httpClient: HttpClient,
    private val remoteLinksRepo: RemoteLinksRepo,
    private val foldersDao: FoldersDao,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo,
    private val preferencesRepository: PreferencesRepository
) : LocalLinksRepo {
    override suspend fun changeIdOfALink(existingId: Long, newId: Long) {
        linksDao.changeIdOfALink(existingId, newId)
    }

    override suspend fun addANewLink(
        link: Link, linkSaveConfig: LinkSaveConfig, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        if (link.linkType == LinkType.HISTORY_LINK) {
            linksDao.deleteLinksFromHistory(link.url)
        }
        val newLinkId = linksDao.getLatestId() + 1
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.CREATE_A_NEW_LINK.name,
                        payload = if (link.idOfLinkedFolder != null && link.idOfLinkedFolder !in defaultFolderIds()) {
                            Json.encodeToString(
                                linksDao.getLink(newLinkId).copy(
                                    idOfLinkedFolder = link.idOfLinkedFolder,
                                    lastModified = eventTimestamp
                                ).asAddLinkDTO().copy(offlineSyncItemId = newLinkId)
                            )
                        } else {
                            Json.encodeToString(
                                linksDao.getLink(newLinkId).copy(lastModified = eventTimestamp)
                                    .asAddLinkDTO().copy(offlineSyncItemId = newLinkId)
                            )
                        }
                    )
                )
            },
            remoteOperation = {
                if (link.idOfLinkedFolder != null && link.idOfLinkedFolder !in defaultFolderIds()) {
                    val remoteIdOfLinkedFolder =
                        foldersDao.getRemoteIdOfAFolder(link.idOfLinkedFolder)
                    remoteLinksRepo.addANewLink(
                        linksDao.getLink(newLinkId).copy(
                            idOfLinkedFolder = remoteIdOfLinkedFolder, lastModified = eventTimestamp
                        ).asAddLinkDTO()
                    )
                } else {
                    remoteLinksRepo.addANewLink(
                        linksDao.getLink(newLinkId).copy(lastModified = eventTimestamp)
                            .asAddLinkDTO()
                    )
                }
            },
            remoteOperationOnSuccess = {
                linksDao.updateALink(
                    linksDao.getLink(newLinkId).copy(
                        remoteId = it.id, lastModified = it.timeStampBasedResponse.eventTimestamp
                    )
                )
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.timeStampBasedResponse.eventTimestamp)
            }) {
            if (linkSaveConfig.forceSaveWithoutRetrievingData) {
                link.url.isAValidLink().ifNot {
                    throw Link.Invalid()
                }
                linksDao.addANewLink(link.copy(localId = newLinkId, lastModified = eventTimestamp))
                return@performLocalOperationWithRemoteSyncFlow
            }
            if (link.url.isATwitterUrl()) {
                retrieveFromVxTwitterApi(link.url)
            } else {
                scrapeLinkData(
                    link.url, link.userAgent ?: primaryUserAgent()
                )
            }.let { scrapedLinkInfo ->
                linksDao.addANewLink(
                    link.copy(
                        title = if (linkSaveConfig.forceAutoDetectTitle) scrapedLinkInfo.title else link.title,
                        imgURL = scrapedLinkInfo.imgUrl,
                        localId = newLinkId,
                        mediaType = scrapedLinkInfo.mediaType,
                        lastModified = eventTimestamp
                    )
                )
            }
        }
    }

    override suspend fun addMultipleLinks(links: List<Link>) {
        linksDao.addMultipleLinks(links)
    }

    override suspend fun getSortedLinks(
        linkType: LinkType, parentFolderId: Long, sortOption: String
    ): Flow<Result<List<Link>>> {
        return linksDao.getSortedLinks(linkType, parentFolderId, sortOption).mapToResultFlow()
    }

    override fun sortLinksAsNonResultFlow(
        linkType: LinkType, parentFolderId: Long, sortOption: String
    ): Flow<List<Link>> {
        return linksDao.getSortedLinks(linkType, parentFolderId, sortOption)
    }

    override suspend fun getSortedLinks(
        linkType: LinkType, sortOption: String
    ): Flow<Result<List<Link>>> {
        return linksDao.getSortedLinks(linkType, sortOption).mapToResultFlow()
    }

    override fun sortLinksAsNonResultFlow(
        linkType: LinkType, sortOption: String
    ): Flow<List<Link>> {
        return linksDao.getSortedLinks(linkType, sortOption)
    }

    override suspend fun sortAllLinks(sortOption: String): Flow<Result<List<Link>>> {
        return linksDao.getAllLinks(sortOption).mapToResultFlow()
    }


    private suspend fun retrieveFromVxTwitterApi(tweetURL: String): ScrapedLinkInfo {
        val vxTwitterResponseBody =
            httpClient.get("https://api.vxtwitter.com/${tweetURL.substringAfter(".com/")}")
                .body<TwitterMetaDataDTO>()
        return ScrapedLinkInfo(
            title = vxTwitterResponseBody.text,
            imgUrl = vxTwitterResponseBody.media.takeIf { vxTwitterResponseBody.hasMedia && it.isNotEmpty() }
                ?.find { it.type in listOf("image", "video", "gif") }
                ?.let { if (it.type == "image") it.url else it.thumbnailUrl }
                ?: vxTwitterResponseBody.userPfp,
            mediaType = if (vxTwitterResponseBody.media.isNotEmpty() && vxTwitterResponseBody.media.first().type == "video") MediaType.VIDEO else MediaType.IMAGE)
    }

    private suspend fun scrapeLinkData(
        linkUrl: String, userAgent: String
    ): ScrapedLinkInfo {
        val baseUrl: String
        try {
            baseUrl = linkUrl.baseUrl()
        } catch (e: Exception) {
            throw Link.Invalid()
        }
        val rawHTML = withContext(Dispatchers.IO) {
            Jsoup.connect(
                "http" + linkUrl.substringAfter("http").substringBefore(" ").trim()
            ).userAgent(userAgent).followRedirects(true).header("Accept", "text/html")
                .header("Accept-Encoding", "gzip,deflate").header("Accept-Language", "en;q=1.0")
                .header("Connection", "keep-alive").ignoreContentType(true).maxBodySize(0)
                .ignoreHttpErrors(true).get()
        }.toString()

        val document = Jsoup.parse(rawHTML)
        val ogImage = document.select("meta[property=og:image]").attr("content")
        val twitterImage = document.select("meta[name=twitter:image]").attr("content")
        val favicon = document.select("link[rel=icon]").attr("href")
        val ogTitle = document.select("meta[property=og:title]").attr("content")
        val pageTitle = document.title()

        val imgURL = when {
            ogImage.isNotNullOrNotBlank() -> {
                if (ogImage.startsWith("/")) {
                    "https://$baseUrl$ogImage"
                } else {
                    ogImage
                }
            }

            ogImage.isNullOrBlank() && twitterImage.isNotNullOrNotBlank() -> if (twitterImage.startsWith(
                    "/"
                )
            ) {
                "https://$baseUrl$twitterImage"
            } else {
                twitterImage
            }

            ogImage.isNullOrBlank() && twitterImage.isNullOrBlank() && favicon.isNotNullOrNotBlank() -> {
                if (favicon.startsWith("/")) {
                    "https://$baseUrl$favicon"
                } else {
                    favicon
                }
            }

            else -> ""
        }

        val title = when {
            ogTitle.isNotNullOrNotBlank() -> ogTitle
            else -> pageTitle
        }
        return ScrapedLinkInfo(title, imgURL)
    }


    override suspend fun deleteLinksOfFolder(folderId: Long): Flow<Result<Unit>> {
        return wrappedResultFlow {
            linksDao.deleteLinksOfFolder(folderId)
        }
    }

    override suspend fun deleteALinkNote(linkId: Long): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = true,
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.updateALinkNote(
                        UpdateNoteOfALinkDTO(
                            linkId, "", eventTimestamp
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                linksDao.updateLinkTimestamp(timestamp = it.eventTimestamp, linkId)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.UPDATE_LINK_NOTE.name,
                        payload = Json.encodeToString(
                            UpdateNoteOfALinkDTO(
                                linkId, newNote = "", eventTimestamp = eventTimestamp
                            )
                        )
                    )
                )
            }) {
            linksDao.deleteALinkNote(linkId)
            linksDao.updateLinkTimestamp(timestamp = eventTimestamp, linkId)
        }
    }

    override suspend fun deleteALink(linkId: Long, viaSocket: Boolean): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.deleteALink(IDBasedDTO(remoteId, eventTimestamp))
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
            },
            onRemoteOperationFailure = {
                if (remoteId != null){
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.DELETE_A_LINK.name,
                            payload = Json.encodeToString(
                                IDBasedDTO(
                                    remoteId, Instant.now().epochSecond
                                )
                            )
                        )
                    )
                }
            }) {
            linksDao.deleteALink(linkId)
        }
    }

    override suspend fun archiveALink(linkId: Long, viaSocket: Boolean): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.archiveALink(IDBasedDTO(remoteId, eventTimestamp))
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                linksDao.updateLinkTimestamp(it.eventTimestamp, linkId)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.ARCHIVE_LINK.name,
                        payload = Json.encodeToString(
                            IDBasedDTO(
                                linkId, eventTimestamp
                            )
                        )
                    )
                )
            }) {
            linksDao.archiveALink(linkId)
            linksDao.updateLinkTimestamp(eventTimestamp, linkId)
        }
    }

    override suspend fun updateLinkNote(
        linkId: Long, newNote: String, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.updateALinkNote(
                        UpdateNoteOfALinkDTO(
                            remoteId, newNote, eventTimestamp
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                linksDao.updateLinkTimestamp(eventTimestamp, linkId)
            },
            onRemoteOperationFailure = {
                val linkDTO =
                    linksDao.getLink(linkId).copy(note = newNote, lastModified = eventTimestamp)
                        .asLinkDTO(id = linkId)
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.UPDATE_LINK_NOTE.name,
                        payload = Json.encodeToString(linkDTO)
                    )
                )
            }) {
            linksDao.updateLinkNote(linkId, newNote)
            linksDao.updateLinkTimestamp(eventTimestamp, linkId)
        }
    }

    override suspend fun updateLinkTitle(
        linkId: Long, newTitle: String, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.updateLinkTitle(
                        UpdateTitleOfTheLinkDTO(
                            remoteId, newTitle, eventTimestamp
                        )
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                linksDao.updateLinkTimestamp(it.eventTimestamp, linkId)
            },
            onRemoteOperationFailure = {
                val linkDTO =
                    linksDao.getLink(linkId).copy(title = newTitle, lastModified = eventTimestamp)
                        .asLinkDTO(linkId)
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.UPDATE_LINK_TITLE.name,
                        payload = Json.encodeToString(linkDTO)
                    )
                )
            }) {
            linksDao.updateLinkTitle(linkId, newTitle)
            linksDao.updateLinkTimestamp(eventTimestamp, linkId)
        }
    }

    override suspend fun markedAsImportant(linkUrl: String): Flow<Result<Boolean>> {
        return wrappedResultFlow {
            linksDao.markedAsImportant(linkUrl)
        }
    }

    override suspend fun isInArchive(url: String): Flow<Result<Boolean>> {
        return wrappedResultFlow {
            linksDao.isInArchive(url)
        }
    }

    override fun search(query: String, sortOption: String): Flow<Result<List<Link>>> {
        return linksDao.search(query, sortOption).mapToResultFlow()
    }

    override suspend fun getLinksOfThisFolderAsList(folderID: Long): List<Link> {
        return linksDao.getLinksOfThisFolderAsList(folderID)
    }

    override suspend fun getAllLinks(): List<Link> {
        return linksDao.getAllLinks()
    }

    override suspend fun deleteAllLinks() {
        linksDao.deleteAllLinks()
    }

    override suspend fun updateALink(link: Link, viaSocket: Boolean): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(link.localId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.updateLink(link.asLinkDTO(id = remoteId))
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                linksDao.updateLinkTimestamp(it.eventTimestamp, link.localId)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.UPDATE_LINK.name,
                        payload = Json.encodeToString(
                            link.asLinkDTO(id = link.localId).copy(eventTimestamp = eventTimestamp)
                        )
                    )
                )
            }) {
            linksDao.updateALink(link)
            linksDao.updateLinkTimestamp(eventTimestamp, link.localId)
        }
    }

    override suspend fun refreshLinkMetadata(link: Link): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(link.localId)
        val eventTimestamp = Instant.now().epochSecond
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = true,
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.updateLink(
                        linksDao.getLink(link.localId).asLinkDTO(id = remoteId)
                    )
                } else {
                    emptyFlow()
                }
            },
            remoteOperationOnSuccess = {
                preferencesRepository.updateLastSyncedWithServerTimeStamp(it.eventTimestamp)
                linksDao.updateLinkTimestamp(it.eventTimestamp, link.localId)
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.UPDATE_LINK.name,
                        payload = Json.encodeToString(
                            linksDao.getLink(link.localId).asLinkDTO(id = link.localId)
                                .copy(eventTimestamp = eventTimestamp)
                        )
                    )
                )
            }) {
            if (link.url.isATwitterUrl()) {
                retrieveFromVxTwitterApi(link.url)
            } else {
                scrapeLinkData(
                    linkUrl = link.url, userAgent = link.userAgent ?: primaryUserAgent()
                )
            }.let { scrapedLinkInfo ->
                linksDao.updateALink(
                    link.copy(
                        title = scrapedLinkInfo.title,
                        imgURL = scrapedLinkInfo.imgUrl,
                        mediaType = scrapedLinkInfo.mediaType,
                        lastModified = Instant.now().epochSecond
                    )
                )
            }
        }
    }

    private suspend fun getRemoteIdOfLink(localLinkId: Long): Long? {
        return linksDao.getRemoteIdOfLocalLink(localLinkId)
    }

    override suspend fun getLocalLinkId(remoteID: Long): Long? {
        return linksDao.getLocalIdOfALink(remoteID)
    }

    override suspend fun getRemoteLinkId(localId: Long): Long? {
        return linksDao.getRemoteIdOfLocalLink(localId)
    }

    override suspend fun getALink(localLinkId: Long): Link {
        return linksDao.getLink(localLinkId)
    }

    override suspend fun getUnSyncedLinks(): List<Link> {
        return linksDao.getUnSyncedLinks()
    }

    override suspend fun getLatestId(): Long {
        return linksDao.getLatestId()
    }

    override suspend fun doesLinkExist(linkType: LinkType, url: String): Boolean {
        return linksDao.doesLinkExist(linkType, url)
    }

    override suspend fun deleteDuplicateLinks(viaSocket: Boolean): Flow<Result<Unit>> {
        val eventTimestamp = Instant.now().epochSecond
        val linksToBeDeleted = mutableListOf<Link>()
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                remoteLinksRepo.deleteDuplicateLinks(DeleteDuplicateLinksDTO(linkIds = linksToBeDeleted.filter {
                    it.remoteId != null
                }.map { it.remoteId!! }, eventTimestamp = eventTimestamp))
            },
            onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.DELETE_DUPLICATE_LINKS.name,
                        payload = Json.encodeToString(
                            DeleteDuplicateLinksDTO(linkIds = linksToBeDeleted.filterNot {
                            it.remoteId == null
                        }.map {
                            it.remoteId!!
                        }, eventTimestamp = eventTimestamp))
                    )
                )
            },
            localOperation = {
                coroutineScope {
                    val allLinks = linksDao.getAllLinks()
                    val allFolders = allLinks.map {
                        it.idOfLinkedFolder
                    }.filter {
                        it != null && it !in defaultFolderIds()
                    }.distinct()

                    val historyLinks =
                        async { allLinks.filter { it.linkType == LinkType.HISTORY_LINK } }
                    val importantLinks =
                        async { allLinks.filter { it.linkType == LinkType.IMPORTANT_LINK } }
                    val savedLinks = async {
                        allLinks.filter { it.linkType == LinkType.SAVED_LINK }
                    }
                    val archiveLinks = async {
                        allLinks.filter { it.linkType == LinkType.ARCHIVE_LINK }
                    }
                    val folderLinks = async {
                        allLinks.filter { it.linkType == LinkType.FOLDER_LINK }
                    }

                    val filteredHistoryLinks = historyLinks.await().distinctBy {
                        it.url
                    }
                    val filteredImportantLinks = importantLinks.await().distinctBy {
                        it.url
                    }
                    val filteredSavedLinks = savedLinks.await().distinctBy {
                        it.url
                    }
                    val filteredArchiveLinks = archiveLinks.await().distinctBy {
                        it.url
                    }
                    val filteredFolderLinks = mutableListOf<Link>()

                    allFolders.forEach { currentFolderId ->
                        filteredFolderLinks.addAll(folderLinks.await().filter {
                            it.idOfLinkedFolder == currentFolderId
                        }.distinctBy {
                            it.url
                        })
                    }


                    awaitAll(async {
                        linksToBeDeleted.addAll(historyLinks.await().filterNot {
                            filteredHistoryLinks.contains(it)
                        })
                    }, async {
                        linksToBeDeleted.addAll(importantLinks.await().filterNot {
                            filteredImportantLinks.contains(it)
                        })
                    }, async {
                        linksToBeDeleted.addAll(savedLinks.await().filterNot {
                            filteredSavedLinks.contains(it)
                        })
                    }, async {
                        linksToBeDeleted.addAll(archiveLinks.await().filterNot {
                            filteredArchiveLinks.contains(it)
                        })
                    }, async {
                        linksToBeDeleted.addAll(folderLinks.await().filterNot {
                            filteredFolderLinks.contains(it)
                        })
                    })
                    linksDao.deleteLinks(linksToBeDeleted.map { it.localId })
                }
            })
    }

    override suspend fun deleteLinksLocally(linksIds: List<Long>): Flow<Result<Unit>> {
        return wrappedResultFlow {
            linksDao.deleteLinks(linksIds)
        }
    }
}