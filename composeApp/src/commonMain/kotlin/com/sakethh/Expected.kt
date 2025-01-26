package com.sakethh

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import kotlinx.coroutines.flow.Flow
import java.io.File

expect val showFollowSystemThemeOption: Boolean
expect val showDynamicThemingOption: Boolean
expect val BUILD_FLAVOUR: String

expect val platform: @Composable () -> Platform

expect val localDatabase: LocalDatabase?

expect val poppinsFontFamily: FontFamily

expect suspend fun writeRawExportStringToFile(
    exportFileType: ExportFileType,
    rawExportString: RawExportString,
    onCompletion: () -> Unit
)

expect suspend fun isStorageAccessPermittedOnAndroid(): Boolean

expect suspend fun pickAValidFileForImporting(
    importFileType: ImportFileType,
    onStart: () -> Unit
): File?

expect fun onShare(url: String)

expect suspend fun onRefreshAllLinks(
    localLinksRepo: LocalLinksRepo,
    preferencesRepository: PreferencesRepository
)

expect suspend fun isAnyRefreshingScheduled(): Flow<Boolean?>

expect fun cancelRefreshingLinks()

@Composable
expect fun PlatformSpecificBackHandler(init: () -> Unit)

expect suspend fun permittedToShowNotification(): Boolean

expect fun platformSpecificLogging(string: String)