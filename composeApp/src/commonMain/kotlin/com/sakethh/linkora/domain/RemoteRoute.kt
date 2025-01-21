package com.sakethh.linkora.domain

sealed interface RemoteRoute {
    enum class Folder {
        CREATE_FOLDER,
        DELETE_FOLDER,
        GET_CHILD_FOLDERS,
        GET_ROOT_FOLDERS,
        MARK_AS_ARCHIVE,
        MARK_AS_REGULAR_FOLDER,
        CHANGE_PARENT_FOLDER,
        UPDATE_FOLDER_NAME,
        UPDATE_FOLDER_NOTE,
        DELETE_FOLDER_NOTE
    }

    enum class Panel {
        ADD_A_NEW_PANEL,
        ADD_A_NEW_FOLDER_IN_A_PANEL,
        DELETE_A_PANEL,
        UPDATE_A_PANEL_NAME,
        DELETE_A_FOLDER_FROM_ALL_PANELS,
        DELETE_A_FOLDER_FROM_A_PANEL
    }
}