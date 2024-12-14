package com.sakethh

import androidx.compose.runtime.Composable
import com.sakethh.linkora.Platform

expect val shouldFollowSystemThemeComposableVisible: Boolean
expect val BUILD_FLAVOUR: String

expect val platform: @Composable () -> Platform