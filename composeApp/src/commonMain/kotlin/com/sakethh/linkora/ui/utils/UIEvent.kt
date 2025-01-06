package com.sakethh.linkora.ui.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

object UIEvent {
    private val _uiChannel = Channel<Type>()
    val uiEventsReadOnlyChannel = _uiChannel.receiveAsFlow()

    suspend fun pushUIEvent(type: Type) {
        _uiChannel.send(type)
    }

    fun CoroutineScope.pushUIEvent(type: Type) {
        this.launch {
            _uiChannel.send(type)
        }
    }

    sealed interface Type {
        data class ShowSnackbar(val message: String) : Type
    }

}