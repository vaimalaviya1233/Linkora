package com.sakethh.linkora.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

object AndroidUIEvent {
    private val _androidUIEventChannel = Channel<Type>()
    val androidUIEventChannel = _androidUIEventChannel.receiveAsFlow()

    fun CoroutineScope.pushUIEvent(type: Type) {
        this.launch {
            _androidUIEventChannel.send(type)
        }
    }

    suspend fun pushUIEvent(type: Type) {
        _androidUIEventChannel.send(type)
    }

    sealed interface Type {
        data object ShowRuntimePermissionForStorage : Type
        data class PermissionGrantedForAndBelowQ(val isGranted: Boolean) : Type
    }
}
