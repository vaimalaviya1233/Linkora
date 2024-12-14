package com.sakethh.linkora.ui.domain.model

import androidx.compose.runtime.MutableState

data class LinkPref(
    val title: String,
    val onClick: () -> Unit,
    val isSwitchChecked: MutableState<Boolean>
)