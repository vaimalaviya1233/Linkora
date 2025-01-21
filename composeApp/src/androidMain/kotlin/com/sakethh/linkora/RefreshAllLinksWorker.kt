package com.sakethh.linkora

import android.content.Context
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.work.CoroutineWorker
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.ui.screens.settings.section.data.DataSettingsScreenVM
import com.sakethh.linkora.ui.screens.settings.section.data.RefreshLinksState
import kotlinx.coroutines.flow.collectLatest
import java.util.UUID

class RefreshAllLinksWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    companion object {
        fun cancelLinksRefreshing() {
            WorkManager.getInstance(LinkoraApp.getContext())
                .cancelWorkById(UUID.fromString(AppPreferences.refreshLinksWorkerTag.value))
            DataSettingsScreenVM.refreshLinksState.value = RefreshLinksState(
                isInRefreshingState = false, currentIteration = 0
            )
        }
    }

    private val refreshAllLinksNotificationService = RefreshAllLinksNotificationService(appContext)

    override suspend fun doWork(): Result {
        return try {
            DependencyContainer.localLinksRepo.value.getAllLinks().let { allLinks ->
                DataSettingsScreenVM.refreshLinksState.value =
                    DataSettingsScreenVM.refreshLinksState.value.copy(isInRefreshingState = true)
                DataSettingsScreenVM.totalLinksForRefresh.value = allLinks.size
                val lastRefreshedIndex =
                    DependencyContainer.preferencesRepo.value.readPreferenceValue(
                        longPreferencesKey(AppPreferenceType.LAST_REFRESHED_LINK_INDEX.name)
                    )
                val startIndex = if (lastRefreshedIndex == null) {
                    0
                } else if (lastRefreshedIndex.toInt() == 0) {
                    1
                } else {
                    lastRefreshedIndex + 1
                }.toInt()
                if (allLinks.lastIndex < startIndex) return Result.success()
                allLinks.subList(
                    fromIndex = startIndex, toIndex = allLinks.size - 1
                ).forEachIndexed { index, link ->
                    DependencyContainer.localLinksRepo.value.refreshLinkMetadata(link)
                        .collectLatest {
                            it.onSuccess {
                                DataSettingsScreenVM.refreshLinksState.value =
                                    DataSettingsScreenVM.refreshLinksState.value.copy(
                                        currentIteration = index + 1
                                    )
                                DependencyContainer.preferencesRepo.value.changePreferenceValue(
                                    preferenceKey = longPreferencesKey(AppPreferenceType.LAST_REFRESHED_LINK_INDEX.name),
                                    newValue = index.toLong()
                                )
                                refreshAllLinksNotificationService.showNotification()
                            }
                        }
                }
            }
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        } finally {
            DataSettingsScreenVM.refreshLinksState.value =
                DataSettingsScreenVM.refreshLinksState.value.copy(isInRefreshingState = false)
        }
    }
}