package com.sakethh.linkora.ui.domain

import androidx.compose.runtime.Stable
import com.sakethh.linkora.ui.PageKey

@Stable
data class PaginationState<T>(
    val isRetrieving: Boolean,
    val errorOccurred: Boolean,
    val errorMessage: String?,
    val pagesCompleted: Boolean,
    val data: T,
)
