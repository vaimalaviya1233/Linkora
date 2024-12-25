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
}