package com.sakethh.linkora.domain

sealed interface Platform {
    sealed interface Android : Platform {
        data object Mobile : Android
        data object Tablet : Android
    }

    data object Desktop : Platform
}
