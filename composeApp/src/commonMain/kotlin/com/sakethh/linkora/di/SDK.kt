package com.sakethh.linkora.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.sakethh.FileManager
import com.sakethh.NativeUtils
import com.sakethh.PermissionManager
import com.sakethh.linkora.data.local.LocalDatabase

object SharedSDK {
    private lateinit var shared: SDK
    private var created = false

    fun getInstance(): SDK = shared

    fun create(sdk: SDK) {
        if (created) return

        shared = sdk
        created = true
    }
}

class SDK(
    val nativeUtils: NativeUtils,
    val fileManager: FileManager,
    val permissionManager: PermissionManager,
    val localDatabase: LocalDatabase,
    val dataStore: DataStore<Preferences>,
    val dataSyncingNotificationService: NativeUtils.DataSyncingNotificationService
)