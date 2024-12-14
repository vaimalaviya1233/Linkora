package com.sakethh.linkora.ui.domain.model

import androidx.compose.runtime.MutableState
import com.sakethh.linkora.domain.model.Folder

data class FolderComponentParam(
    val folder: Folder,
    val onClick: () -> Unit,
    val onLongClick: () -> Unit,
    val onMoreIconClick: () -> Unit,
    val isCurrentlyInDetailsView: MutableState<Boolean>,
    val showMoreIcon: MutableState<Boolean>
)
