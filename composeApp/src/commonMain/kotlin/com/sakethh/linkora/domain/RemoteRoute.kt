package com.sakethh.linkora.domain

sealed interface RemoteRoute {
    enum class Folder : RemoteRoute {
        CREATE_FOLDER,
        DELETE_FOLDER,
        GET_CHILD_FOLDERS,
        GET_ROOT_FOLDERS,
        MARK_FOLDER_AS_ARCHIVE,
        MARK_AS_REGULAR_FOLDER,
        CHANGE_PARENT_FOLDER,
        UPDATE_FOLDER_NAME,
        UPDATE_FOLDER_NOTE,
        DELETE_FOLDER_NOTE,
        MARK_FOLDERS_AS_ROOT
    }

    enum class Panel : RemoteRoute {
        ADD_A_NEW_PANEL,
        ADD_A_NEW_FOLDER_IN_A_PANEL,
        DELETE_A_PANEL,
        UPDATE_A_PANEL_NAME,
        DELETE_A_FOLDER_FROM_ALL_PANELS,
        DELETE_A_FOLDER_FROM_A_PANEL,
    }

    enum class Link : RemoteRoute {
        UPDATE_LINK_TITLE, UPDATE_LINK_NOTE,
        CREATE_A_NEW_LINK, DELETE_A_LINK, UPDATE_LINKED_FOLDER_ID,
        UPDATE_USER_AGENT, GET_LINKS_FROM_A_FOLDER, GET_LINKS, ARCHIVE_LINK,
        UNARCHIVE_LINK, MARK_AS_IMP, UNMARK_AS_IMP, UPDATE_LINK,DELETE_DUPLICATE_LINKS
    }

    enum class SyncInLocalRoute {
        TEST_BEARER,
        GET_UPDATES,
        GET_TOMBSTONES,
        DELETE_EVERYTHING
    }
}