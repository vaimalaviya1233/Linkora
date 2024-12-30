package com.sakethh.linkora.ui.components.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MenuBtmSheetVM : ViewModel() {

    val importantOptionIcon = mutableStateOf(Icons.Outlined.Favorite)
    val importantOptionText = mutableStateOf("")

    val archiveOptionIcon = mutableStateOf(Icons.Outlined.Archive)
    val archiveOptionText = mutableStateOf("")

    suspend fun updateImportantCardData(url: String) {
        if (false) {
            importantOptionIcon.value = Icons.Outlined.DeleteForever
            importantOptionText.value = "LocalizedStrings.removeFromImportantLinks.value"
        } else {
            importantOptionIcon.value = Icons.Outlined.StarOutline
            importantOptionText.value = "LocalizedStrings.addToImportantLinks.value"
        }
    }

    fun updateArchiveLinkCardData(url: String) {
        if (false) {
            archiveOptionIcon.value = Icons.Outlined.Unarchive
            archiveOptionText.value = "LocalizedStrings.removeFromArchive.value"
        } else {
            archiveOptionIcon.value = Icons.Outlined.Archive
            archiveOptionText.value = "LocalizedStrings.moveToArchive.value"
        }
    }

    fun updateArchiveFolderCardData(folderID: Long) {
        if (false) {
            archiveOptionIcon.value = Icons.Outlined.Unarchive
            archiveOptionText.value = "LocalizedStrings.removeFromArchive.value"
        } else {
            archiveOptionIcon.value = Icons.Outlined.Archive
            archiveOptionText.value = "LocalizedStrings.moveToArchive.value"
        }
    }
}