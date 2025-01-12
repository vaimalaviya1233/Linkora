package com.sakethh

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.sakethh.linkora.Platform
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.ui.screens.settings.section.data.ExportType

expect val showFollowSystemThemeOption: Boolean
expect val showDynamicThemingOption: Boolean
expect val BUILD_FLAVOUR: String

expect val platform: @Composable () -> Platform

expect val localDatabase: LocalDatabase?

expect val poppinsFontFamily: FontFamily

expect fun writeRawExportStringToFile(
    exportType: ExportType,
    rawExportString: RawExportString,
    onCompletion: () -> Unit
)
