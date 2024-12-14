package com.sakethh

import androidx.compose.runtime.Composable
import com.sakethh.linkora.Platform

actual val shouldFollowSystemThemeComposableVisible: Boolean = true
actual val BUILD_FLAVOUR: String = "desktop"
actual val platform: @Composable () -> Platform = {
    Platform.Desktop
}