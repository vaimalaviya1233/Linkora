package com.sakethh.linkora.common.utils

import com.sakethh.linkora.domain.Result
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