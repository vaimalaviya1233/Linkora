package com.sakethh.linkora.ui.screens.settings

import androidx.compose.ui.graphics.vector.ImageVector

data class SettingSectionComponentParam(
    val onClick: () -> Unit,
    val sectionTitle: String,
    val sectionIcon: ImageVector,
    val shouldArrowIconAppear: Boolean = true
)
