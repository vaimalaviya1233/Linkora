package com.sakethh.linkora.core.preferences

import androidx.compose.runtime.mutableStateOf

object AppPreferences {
    val shouldDarkThemeBeEnabled = mutableStateOf(true)
    val shouldFollowSystemTheme = mutableStateOf(false)
    val shouldFollowAmoledTheme = mutableStateOf(false)
    val shouldFollowDynamicTheming = mutableStateOf(false)
}