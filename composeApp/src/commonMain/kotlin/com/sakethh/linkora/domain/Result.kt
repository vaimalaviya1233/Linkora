package com.sakethh.linkora.domain

sealed interface Result<T> {
    data class Success<T>(val data: T) : Result<T>
    data class Loading<T>(val message: String = "Loading...") : Result<T>
    data class Failure<T>(val message: String) : Result<T>
}

suspend fun <T> Result<T>.onSuccess(init: suspend (T) -> Unit): Result<T> {
    if (this is Result.Success) {
        init(this.data)
    }
    return this
}

suspend fun <T> Result<T>.onFailure(init: suspend (failureMessage: String) -> Unit): Result<T> {
    if (this is Result.Failure) {
        init(this.message)
    }
    return this
}

suspend fun <T> Result<T>.onLoading(init: suspend () -> Unit): Result<T> {
    if (this is Result.Loading) {
        init()
    }
    return this
}