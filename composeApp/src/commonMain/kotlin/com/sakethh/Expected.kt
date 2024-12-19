package com.sakethh

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.sakethh.linkora.Platform
import com.sakethh.linkora.data.local.LocalDatabase

expect val shouldShowFollowSystemThemeOption: Boolean
expect val BUILD_FLAVOUR: String

expect val platform: @Composable () -> Platform

expect val localDatabase: LocalDatabase?

expect val poppinsFontFamily: FontFamily