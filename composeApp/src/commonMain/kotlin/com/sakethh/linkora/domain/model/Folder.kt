package com.sakethh.linkora.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
@Serializable
data class Folder(
    val name: String,
    val note: String,
    val parentFolderId: Long?,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val isArchived: Boolean = false
) {
    class FolderAlreadyExists(message: String) : Throwable(message)
    class InvalidName(message: String) : Throwable(message)
}
