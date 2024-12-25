package com.sakethh.linkora.common.network

import com.sakethh.linkora.domain.model.WebSocketEvent
import com.sakethh.linkora.domain.repository.local.LocalFoldersRepo
import com.sakethh.linkora.ui.utils.linkoraLog
import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.DefaultClientWebSocketSession
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.pingInterval
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.serialization.json.Json
import kotlin.time.Duration.Companion.seconds

object Network {
    val client = HttpClient(CIO) {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    encodeDefaults = true
                })
        }
    }
}