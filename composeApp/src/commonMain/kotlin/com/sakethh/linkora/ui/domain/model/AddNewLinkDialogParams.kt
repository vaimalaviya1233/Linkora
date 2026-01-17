package com.sakethh.linkora.ui.domain.model

import androidx.compose.runtime.Stable
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.tag.Tag
import com.sakethh.linkora.ui.PageKey
import com.sakethh.linkora.ui.domain.AddANewLinkDialogBoxAction
import com.sakethh.linkora.ui.domain.PaginationState
import kotlinx.coroutines.flow.StateFlow

@Stable
data class AddNewLinkDialogParams(
   val onDismiss: () -> Unit,
   val currentFolder: Folder?,
   val allTags: StateFlow<PaginationState<Map<PageKey, List<Tag>>>>,
   val selectedTags: List<Tag>,
   val foldersSearchQuery: String,
   val foldersSearchQueryResult: StateFlow<List<Folder>>,
   val rootRegularFolders: StateFlow<PaginationState<Map<PageKey, List<Folder>>>>,
   val performAction: (AddANewLinkDialogBoxAction) -> Unit,
   val url: String = "",
)
