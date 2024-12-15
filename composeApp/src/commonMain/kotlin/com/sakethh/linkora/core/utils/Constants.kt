package com.sakethh.linkora.core.utils

object Constants {
    const val LOCALIZATION_SERVER_URL = "https://linkoralocalizationserver.onrender.com/"

    const val ALL_LINKS_ID: Long = -1
    const val SAVED_LINKS_ID: Long = -2
    const val IMPORTANT_LINKS_ID: Long = -3
    const val ARCHIVE_ID: Long = -4

    const val VALUE_PLACE_HOLDER_1 = "{#PLACE_HOLDER_1#}"
    const val VALUE_PLACE_HOLDER_2 = "{#PLACE_HOLDER_2#}"

    fun placeholders(): List<String> = listOf(VALUE_PLACE_HOLDER_1, VALUE_PLACE_HOLDER_2)
}