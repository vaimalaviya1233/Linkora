package com.sakethh

import android.os.Build

actual val shouldFollowSystemThemeComposableVisible: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
actual val BUILD_FLAVOUR: String = ""