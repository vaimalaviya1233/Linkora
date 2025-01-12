package com.sakethh.linkora.data.local.repository

import com.sakethh.linkora.common.utils.baseUrl
import com.sakethh.linkora.common.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.isATwitterUrl
import com.sakethh.linkora.common.utils.isAValidLink
import com.sakethh.linkora.common.utils.isNotNullOrNotBlank
import com.sakethh.linkora.common.utils.wrappedResultFlow
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.twitter.TwitterMetaDataDTO
import com.sakethh.linkora.domain.mapToResultFlow
import com.sakethh.linkora.domain.model.ScrapedLinkInfo
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.withContext
import org.jsoup.Jsoup

class LocalLinksRepoImpl(
    private val linksDao: LinksDao,
    private val primaryUserAgent: () -> String,
    private val httpClient: HttpClient
) : LocalLinksRepo {
    override suspend fun addANewLink(
        link: Link, linkSaveConfig: LinkSaveConfig
    ): Flow<Result<Unit>> {
        return flow {
            emit(Result.Loading())
            if (linkSaveConfig.forceSaveWithoutRetrievingData) {
                link.url.isAValidLink().ifNot {
                    throw Link.Invalid()
                }
                linksDao.addANewLink(link)
                emit(Result.Success(Unit))
                return@flow
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
                        imgURL = scrapedLinkInfo.imgUrl
                    )
                )
            }
            emit(Result.Success(Unit))
        }.catchAsThrowableAndEmitFailure()
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
                ?: vxTwitterResponseBody.userPfp)
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
        return wrappedResultFlow {
            linksDao.deleteALinkNote(linkId)
        }
    }

    override suspend fun deleteALink(linkId: Long): Flow<Result<Unit>> {
        return wrappedResultFlow {
            linksDao.deleteALink(linkId)
        }
    }

    override suspend fun archiveALink(linkId: Long): Flow<Result<Unit>> {
        return wrappedResultFlow {
            linksDao.archiveALink(linkId)
        }
    }

    override suspend fun updateLinkNote(linkId: Long, newNote: String): Flow<Result<Unit>> {
        return wrappedResultFlow {
            linksDao.updateLinkNote(linkId, newNote)
        }
    }

    override suspend fun updateLinkTitle(linkId: Long, newTitle: String): Flow<Result<Unit>> {
        return wrappedResultFlow {
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
}