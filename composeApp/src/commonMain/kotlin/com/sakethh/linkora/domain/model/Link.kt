package com.sakethh.linkora.domain.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sakethh.linkora.domain.LinkType

@Entity
data class Link(
    val linkType: LinkType,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 1,
    val linkTitle: String,
    val webURL: String,
    val baseURL: String,
    val imgURL: String,
    val infoForSaving: String,
    val lastModified: String,
    val isLinkedWithSavedLinks: Boolean,
    val isLinkedWithFolders: Boolean,
    val idOfLinkedFolder: Long?,
    val userAgent: String?,
)