package com.sakethh.linkora.common.utils

object Constants {
    const val LOCALIZATION_SERVER_URL = "https://linkoralocalizationserver.onrender.com/"

    const val ALL_LINKS_ID: Long = -1
    const val SAVED_LINKS_ID: Long = -2
    const val IMPORTANT_LINKS_ID: Long = -3
    const val ARCHIVE_ID: Long = -4

    const val DEFAULT_APP_LANGUAGE_CODE = "en"
    const val DEFAULT_APP_LANGUAGE_NAME = "English"
}

object LinkType {
    const val SAVED_LINK = "SAVED_LINK"
    const val FOLDER_LINK = "FOLDER_LINK"
    const val HISTORY_LINK = "HISTORY_LINK"
    const val IMPORTANT_LINK = "IMPORTANT_LINK"
    const val ARCHIVE_LINK = "ARCHIVE_LINK"
}