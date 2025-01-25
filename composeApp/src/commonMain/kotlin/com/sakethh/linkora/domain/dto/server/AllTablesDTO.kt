package com.sakethh.linkora.domain.dto.server

import com.sakethh.linkora.domain.dto.server.folder.FolderDTO
import com.sakethh.linkora.domain.dto.server.link.LinkDTO
import com.sakethh.linkora.domain.dto.server.panel.PanelDTO
import com.sakethh.linkora.domain.dto.server.panel.PanelFolderDTO
import kotlinx.serialization.Serializable

@Serializable
data class AllTablesDTO(
    val links: List<LinkDTO>,
    val folders: List<FolderDTO>,
    val panels: List<PanelDTO>,
    val panelFolders: List<PanelFolderDTO>
)