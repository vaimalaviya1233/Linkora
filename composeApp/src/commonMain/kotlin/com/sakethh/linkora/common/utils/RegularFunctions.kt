package com.sakethh.linkora.common.utils

import com.sakethh.linkora.domain.Result
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun unitFlowResult(init: suspend () -> Unit): Flow<Result<Unit>> {
    return flow {
        emit(Result.Loading())
        init()
        emit(Result.Success(Unit))
    }.catchAsExceptionAndEmitFailure()
}