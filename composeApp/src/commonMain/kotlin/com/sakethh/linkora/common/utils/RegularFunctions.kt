package com.sakethh.linkora.common.utils

import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> wrappedResultFlow(init: suspend () -> T): Flow<Result<T>> {
    return flow {
        emit(Result.Loading())
        init().let {
            emit(Result.Success(it))
        }
    }.catchAsExceptionAndEmitFailure()
}

fun forceSaveWithoutRetrieving(): LinkSaveConfig {
    return LinkSaveConfig(forceAutoDetectTitle = false, forceSaveWithoutRetrievingData = true)
}

fun defaultSavedLinksFolder(): Folder = Folder(
    name = Localization.Key.SavedLinks.getLocalizedString(),
    note = "",
    parentFolderId = null,
    localId = Constants.SAVED_LINKS_ID,
    remoteId = null,
    isArchived = false
)

fun defaultImpLinksFolder(): Folder = Folder(
    name = Localization.Key.ImportantLinks.getLocalizedString(),
    note = "",
    parentFolderId = null,
    localId = Constants.IMPORTANT_LINKS_ID,
    remoteId = null,
    isArchived = false
)

inline fun <reified OutgoingBody, reified IncomingBody> postFlow(
    httpClient: HttpClient,
    crossinline baseUrl: () -> String,
    crossinline authToken: () -> String,
    endPoint: String,
    body: OutgoingBody,
    contentType: ContentType = ContentType.Application.Json
): Flow<Result<IncomingBody>> {
    return flow {
        emit(Result.Loading())
        httpClient.post(baseUrl() + endPoint) {
            bearerAuth(authToken())
            contentType(contentType)
            setBody(body)
        }.handleResponseBody<IncomingBody>().run {
            emit(this)
        }
    }.catchAsExceptionAndEmitFailure()
}