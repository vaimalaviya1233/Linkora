package com.sakethh.linkora.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Folder(
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val isArchived: Boolean = false
) {
    class FolderAlreadyExistsException(message: String) : Exception(message)
}
