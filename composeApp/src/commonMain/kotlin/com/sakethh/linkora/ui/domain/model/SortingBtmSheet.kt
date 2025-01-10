package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.ui.domain.Sorting

data class SortingBtmSheet(
    val sortingName: String,
    val onClick: () -> Unit,
    val sortingType: Sorting,
)