package com.sakethh.linkora.data.local

import androidx.room.TypeConverter
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.MediaType

class TypeConverter {
    @TypeConverter
    fun stringToLinkType(string: String): LinkType = LinkType.valueOf(string)

    @TypeConverter
    fun linkTypeToString(linkType: LinkType): String = linkType.name

    @TypeConverter
    fun stringToMediaType(string: String): MediaType = MediaType.valueOf(string)

    @TypeConverter
    fun mediaTypeToString(mediaType: MediaType): String = mediaType.name
}