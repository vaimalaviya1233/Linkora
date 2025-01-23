package com.sakethh.linkora.common.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.server.Correlation
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.ui.utils.rememberDeserializableObject
import com.sakethh.platform
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch

fun String?.ifNullOrBlank(string: () -> String): String {
    return if (this.isNullOrBlank()) {
        string()
    } else {
        this
    }
}

fun String.baseUrl(throwOnException: Boolean = true): String {
    return try {
        this.split("/")[2]
    } catch (e: Exception) {
        if (throwOnException) {
            throw e
        }
        this
    }
}

fun Modifier.fillMaxWidthWithPadding(
    paddingValues: PaddingValues = PaddingValues(
        start = 15.dp,
        end = 15.dp
    )
): Modifier {
    return this.fillMaxWidth().padding(paddingValues)
}

@Composable
fun Modifier.bottomNavPaddingAcrossPlatforms(): Modifier {
    return if (platform() is Platform.Android) {
        this.navigationBarsPadding()
    } else {
        this.padding(bottom = 10.dp)
    }
}

fun Any?.isNotNull(): Boolean {
    return this != null
}

fun Any?.isNull(): Boolean {
    return this == null
}

fun String?.isNotNullOrNotBlank(): Boolean {
    return !this.isNullOrBlank()
}

fun String.isAValidLink(): Boolean {
    return try {
        this.baseUrl()
        true
    } catch (e: Exception) {
        false
    }
}

fun Boolean.ifNot(init: () -> Unit): Boolean {
    if (!this) {
        init()
    }
    return this
}

fun Boolean.ifTrue(init: () -> Unit): Boolean {
    if (this) {
        init()
    }
    return this
}

fun Localization.Key.getLocalizedString(): String {
    return Localization.getLocalizedString(this)
}

@Composable
fun Localization.Key.rememberLocalizedString(): String {
    return Localization.rememberLocalizedString(this)
}

suspend fun <T> Result<T>.pushSnackbarOnFailure() {
    if (this is Result.Failure) {
        pushUIEvent(UIEvent.Type.ShowSnackbar(this.message))
    }
}

fun Exception?.pushSnackbar(coroutineScope: CoroutineScope) {
    if (this.isNotNull()) {
        coroutineScope.pushUIEvent(UIEvent.Type.ShowSnackbar(this?.message.toString()))
    }
}

fun Throwable?.pushSnackbar(coroutineScope: CoroutineScope) {
    if (this.isNotNull()) {
        coroutineScope.pushUIEvent(UIEvent.Type.ShowSnackbar(this?.message.toString()))
    }
}

fun <T> Flow<Result<T>>.catchAsThrowableAndEmitFailure(): Flow<Result<T>> {
    return this.catch {
        it.printStackTrace()
        emit(Result.Failure(message = it.message.toString()))
    }
}

fun <T> Flow<Result<T>>.catchAsExceptionAndEmitFailure(): Flow<Result<T>> {
    return this.catch {
        try {
            it as Exception
            it.printStackTrace()
            emit(Result.Failure(message = it.message.toString()))
        } catch (e: Exception) {
            e.printStackTrace()
            it.printStackTrace()
            emit(Result.Failure(message = it.message.toString()))
        }
    }
}

fun String.replaceFirstPlaceHolderWith(string: String): String {
    return this.replace(LinkoraPlaceHolder.First.value, string.inDoubleQuotes())
}

fun String.isATwitterUrl(): Boolean {
    return this.trim().startsWith("http://twitter.com/") or this.trim()
        .startsWith("https://twitter.com/") or this.trim().startsWith(
        "http://x.com/"
    ) or this.trim().startsWith("https://x.com/")
}

suspend fun <T : Any> T.then(init: suspend () -> Unit): T {
    init()
    return this
}

suspend fun <T> T?.ifNotNull(init: suspend (T) -> Unit): T? {
    if (this.isNotNull()) {
        init(this!!)
    }
    return this
}

@Composable
fun NavHostController.inRootScreen(includeSettingsScreen: Boolean): Boolean? {
    val rootRoutesList = rememberDeserializableObject {
        listOf(
            Navigation.Root.HomeScreen,
            Navigation.Root.SearchScreen,
            Navigation.Root.CollectionsScreen,
            Navigation.Root.SettingsScreen,
        )
    }
    return this.currentBackStackEntryAsState().value?.destination?.let { destination ->
        rootRoutesList.filter {
            includeSettingsScreen || it != Navigation.Root.SettingsScreen
        }.any {
            destination.hasRoute(it::class)
        }
    }
}

fun String.inDoubleQuotes(): String = "\"$this\""

fun Link.excludeLocalId(): Link = Link(
    linkType = this.linkType,
    title = this.title,
    url = this.url,
    imgURL = this.imgURL,
    note = this.note,
    idOfLinkedFolder = this.idOfLinkedFolder,
    lastModified = this.lastModified,
    userAgent = this.userAgent,
    markedAsImportant = this.markedAsImportant
)

suspend inline fun <reified IncomingBody> HttpResponse.handleResponseBody(): Result<IncomingBody> {
    return if (this.status.isSuccess().not()) {
        Result.Failure(this.status.value.toString() + " " + this.status.description)
    } else {
        Result.Success(this.body<IncomingBody>())
    }
}

fun String.asWebSocketUrl(): String = "ws://" + this.substringAfter("://")

fun Correlation.isSameAsCurrentClientID(): Boolean = this == AppPreferences.correlation