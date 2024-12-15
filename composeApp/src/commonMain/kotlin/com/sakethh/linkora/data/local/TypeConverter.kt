package com.sakethh.linkora.data.local

import androidx.room.TypeConverter
import com.sakethh.linkora.domain.LinkType

class TypeConverter {
    @TypeConverter
    fun stringToLinkType(string: String): LinkType = LinkType.valueOf(string)

    @TypeConverter
    fun linkTypeToString(linkType: LinkType): String = linkType.name
}