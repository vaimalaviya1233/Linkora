package com.sakethh.linkora.ui.utils

import com.sakethh.platformSpecificLogging

fun linkoraLog(value: Any?) {
    platformSpecificLogging(value.toString())
}