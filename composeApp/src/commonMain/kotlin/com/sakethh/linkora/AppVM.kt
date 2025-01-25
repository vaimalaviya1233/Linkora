package com.sakethh.linkora

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.pushSnackbar
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.repository.remote.RemoteSyncRepo
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppVM(
    private val remoteSyncRepo: RemoteSyncRepo
) : ViewModel() {
    init {
        readSocketEvents()

        viewModelScope.launch {
            launch {
                remoteSyncRepo.updateDataBasedOnRemoteTombstones(0).collectLatest {
                    it.pushSnackbarOnFailure()
                }
            }
            launch {
                remoteSyncRepo.applyUpdatesFromRemote(0).collectLatest {
                    it.pushSnackbarOnFailure()
                }
            }
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