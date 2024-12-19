package com.sakethh.linkora.common.utils

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

fun String?.ifNullOrBlank(string: () -> String): String {
    return if (this.isNullOrBlank()) {
        string()
    } else {
        this
    }
}

fun String.baseUrl(): String {
    return this.split("/")[2]
}

fun Modifier.fillMaxWidthWithPadding(
    paddingValues: PaddingValues = PaddingValues(
        start = 15.dp,
        end = 15.dp
    )
): Modifier {
    return this.fillMaxWidth().padding(paddingValues)
}