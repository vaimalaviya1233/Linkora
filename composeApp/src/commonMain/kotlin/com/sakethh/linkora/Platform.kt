package com.sakethh.linkora

sealed interface Platform {
    sealed interface Android : Platform {
        data object Mobile : Android
        data object Tablet : Android
    }

    data object Desktop : Platform
}
