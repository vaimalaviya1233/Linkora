package com.sakethh.linkora.ui.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

sealed interface UIEvent {
    data class ShowSnackbar(val message: String) : UIEvent
}

object UiEventManager {
    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val _uiChannel = Channel<UIEvent>()
    val uiEventsReadOnlyChannel = _uiChannel.receiveAsFlow()

    fun pushUIEvent(uiEvent: UIEvent) {
        coroutineScope.launch {
            _uiChannel.send(uiEvent)
        }
    }

}