package com.sakethh

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sakethh.linkora.RefreshAllLinksService
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.duplicate
import com.sakethh.linkora.common.utils.getLocalizedString
import com.sakethh.linkora.common.utils.ifNot
import com.sakethh.linkora.common.utils.isNotNull
import com.sakethh.linkora.common.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.withContext
import okio.Path.Companion.toPath
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


actual val showFollowSystemThemeOption: Boolean = true
actual val BUILD_FLAVOUR: String = "desktop"
actual val platform: @Composable () -> Platform = {
    Platform.Desktop
}
private val linkoraSpecificFolder = System.getProperty("user.home").run {
    val appDataDir = File(this, ".linkora")
    if (appDataDir.exists().not()) {
        appDataDir.mkdirs()
    }
    appDataDir
}

actual val localDatabase: LocalDatabase? =
    File(linkoraSpecificFolder, "${LocalDatabase.NAME}.db").run {
        Room.databaseBuilder<LocalDatabase>(name = this.absolutePath)
            .setDriver(BundledSQLiteDriver()).addMigrations(
                LocalDatabase.MIGRATION_9_10, LocalDatabase.MIGRATION_10_11
            ).build()
    }
actual val linkoraDataStore: DataStore<Preferences> = PreferenceDataStoreFactory.createWithPath {
    linkoraSpecificFolder.resolve(Constants.DATA_STORE_NAME).absolutePath.toPath()
}
actual val poppinsFontFamily: FontFamily = com.sakethh.linkora.ui.theme.poppinsFontFamily
actual val showDynamicThemingOption: Boolean = false

actual suspend fun writeRawExportStringToFile(
    exportLocation: String,
    exportFileType: ExportFileType,
    rawExportString: RawExportString,
    onCompletion: suspend (String) -> Unit
) {
    val exportsFolder = File(exportLocation)

    exportsFolder.exists().ifNot {
        exportsFolder.mkdirs()
    }

    val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
    val timestamp = simpleDateFormat.format(Date())
    val exportFileName =
        "LinkoraExport-$timestamp.${if (exportFileType == ExportFileType.HTML) "html" else "json"}"

    val exportFilePath = Paths.get(exportsFolder.absolutePath, exportFileName)

    withContext(Dispatchers.IO) {
        Files.write(exportFilePath, rawExportString.toByteArray())
    }
    onCompletion(exportFileName)
    linkoraLog(exportFileName)
}

actual suspend fun isStorageAccessPermittedOnAndroid(): Boolean = false

actual suspend fun pickAValidFileForImporting(
    importFileType: ImportFileType, onStart: () -> Unit
): File? {
    val fileDialog = FileDialog(
        Frame(),
        Localization.Key.SelectAValidFile.getLocalizedString()
            .replaceFirstPlaceHolderWith(importFileType.name),
        FileDialog.LOAD
    )
    fileDialog.isVisible = true
    val sourceFile = File(fileDialog.directory, fileDialog.file).duplicate()
    return if (sourceFile.isNotNull() && sourceFile!!.extension == importFileType.name.lowercase()) {
        onStart()
        sourceFile
    } else if (sourceFile.isNotNull() && sourceFile!!.extension != importFileType.name.lowercase()) {
        UIEvent.pushUIEvent(
            UIEvent.Type.ShowSnackbar(
                Localization.Key.FileTypeNotSupportedOnDesktopImport.getLocalizedString()
                    .replace(LinkoraPlaceHolder.First.value, sourceFile.extension)
                    .replace(LinkoraPlaceHolder.Second.value, importFileType.name)
            )
        )
        null
    } else null
}

actual fun onShare(url: String) = Unit

actual suspend fun onRefreshAllLinks(
    localLinksRepo: LocalLinksRepo, preferencesRepository: PreferencesRepository
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


actual suspend fun permittedToShowNotification(): Boolean = false

actual fun platformSpecificLogging(string: String) {
    println("Linkora Log : $string")
}


actual class DataSyncingNotificationService actual constructor() {
    actual fun showNotification() = Unit
    actual fun clearNotification() = Unit
}

actual suspend fun exportSnapshotData(
    exportLocation: String,
    rawExportString: String,
    fileType: ExportFileType,
    onCompletion: suspend (String) -> Unit
) {
    writeRawExportStringToFile(
        exportLocation = exportLocation,
        exportFileType = fileType,
        rawExportString = rawExportString,
        onCompletion = onCompletion
    )
}

actual suspend fun saveSyncServerCertificateInternally(
    file: File, onCompletion: () -> Unit
) {
    file.inputStream().use { inputStream ->
        linkoraSpecificFolder.resolve("sync-server-cert.cer").outputStream().use {
            inputStream.copyTo(it)
        }
    }
    onCompletion()
}

actual suspend fun loadSyncServerCertificate(): File {
    return linkoraSpecificFolder.resolve("sync-server-cert.cer")
}

actual suspend fun pickADirectory(): String? {
    return "https://music.youtube.com/watch?v=LWUgT34GYhU"
}

actual fun getDefaultExportLocation(): String? {
    val userHomeDir = System.getProperty("user.home")
    return File(userHomeDir, "/Documents/Linkora/Exports").absolutePath
}