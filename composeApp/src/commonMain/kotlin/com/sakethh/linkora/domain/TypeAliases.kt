package com.sakethh.linkora.domain

import androidx.compose.runtime.Composable
import com.sakethh.linkora.domain.dto.server.ArchiveMultipleItemsDTO
import com.sakethh.linkora.domain.dto.server.MoveItemsDTO

typealias ComposableContent = @Composable () -> Unit

typealias RawExportString = String

typealias ImportFileType = FileType

typealias ExportFileType = FileType

typealias CopyItemsDTO = MoveItemsDTO

typealias DeleteMultipleItemsDTO = ArchiveMultipleItemsDTO