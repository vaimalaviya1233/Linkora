package com.sakethh.linkora.common.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.platform
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

fun Boolean.ifNot(init: () -> Unit) {
    if (!this) {
        init()
    }
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

fun <T> Flow<Result<T>>.catchAsThrowableAndEmitFailure(): Flow<Result<T>> {
    return this.catch {
        it.printStackTrace()
        emit(Result.Failure(message = it.message.toString()))
    }
}

fun <T> Flow<Result<T>>.catchAsExceptionAndEmitFailure(): Flow<Result<T>> {
    return this.catch {
        it as Exception
        it.printStackTrace()
        emit(Result.Failure(message = it.message.toString()))
    }
}

fun String.replaceFirstPlaceHolderWith(string: String): String {
    return this.replace(LinkoraPlaceHolder.First.value, "\"${string}\"")
}