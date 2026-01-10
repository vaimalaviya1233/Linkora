package com.sakethh.linkora.ui.domain.model

import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.tag.Tag
import kotlinx.serialization.Serializable

@Serializable
data class CollectionDetailPaneInfo(
    val currentFolder: Folder?,
    val currentTag: Tag?,
    val collectionType: CollectionType?,
)

enum class CollectionType {
    FOLDER,
    TAG
}
