package com.sakethh.linkora.domain.model.link

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.baseUrl
import com.sakethh.linkora.common.utils.isATwitterUrl
import com.sakethh.linkora.domain.LinkType
import kotlinx.serialization.Serializable

@Entity(tableName = "links")
@Serializable
data class Link(
    val linkType: LinkType,
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val url: String,
    val baseURL: String = if (url.isATwitterUrl()) "twitter.com" else url.baseUrl(throwOnException = false),
    val imgURL: String,
    val note: String,
    val lastModified: String = "",
    val idOfLinkedFolder: Long?,
    val userAgent: String? = AppPreferences.primaryJsoupUserAgent.value,
    val markedAsImportant: Boolean = linkType == LinkType.IMPORTANT_LINK
) {
    class Invalid(message: String = Localization.getLocalizedString(Localization.Key.InvalidLink)) :
        Throwable(message)
}