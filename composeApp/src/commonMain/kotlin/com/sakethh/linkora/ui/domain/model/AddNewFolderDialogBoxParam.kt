package com.sakethh.linkora.ui.domain.model

import androidx.compose.runtime.MutableState
import com.sakethh.linkora.domain.model.Folder

data class AddNewFolderDialogBoxParam(
    val shouldBeVisible: MutableState<Boolean>,
    val onCreated: () -> Unit = {},
    val inAChildFolderScreen: Boolean,
    val onFolderCreateClick: (folderName: String, folderNote: String) -> Unit,
    val thisFolder: Folder?
)
