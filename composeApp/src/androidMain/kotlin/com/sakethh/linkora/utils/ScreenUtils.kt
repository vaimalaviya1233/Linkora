package com.sakethh.linkora.utils

import android.content.res.Configuration

fun isTablet(localConfiguration: Configuration): Boolean {
    return if (localConfiguration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
        localConfiguration.screenWidthDp > 840
    } else {
        localConfiguration.screenWidthDp > 600
    }
}