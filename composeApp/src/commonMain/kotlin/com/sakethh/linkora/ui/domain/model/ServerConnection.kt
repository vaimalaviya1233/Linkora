package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.domain.SyncType

data class ServerConnection(
    val serverUrl: String,
    val webSocketScheme: String = AppPreferences.WEB_SOCKET_SCHEME,
    val authToken: String,
    val syncType: SyncType
)
