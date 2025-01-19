package com.sakethh

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sakethh.linkora.Platform
import com.sakethh.linkora.RefreshAllLinksService
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.isNotNull
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.utils.UIEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.DateFormat
import java.util.Date

actual val showFollowSystemThemeOption: Boolean = true
actual val BUILD_FLAVOUR: String = "desktop"
actual val platform: @Composable () -> Platform = {
    Platform.Desktop
}
actual val localDatabase: LocalDatabase? =
    File(System.getProperty("java.io.tmpdir"), "${LocalDatabase.NAME}.db").run {
        Room.databaseBuilder<LocalDatabase>(name = this.absolutePath).setDriver(BundledSQLiteDriver()).build()
    }

actual val poppinsFontFamily: FontFamily = com.sakethh.linkora.ui.theme.poppinsFontFamily
actual val showDynamicThemingOption: Boolean = false

actual suspend fun writeRawExportStringToFile(
    exportFileType: ExportFileType, rawExportString: RawExportString, onCompletion: () -> Unit
) {
    val currentDir = System.getProperty("user.dir")
    val exportsFolder = File(currentDir, "Exports")

    exportsFolder.exists().ifNot {
        exportsFolder.mkdir()
    }

    val exportFileName = "LinkoraExport-${
        DateFormat.getDateTimeInstance().format(Date()).replace(":", "").replace(" ", "")
    }.${if (exportFileType == ExportFileType.HTML) "html" else "json"}"

    val exportFilePath = Paths.get(exportsFolder.absolutePath, exportFileName)

    withContext(Dispatchers.IO) {
        Files.write(exportFilePath, rawExportString.toByteArray())
    }
    onCompletion()
}

actual suspend fun isStoragePermissionPermittedOnAndroid(): Boolean = false

actual suspend fun pickAValidFileForImporting(importFileType: ImportFileType): File? {
    val fileDialog =
        FileDialog(Frame(), "Select a valid ${importFileType.name} File", FileDialog.LOAD)
    fileDialog.isVisible = true
    val chosenFile: File? = try {
        File(fileDialog.directory, fileDialog.file)
    } catch (e: Exception) {
        null
    }
    return if (chosenFile.isNotNull() && chosenFile!!.extension == importFileType.name.lowercase()) {
        chosenFile
    } else if (chosenFile.isNotNull() && chosenFile!!.extension != importFileType.name.lowercase()) {
        UIEvent.pushUIEvent(UIEvent.Type.ShowSnackbar("${chosenFile.extension} files are not supported for importing, pick valid ${importFileType.name} file."))
        null
    } else null
}

actual fun onShare(url: String) = Unit

actual suspend fun onRefreshAllLinks(
    localLinksRepo: LocalLinksRepo,
    preferencesRepository: PreferencesRepository
) {
    RefreshAllLinksService.invoke(localLinksRepo)
}

actual fun cancelRefreshingLinks() {
    RefreshAllLinksService.cancel()
}

actual suspend fun isAnyRefreshingScheduled(): Flow<Boolean?> {
    return emptyFlow()
}

@Composable
actual fun PlatformSpecificBackHandler(init: () -> Unit) = Unit