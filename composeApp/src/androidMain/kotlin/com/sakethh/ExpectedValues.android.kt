package com.sakethh

import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import com.sakethh.linkora.LinkoraApp
import com.sakethh.linkora.Platform
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.utils.isTablet

actual val shouldFollowSystemThemeComposableVisible: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
actual val platform: @Composable () -> Platform = {
    if (isTablet(LocalConfiguration.current) || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.Android.Tablet else Platform.Android.Mobile
}
actual val BUILD_FLAVOUR: String = platform.toString()
actual val localDatabase: LocalDatabase? = LinkoraApp.getLocalDb()