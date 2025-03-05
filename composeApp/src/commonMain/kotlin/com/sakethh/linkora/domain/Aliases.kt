package com.sakethh.linkora.domain

import androidx.compose.runtime.Composable
import com.sakethh.linkora.domain.dto.server.link.MoveLinksDTO

typealias ComposableContent = @Composable () -> Unit

typealias RawExportString = String

typealias ImportFileType = FileType

typealias ExportFileType = FileType

typealias CopyLinksDTO = MoveLinksDTO
