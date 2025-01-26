package com.sakethh.linkora

import androidx.datastore.preferences.core.longPreferencesKey
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.preferences.AppPreferenceType
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.pushSnackbar
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import java.time.Instant

class AppVM(
    private val remoteSyncRepo: RemoteSyncRepo,
    private val preferencesRepository: PreferencesRepository
) : ViewModel() {
    init {
        readSocketEvents()

        viewModelScope.launch {

            launch {
                if (AppPreferences.canPushToServer()) {
                    remoteSyncRepo.pushPendingSyncQueueToServer().collectLatest {
                        it.pushSnackbarOnFailure()
                    }
                }
            }

            listOf(launch {
                if (AppPreferences.canReadFromServer()) {
                    remoteSyncRepo.applyUpdatesBasedOnRemoteTombstones(AppPreferences.lastSyncedLocally())
                        .collectLatest {
                            it.pushSnackbarOnFailure()
                        }
                }
            }, launch {
                if (AppPreferences.canReadFromServer()) {
                    remoteSyncRepo.applyUpdatesFromRemote(AppPreferences.lastSyncedLocally())
                        .collectLatest {
                            it.pushSnackbarOnFailure()
                        }
                }
            }).joinAll()
            preferencesRepository.changePreferenceValue(
                preferenceKey = longPreferencesKey(
                    AppPreferenceType.LAST_TIME_STAMP_SYNCED_WITH_SERVER.name
                ), newValue = Instant.now().epochSecond
            )
        }
    }

    companion object {
        private var socketEventJob: Job? = null

        fun shutdownSocketConnection() {
            socketEventJob?.cancel()
        }
    }

    private fun readSocketEvents() {
        if (AppPreferences.canReadFromServer().not()) return

        socketEventJob = viewModelScope.launch(CoroutineExceptionHandler { _, throwable ->
            throwable.printStackTrace()
            throwable.pushSnackbar(viewModelScope)
        }) {
            remoteSyncRepo.readSocketEvents().collectLatest {
                it.pushSnackbarOnFailure()
            }
        }
    }
}