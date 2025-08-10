package com.sakethh.linkora

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.room.Room
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.sakethh.FileManager
import com.sakethh.NativeUtils
import com.sakethh.PermissionManager
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.di.SDK
import com.sakethh.linkora.di.SharedSDK
import kotlinx.coroutines.Dispatchers
import okio.Path.Companion.toPath

class LinkoraApp : Application() {

    override fun onCreate() {
        super.onCreate()
        SharedSDK.create(sdk = SDK(
            nativeUtils = NativeUtils(applicationContext),
            fileManager = FileManager(applicationContext),
            permissionManager = PermissionManager(applicationContext),
            localDatabase = run {
                val dbFile = applicationContext.getDatabasePath(LocalDatabase.NAME)
                Room.databaseBuilder(
                    applicationContext,
                    LocalDatabase::class.java,
                    name = dbFile.absolutePath
                ).setDriver(AndroidSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).addMigrations(
                    LocalDatabase.MIGRATION_1_2,
                    LocalDatabase.MIGRATION_2_3,
                    LocalDatabase.MIGRATION_3_4,
                    LocalDatabase.MIGRATION_4_5,
                    LocalDatabase.MIGRATION_5_6,
                    LocalDatabase.MIGRATION_6_7,
                    LocalDatabase.MIGRATION_7_8,
                    LocalDatabase.MIGRATION_8_9,
                    LocalDatabase.MIGRATION_9_10,
                    LocalDatabase.MIGRATION_10_11
                ).build()
            },
            dataStore = PreferenceDataStoreFactory.createWithPath(
                produceFile = {
                    applicationContext.filesDir.resolve(Constants.DATA_STORE_NAME).absolutePath.toPath()
                }), dataSyncingNotificationService = NativeUtils.DataSyncingNotificationService(applicationContext)
        ))
        AppPreferences.readAll(defaultExportLocation = SharedSDK.getInstance().fileManager.getDefaultExportLocation(),preferencesRepository = DependencyContainer.preferencesRepo)
        Localization.loadLocalizedStrings(
            AppPreferences.preferredAppLanguageCode.value
        )
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                "1", "Data Syncing", NotificationManager.IMPORTANCE_HIGH
            )
            notificationChannel.description =
                "Used to notify about the data syncing status, including link refresh."
            val notificationManager =
                getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}