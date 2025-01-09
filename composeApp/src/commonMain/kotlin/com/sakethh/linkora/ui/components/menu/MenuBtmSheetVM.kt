package com.sakethh.linkora.ui.components.menu

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Favorite
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material.icons.outlined.Unarchive
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.pushSnackbarOnFailure
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.runBlocking
import kotlin.properties.Delegates

class MenuBtmSheetVM(private val localLinksRepo: LocalLinksRepo) : ViewModel() {

    companion object {
        val importantOptionIcon = mutableStateOf(Icons.Outlined.Favorite)
        val importantOptionText = mutableStateOf("")

        val archiveOptionIcon = mutableStateOf(Icons.Outlined.Archive)
        val archiveOptionText = mutableStateOf("")
    }

    fun updateImpLinkInfo(linkUrl: String) = runBlocking {
        localLinksRepo.markedAsImportant(linkUrl).collectLatest {
            it.onSuccess {
                if (it.data) {
                    importantOptionIcon.value = Icons.Outlined.DeleteForever
                    importantOptionText.value =
                        Localization.Key.RemoveALinkFromImpLink.getLocalizedString()
                } else {
                    importantOptionIcon.value = Icons.Outlined.StarOutline
                    importantOptionText.value =
                        Localization.Key.MarkALinkAsImpLink.getLocalizedString()
                }
            }.pushSnackbarOnFailure()
        }
    }

    fun shouldShowArchiveOption(url: String): Boolean {
        var isArchived by Delegates.notNull<Boolean>()
        runBlocking {
            localLinksRepo.isInArchive(url).collectLatest {
                it.onSuccess {
                    isArchived = it.data
                }.pushSnackbarOnFailure()
            }
        }
        return isArchived.not()
    }

    fun updateArchiveLinkInfo(url: String) = runBlocking {
        localLinksRepo.isInArchive(url).collectLatest {
            it.onSuccess {
                if (it.data) {
                    archiveOptionIcon.value = Icons.Outlined.Unarchive
                    archiveOptionText.value = Localization.Key.UnArchive.getLocalizedString()
                } else {
                    archiveOptionIcon.value = Icons.Outlined.Archive
                    archiveOptionText.value = Localization.Key.Archive.getLocalizedString()
                }
            }.pushSnackbarOnFailure()
        }
    }

    fun updateArchiveFolderCardData(isFolderArchived: Boolean) {
        if (isFolderArchived) {
            archiveOptionIcon.value = Icons.Outlined.Unarchive
            archiveOptionText.value = Localization.Key.UnArchive.getLocalizedString()
        } else {
            archiveOptionIcon.value = Icons.Outlined.Archive
            archiveOptionText.value = Localization.Key.Archive.getLocalizedString()
        }
    }
}