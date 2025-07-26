package com.sakethh.linkora

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.di.DependencyContainer

class LinkoraApp : Application() {

    companion object {
        private lateinit var applicationContext: Context

        fun getContext(): Context = applicationContext
    }

    override fun onCreate() {
        super.onCreate()
        LinkoraApp.applicationContext = this.applicationContext
        AppPreferences.readAll(DependencyContainer.preferencesRepo)
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
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }
}