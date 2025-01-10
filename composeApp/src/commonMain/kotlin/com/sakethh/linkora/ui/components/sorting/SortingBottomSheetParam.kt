package com.sakethh.linkora.ui.components.sorting

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SheetState
import androidx.compose.runtime.MutableState
import com.sakethh.linkora.ui.domain.SortingBtmSheetType
import com.sakethh.linkora.ui.domain.SortingType

data class SortingBottomSheetParam @OptIn(ExperimentalMaterial3Api::class) constructor(
    val shouldBottomSheetBeVisible: MutableState<Boolean>,
    val onSelected: (
        selectedSortingTypeType: SortingType,
        isLinksSortingSelected: Boolean, isFoldersSortingSelected: Boolean
    ) -> Unit,
    val bottomModalSheetState: SheetState,
    val sortingBtmSheetType: SortingBtmSheetType,
    val shouldFoldersSelectionBeVisible: MutableState<Boolean>,
    val shouldLinksSelectionBeVisible: MutableState<Boolean>
)