package com.sakethh

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.ui.screens.settings.section.data.ExportType
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

actual fun writeRawExportStringToFile(
    exportType: ExportType, rawExportString: RawExportString, onCompletion: () -> Unit
) {
    val currentDir = System.getProperty("user.dir")
    val exportsFolder = File(currentDir, "Exports")

    exportsFolder.exists().ifNot {
        exportsFolder.mkdir()
    }

    val exportFileName = "LinkoraExport-${
        DateFormat.getDateTimeInstance().format(Date()).replace(":", "").replace(" ", "")
    }.${if (exportType == ExportType.HTML) "html" else "json"}"

    val exportFilePath = Paths.get(exportsFolder.absolutePath, exportFileName)

    Files.write(exportFilePath, rawExportString.toByteArray())
    onCompletion()
}