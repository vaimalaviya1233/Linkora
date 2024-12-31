package com.sakethh

import android.content.res.Configuration
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import com.sakethh.linkora.LinkoraApp
import com.sakethh.linkora.Platform
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.ui.theme.poppinsFontFamily
import com.sakethh.linkora.utils.isTablet

actual val showFollowSystemThemeOption: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
actual val platform: @Composable () -> Platform = {
    if (isTablet(LocalConfiguration.current) || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.Android.Tablet else Platform.Android.Mobile
}
actual val BUILD_FLAVOUR: String = platform.toString()
actual val localDatabase: LocalDatabase? = LinkoraApp.getLocalDb()

actual val poppinsFontFamily: FontFamily = poppinsFontFamily
actual val showDynamicThemingOption: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S