package com.sakethh.linkora.domain.model.legacy

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
@SerialName("Export")
data class LegacyExportSchema(
    @SerialName("appVersion") val schemaVersion: Int,
    @SerialName("savedLinks") val linksTable: List<LinksTable>,
    @SerialName("importantLinks") val importantLinksTable: List<ImportantLinks>,
    @SerialName("folders") val foldersTable: List<FoldersTable>,
    @SerialName("archivedLinks") val archivedLinksTable: List<ArchivedLinks>,
    @SerialName("archivedFolders") val archivedFoldersTable: List<ArchivedFolders>,
    @SerialName("historyLinks") val historyLinksTable: List<RecentlyVisited>,
    val panels: List<Panel> = emptyList(),
    val panelFolders: List<PanelFolder> = emptyList(),
)