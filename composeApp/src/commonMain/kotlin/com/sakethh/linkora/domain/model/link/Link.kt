package com.sakethh.linkora.domain.model.link

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.baseUrl
import com.sakethh.linkora.domain.LinkType

@Entity(tableName = "links")
data class Link(
    val linkType: LinkType,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val baseURL: String = url.baseUrl(throwOnException = false),
    val imgURL: String,
    val note: String,
    val lastModified: String = "",
    val idOfLinkedFolder: Long?,
    val userAgent: String?,
) {
    class Invalid(message: String = Localization.getLocalizedString(Localization.Key.InvalidLink)) :
        Throwable(message)
}