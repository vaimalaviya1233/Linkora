package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.baseUrl
import com.sakethh.linkora.common.utils.defaultFolderIds
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.isATwitterUrl
import com.sakethh.linkora.common.utils.isAValidLink
import com.sakethh.linkora.common.utils.isNotNullOrNotBlank
import com.sakethh.linkora.common.utils.performLocalOperationWithRemoteSyncFlow
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
import com.sakethh.linkora.domain.dto.server.link.UpdateNoteOfALinkDTO
import com.sakethh.linkora.domain.dto.twitter.TwitterMetaDataDTO
import com.sakethh.linkora.domain.mapToResultFlow
import com.sakethh.linkora.domain.model.PendingSyncQueue
import com.sakethh.linkora.domain.model.ScrapedLinkInfo
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PendingSyncQueueRepo
import com.sakethh.linkora.domain.repository.remote.RemoteLinksRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jsoup.Jsoup

class LocalLinksRepoImpl(
    private val linksDao: LinksDao,
    private val primaryUserAgent: () -> String,
    private val httpClient: HttpClient,
    private val remoteLinksRepo: RemoteLinksRepo,
    private val foldersDao: FoldersDao,
    private val pendingSyncQueueRepo: PendingSyncQueueRepo
) : LocalLinksRepo {
    override suspend fun addANewLink(
        link: Link, linkSaveConfig: LinkSaveConfig, viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val newLinkId = linksDao.getLatestId() + 1
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(), onRemoteOperationFailure = {
                pendingSyncQueueRepo.addInQueue(
                    PendingSyncQueue(
                        operation = RemoteRoute.Link.CREATE_A_NEW_LINK.name,
                        payload = if (link.idOfLinkedFolder != null && link.idOfLinkedFolder !in defaultFolderIds()) {
                            val remoteIdOfLinkedFolder =
                                foldersDao.getRemoteIdOfAFolder(link.idOfLinkedFolder)
                            Json.encodeToString(
                                linksDao.getLink(newLinkId)
                                    .copy(idOfLinkedFolder = remoteIdOfLinkedFolder).asAddLinkDTO()
                                    .copy(pendingQueueSyncLocalId = link.localId)
                            )
                        } else {
                            Json.encodeToString(
                                linksDao.getLink(newLinkId).copy().asAddLinkDTO()
                                    .copy(pendingQueueSyncLocalId = link.localId)
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
                        linksDao.getLink(newLinkId).copy(idOfLinkedFolder = remoteIdOfLinkedFolder)
                            .asAddLinkDTO()
                    )
                } else {
                    remoteLinksRepo.addANewLink(linksDao.getLink(newLinkId).copy().asAddLinkDTO())
                }
            },
            remoteOperationOnSuccess = {
                linksDao.updateALink(
                    linksDao.getLink(newLinkId).copy(remoteId = it.id)
                )
            }) {
            if (linkSaveConfig.forceSaveWithoutRetrievingData) {
                link.url.isAValidLink().ifNot {
                    throw Link.Invalid()
                }
                linksDao.addANewLink(link.copy(localId = newLinkId))
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
                        mediaType = scrapedLinkInfo.mediaType
                    )
                )
            }
        }
    }

    override suspend fun addMultipleLinks(links: List<Link>) {
        linksDao.addMultipleLinks(links)
    }

    override suspend fun sortLinks(
        linkType: LinkType, parentFolderId: Long, sortOption: String
    ): Flow<Result<List<Link>>> {
        return linksDao.sortLinks(linkType, parentFolderId, sortOption).mapToResultFlow()
    }

    override fun sortLinksAsNonResultFlow(
        linkType: LinkType,
        parentFolderId: Long,
        sortOption: String
    ): Flow<List<Link>> {
        return linksDao.sortLinks(linkType, parentFolderId, sortOption)
    }
    override suspend fun sortLinks(
        linkType: LinkType, sortOption: String
    ): Flow<Result<List<Link>>> {
        return linksDao.sortLinks(linkType, sortOption).mapToResultFlow()
    }

    override fun sortLinksAsNonResultFlow(
        linkType: LinkType,
        sortOption: String
    ): Flow<List<Link>> {
        return linksDao.sortLinks(linkType, sortOption)
    }
    override suspend fun sortAllLinks(sortOption: String): Flow<Result<List<Link>>> {
        return linksDao.sortAllLinks(sortOption).mapToResultFlow()
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
            mediaType = if (vxTwitterResponseBody.media.first().type == "video") MediaType.VIDEO else MediaType.IMAGE)
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
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = true,
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.renameALinkNote(remoteId, newNote = "")
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                if (remoteId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.UPDATE_LINK_NOTE.name,
                            payload = Json.encodeToString(
                                UpdateNoteOfALinkDTO(
                                    remoteId, newNote = "", pendingQueueSyncLocalId = linkId
                                )
                            )
                        )
                    )
                }
            }) {
            linksDao.deleteALinkNote(linkId)
        }
    }

    override suspend fun deleteALink(linkId: Long, viaSocket: Boolean): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.deleteALink(remoteId)
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                if (remoteId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.DELETE_A_LINK.name,
                            payload = Json.encodeToString(
                                IDBasedDTO(
                                    remoteId,
                                    pendingQueueSyncLocalId = linkId
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
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.archiveALink(remoteId)
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                if (remoteId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.ARCHIVE_LINK.name,
                            payload = Json.encodeToString(
                                IDBasedDTO(
                                    remoteId,
                                    pendingQueueSyncLocalId = linkId
                                )
                            )
                        )
                    )
                }
            }) {
            linksDao.archiveALink(linkId)
        }
    }

    override suspend fun updateLinkNote(
        linkId: Long, newNote: String,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.renameALinkNote(remoteId, newNote)
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                if (remoteId != null) {
                    val linkDTO = linksDao.getLink(linkId).copy(note = newNote).asLinkDTO(remoteId)
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.UPDATE_LINK_NOTE.name,
                            payload = Json.encodeToString(linkDTO.copy(pendingQueueSyncLocalId = linkId))
                        )
                    )
                }
            }) {
            linksDao.updateLinkNote(linkId, newNote)
        }
    }

    override suspend fun updateLinkTitle(
        linkId: Long, newTitle: String,
        viaSocket: Boolean
    ): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(linkId)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(),
            remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.renameALinkTitle(remoteId, newTitle)
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                if (remoteId != null) {
                    val linkDTO =
                        linksDao.getLink(linkId).copy(title = newTitle).asLinkDTO(remoteId)
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.UPDATE_LINK_TITLE.name,
                            payload = Json.encodeToString(linkDTO.copy(pendingQueueSyncLocalId = linkId))
                        )
                    )
                }
            }) {
            linksDao.updateLinkTitle(linkId, newTitle)
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
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = viaSocket.not(), remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.updateLink(link.asLinkDTO(id = remoteId))
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                if (remoteId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.UPDATE_LINK.name,
                            payload = Json.encodeToString(
                                link.asLinkDTO(id = remoteId)
                                    .copy(pendingQueueSyncLocalId = link.localId)
                            )
                        )
                    )
                }
            }) {
            linksDao.updateALink(link)
        }
    }

    override suspend fun refreshLinkMetadata(link: Link): Flow<Result<Unit>> {
        val remoteId = getRemoteIdOfLink(link.localId)
        return performLocalOperationWithRemoteSyncFlow(
            performRemoteOperation = true, remoteOperation = {
                if (remoteId != null) {
                    remoteLinksRepo.updateLink(
                        linksDao.getLink(link.localId).asLinkDTO(id = remoteId)
                    )
                } else {
                    emptyFlow()
                }
            }, onRemoteOperationFailure = {
                if (remoteId != null) {
                    pendingSyncQueueRepo.addInQueue(
                        PendingSyncQueue(
                            operation = RemoteRoute.Link.UPDATE_LINK.name,
                            payload = Json.encodeToString(
                                linksDao.getLink(link.localId).asLinkDTO(id = remoteId)
                                    .copy(pendingQueueSyncLocalId = link.localId)
                            )
                        )
                    )
                }
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
                        mediaType = scrapedLinkInfo.mediaType
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

    override suspend fun getALink(localLinkId: Long): Link {
        return linksDao.getLink(localLinkId)
    }
}