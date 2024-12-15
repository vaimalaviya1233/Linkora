package com.sakethh

import androidx.compose.runtime.Composable
import com.sakethh.linkora.Platform
import com.sakethh.linkora.data.local.LocalDatabase

expect val shouldFollowSystemThemeComposableVisible: Boolean
expect val BUILD_FLAVOUR: String

expect val platform: @Composable () -> Platform

expect val localDatabase: LocalDatabase?