package com.sakethh.linkora.worker

import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sakethh.linkora.LinkoraApp
import com.sakethh.linkora.R
import com.sakethh.linkora.RefreshAllLinksNotificationService
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.onFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreenVM
import com.sakethh.linkora.ui.screens.settings.section.data.RefreshLinksState
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

class RefreshAllLinksWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    companion object {
        fun cancelLinksRefreshing() {
            WorkManager.Companion.getInstance(LinkoraApp.Companion.getContext())
                .cancelWorkById(UUID.fromString(AppPreferences.refreshLinksWorkerTag.value))
            DataSettingsScreenVM.Companion.refreshLinksState.value = RefreshLinksState(
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
        return try {
            DependencyContainer.localLinksRepo.value.getAllLinks().let { allLinks ->
                DataSettingsScreenVM.Companion.refreshLinksState.value =
                    DataSettingsScreenVM.Companion.refreshLinksState.value.copy(isInRefreshingState = true)
                DataSettingsScreenVM.Companion.totalLinksForRefresh.value = allLinks.size
                val lastRefreshedIndex =
                    DependencyContainer.preferencesRepo.value.readPreferenceValue(
                        longPreferencesKey(AppPreferenceType.LAST_REFRESHED_LINK_INDEX.name)
                    )
                val startIndex = (lastRefreshedIndex?.plus(1) ?: 0).toInt()
                if (allLinks.lastIndex < startIndex) return Result.success()
                allLinks.subList(
                    fromIndex = startIndex, toIndex = allLinks.size
                ).forEachIndexed { index, link ->
                    DependencyContainer.localLinksRepo.value.refreshLinkMetadata(link)
                        .collectLatest {
                            it.onSuccess {
                                DataSettingsScreenVM.Companion.refreshLinksState.value =
                                    DataSettingsScreenVM.Companion.refreshLinksState.value.copy(
                                        currentIteration = index + 1
                                    )
                                DependencyContainer.preferencesRepo.value.changePreferenceValue(
                                    preferenceKey = longPreferencesKey(AppPreferenceType.LAST_REFRESHED_LINK_INDEX.name),
                                    newValue = index.toLong()
                                )
                                refreshAllLinksNotificationService.showNotification()
                            }.onFailure {
                                linkoraLog(it)
                            }
                        }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            DataSettingsScreenVM.Companion.refreshLinksState.value =
                DataSettingsScreenVM.Companion.refreshLinksState.value.copy(
                    isInRefreshingState = false, currentIteration = 0
                )
            refreshAllLinksNotificationService.clearNotifications()
        }
    }
}