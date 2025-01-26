package com.sakethh.linkora.domain

import androidx.compose.runtime.Composable
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.catchAsThrowableAndEmitFailure
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.dto.server.folder.AddFolderDTO
import com.sakethh.linkora.domain.dto.server.link.AddLinkDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

fun LinkType.asMenuBtmSheetType(): MenuBtmSheetType.Link {
    return when (this) {
        LinkType.SAVED_LINK -> MenuBtmSheetType.Link.SavedLink
        LinkType.FOLDER_LINK -> MenuBtmSheetType.Link.FolderLink
        LinkType.HISTORY_LINK -> MenuBtmSheetType.Link.HistoryLink
        LinkType.IMPORTANT_LINK -> MenuBtmSheetType.Link.ImportantLink
        LinkType.ARCHIVE_LINK -> MenuBtmSheetType.Link.ArchiveLink
    }
}

fun <T> Flow<T>.mapToResultFlow(): Flow<Result<T>> {
    return this.map {
        Result.Success(it)
    }.catchAsThrowableAndEmitFailure()
}

fun Link.asHistoryLinkWithoutId(): Link {
    return Link(
        linkType = LinkType.HISTORY_LINK,
        title = this.title,
        url = this.url,
        baseURL = this.baseURL,
        imgURL = this.imgURL,
        note = this.note,
        idOfLinkedFolder = null,
        userAgent = this.userAgent,
        markedAsImportant = this.markedAsImportant
    )
}

@Composable
fun LinkType.asLocalizedString(): String {
    return when (this) {
        LinkType.SAVED_LINK -> Localization.Key.SavedLinks.rememberLocalizedString()
        LinkType.FOLDER_LINK -> Localization.Key.FolderLinks.rememberLocalizedString()
        LinkType.HISTORY_LINK -> Localization.Key.HistoryLinks.rememberLocalizedString()
        LinkType.IMPORTANT_LINK -> Localization.Key.ImportantLinks.rememberLocalizedString()
        LinkType.ARCHIVE_LINK -> Localization.Key.ArchiveLinks.rememberLocalizedString()
    }
}

@Composable
fun FolderType.asLocalizedString(): String {
    return when (this) {
        FolderType.REGULAR_FOLDER -> Localization.Key.RegularFolder.rememberLocalizedString()
        FolderType.ARCHIVE_FOLDER -> Localization.Key.ArchiveFolder.rememberLocalizedString()
    }
}

fun Folder.asAddFolderDTO(): AddFolderDTO = AddFolderDTO(
    name = this.name,
    note = this.note,
    parentFolderId = this.parentFolderId,
    isArchived = this.isArchived
)

fun Link.asAddLinkDTO(): AddLinkDTO = AddLinkDTO(
    linkType = this.linkType,
    title = this.title,
    url = this.url,
    baseURL = this.baseURL,
    imgURL = this.imgURL,
    note = this.note,
    lastModified = "",
    idOfLinkedFolder = this.idOfLinkedFolder,
    userAgent = this.userAgent,
    markedAsImportant = this.markedAsImportant,
    mediaType = this.mediaType
)

fun Link.asLinkDTO(id: Long): LinkDTO = LinkDTO(
    linkType = this.linkType,
    title = this.title,
    url = this.url,
    baseURL = this.baseURL,
    imgURL = this.imgURL,
    note = this.note,
    idOfLinkedFolder = this.idOfLinkedFolder,
    userAgent = this.userAgent,
    markedAsImportant = this.markedAsImportant,
    mediaType = this.mediaType,
    id = id
)