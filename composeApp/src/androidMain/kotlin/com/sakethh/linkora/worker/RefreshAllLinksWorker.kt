package com.sakethh.linkora.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sakethh.linkora.R
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.preferences.AppPreferenceType
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.service.RefreshAllLinksNotificationService
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreenVM
import com.sakethh.linkora.ui.screens.settings.section.data.RefreshLinksState
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

class RefreshAllLinksWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    companion object {
        fun cancelLinksRefreshing(appContext: Context) {
            WorkManager.getInstance(appContext)
                .cancelWorkById(UUID.fromString(AppPreferences.refreshLinksWorkerTag.value))
            DataSettingsScreenVM.refreshLinksState.value = RefreshLinksState(
                isInRefreshingState = false, currentIteration = 0
            )
        }
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        return ForegroundInfo(
            1,
            NotificationCompat.Builder(applicationContext, "1")
                .setSmallIcon(R.drawable.ic_stat_name)
                .build()
        )
    }

    private val refreshAllLinksNotificationService = RefreshAllLinksNotificationService(appContext)

    override suspend fun doWork(): Result {
        if (isStopped) {
            cleanUp()
            return Result.success()
        }
        return try {
            val allLinks = DependencyContainer.localLinksRepo.getAllLinks()
            DataSettingsScreenVM.refreshLinksState.value =
                DataSettingsScreenVM.refreshLinksState.value.copy(isInRefreshingState = true)
            DataSettingsScreenVM.totalLinksForRefresh.value = allLinks.size
            val lastRefreshedIndex =
                DependencyContainer.preferencesRepo.readPreferenceValue(
                    longPreferencesKey(AppPreferenceType.LAST_REFRESHED_LINK_INDEX.name)
                )
            val currStartIndex = (lastRefreshedIndex?.plus(1) ?: 0).toInt()
            if (allLinks.lastIndex < currStartIndex) return Result.success()
            allLinks.subList(
                fromIndex = currStartIndex, toIndex = allLinks.size
            ).forEachIndexed { index, link ->
                if (isStopped) {
                    cleanUp()
                    return Result.success()
                }

                linkoraLog("currStartIndex = $currStartIndex, index = $index")
                DependencyContainer.localLinksRepo.refreshLinkMetadata(link)
                    .cancellable()
                    .collectLatest {
                        it.onSuccess {
                            DataSettingsScreenVM.refreshLinksState.value =
                                DataSettingsScreenVM.refreshLinksState.value.copy(
                                    currentIteration = currStartIndex + index + 1
                                )
                            refreshAllLinksNotificationService.showNotification()
                        }.onFailure {
                            linkoraLog(it)
                        }
                        DependencyContainer.preferencesRepo.changePreferenceValue(
                            preferenceKey = longPreferencesKey(AppPreferenceType.LAST_REFRESHED_LINK_INDEX.name),
                            newValue = (currStartIndex + index).toLong()
                        )
                    }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            cleanUp()
        }
    }

    private fun cleanUp() {
        cancelLinksRefreshing(applicationContext)
        DataSettingsScreenVM.refreshLinksState.value =
            DataSettingsScreenVM.refreshLinksState.value.copy(
                isInRefreshingState = false, currentIteration = 0
            )
        refreshAllLinksNotificationService.clearNotifications()
    }
}