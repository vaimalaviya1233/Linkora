package com.sakethh.linkora.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.sakethh.FileManager
import com.sakethh.NativeUtils
import com.sakethh.PermissionManager
import com.sakethh.linkora.data.local.LocalDatabase

object LinkoraSDKProvider {
    private lateinit var shared: LinkoraSDK
    private var assigned = false

    fun getInstance(): LinkoraSDK {
        require(assigned) {
            "LinkoraSDK has not been set. Call LinkoraSDKProvider.set() first."
        }
        return shared
    }

    fun set(linkoraSdk: LinkoraSDK) {
        require(!assigned) {
            "LinkoraSDK has already been set and can only be set once."
        }

        shared = linkoraSdk
        assigned = true
    }
}

class LinkoraSDK(
    val nativeUtils: NativeUtils,
    val fileManager: FileManager,
    val permissionManager: PermissionManager,
    val localDatabase: LocalDatabase,
    val dataStore: DataStore<Preferences>,
    val dataSyncingNotificationService: NativeUtils.DataSyncingNotificationService
)