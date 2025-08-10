package com.sakethh.linkora.domain.model

import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.panel.Panel
import com.sakethh.linkora.domain.model.panel.PanelFolder
import kotlinx.serialization.Serializable

@Serializable
data class JSONExportSchema(
    val schemaVersion: Int,
    val links: List<Link>,
    val folders: List<Folder>,
    val panels: PanelForJSONExportSchema
) {
    companion object {
        const val VERSION = 12
    }
}

@Serializable
data class PanelForJSONExportSchema(
    val panels: List<Panel>, val panelFolders: List<PanelFolder>
)
