package com.sakethh.linkora.domain

import com.sakethh.linkora.common.utils.catchAsThrowableAndEmitFailure
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

fun Flow<List<Link>>.mapToSuccessAndCatch(): Flow<Result<List<Link>>> {
    return this.map {
        Result.Success(it)
    }.catchAsThrowableAndEmitFailure()
}