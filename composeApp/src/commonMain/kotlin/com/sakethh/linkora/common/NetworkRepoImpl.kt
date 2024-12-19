package com.sakethh.linkora.common

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.repository.NetworkRepo
import io.ktor.client.HttpClient
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

class NetworkRepoImpl(private val httpClient: HttpClient) : NetworkRepo {
    override suspend fun testServerConnection(
        serverUrl: String, token: String
    ): Flow<Result<HttpResponse>> {
        return flow {
            emit(Result.Loading())
            val request = httpClient.get(serverUrl) {
                bearerAuth(token)
            }
            if (request.status.isSuccess()) {
                emit(Result.Success(request))
            } else {
                emit(Result.Failure("${request.status.value} ${request.status.description}"))
            }
        }.catch {
            it as Exception
            emit(Result.Failure(it.message.toString()))
        }
    }
}