package com.sakethh.linkora.common.utils

import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.ui.domain.model.ServerConnection
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow

fun <T> wrappedResultFlow(init: suspend (SendChannel<Result<T>>) -> T): Flow<Result<T>> {
    return channelFlow {
        send(Result.Loading())
        init(this.channel).let {
            send(Result.Success(it))
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

fun <LocalType, RemoteType> performLocalOperationWithRemoteSyncFlow(
    performRemoteOperation: Boolean,
    remoteOperation: suspend () -> Flow<Result<RemoteType>> = { emptyFlow() },
    remoteOperationOnSuccess: suspend (RemoteType) -> Unit = {},
    onRemoteOperationFailure: suspend () -> Unit = {},
    localOperation: suspend () -> LocalType
): Flow<Result<LocalType>> {
    return flow {
        emit(Result.Loading())
        val localResult = localOperation()
        Result.Success(localResult).let { success ->
            if (performRemoteOperation && AppPreferences.canPushToServer()) {
                remoteOperation().collect { remoteResult ->
                    remoteResult.onFailure { failureMessage ->
                        success.isRemoteExecutionSuccessful = false
                        success.remoteFailureMessage = failureMessage
                        onRemoteOperationFailure()
                    }
                    remoteResult.onSuccess {
                        remoteOperationOnSuccess(it.data)
                    }
                }
            }
            emit(success)
        }
    }.catchAsThrowableAndEmitFailure(init = {
        if (performRemoteOperation && AppPreferences.canPushToServer()) {
            onRemoteOperationFailure()
        }
    })
}

fun defaultFolderIds(): List<Long> = listOf(
    Constants.SAVED_LINKS_ID,
    Constants.IMPORTANT_LINKS_ID,
    Constants.ARCHIVE_ID,
    Constants.ALL_LINKS_ID,
    Constants.HISTORY_ID,
    Constants.DEFAULT_PANELS_ID
)

fun initializeIfServerConfigured(init: () -> Unit) {
    if (AppPreferences.isServerConfigured()) {
        init()
    }
}

fun getVideoPlatformBaseUrls(): List<String> = listOf("youtube.com", "youtu.be")

fun currentSavedServerConfig(): ServerConnection {
    return ServerConnection(
        serverUrl = AppPreferences.serverBaseUrl.value,
        authToken = AppPreferences.serverSecurityToken.value,
        syncType = AppPreferences.serverSyncType.value
    )
}