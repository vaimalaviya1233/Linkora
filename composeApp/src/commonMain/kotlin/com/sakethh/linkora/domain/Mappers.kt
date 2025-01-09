package com.sakethh.linkora.domain

import com.sakethh.linkora.ui.components.menu.MenuBtmSheetType

fun LinkType.asMenuBtmSheetType(): MenuBtmSheetType.Link {
    return when (this) {
        LinkType.SAVED_LINK -> MenuBtmSheetType.Link.SavedLink
        LinkType.FOLDER_LINK -> MenuBtmSheetType.Link.FolderLink
        LinkType.HISTORY_LINK -> MenuBtmSheetType.Link.HistoryLink
        LinkType.IMPORTANT_LINK -> MenuBtmSheetType.Link.ImportantLink
        LinkType.ARCHIVE_LINK -> MenuBtmSheetType.Link.ArchiveLink
    }
}