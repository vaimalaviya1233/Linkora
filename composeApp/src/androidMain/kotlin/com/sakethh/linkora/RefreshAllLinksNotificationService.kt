package com.sakethh.linkora

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreenVM

class RefreshAllLinksNotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private val cancelRefreshingIntent = Intent(context, CancelRefreshingActionReceiver::class.java)
    private val cancelRefreshingPendingIntent = PendingIntent.getBroadcast(
        context,
        0,
        cancelRefreshingIntent,
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

    fun showNotification() {
        val notification = NotificationCompat.Builder(context, "1")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setContentTitle(Localization.Key.RefreshingLinks.getLocalizedString())
            .setContentText(
                Localization.Key.NoOfLinksRefreshed.getLocalizedString()
                    .replace(
                        LinkoraPlaceHolder.First.value,
                        DataSettingsScreenVM.refreshLinksState.value.currentIteration.toString()
                    ).replace(
                        LinkoraPlaceHolder.Second.value,
                        DataSettingsScreenVM.totalLinksForRefresh.value.toString()
                    )
            )
            .setProgress(
                DataSettingsScreenVM.totalLinksForRefresh.value,
                DataSettingsScreenVM.refreshLinksState.value.currentIteration,
                false
            )
            .addAction(
                R.drawable.ic_launcher_background,
                Localization.Key.Cancel.getLocalizedString(),
                cancelRefreshingPendingIntent
            )
            .build()

        notificationManager.notify(1, notification)
    }
}

class CancelRefreshingActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        RefreshAllLinksWorker.cancelLinksRefreshing()
    }
}