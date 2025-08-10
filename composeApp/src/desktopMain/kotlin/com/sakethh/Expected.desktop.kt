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
import com.sakethh.linkora.domain.PermissionStatus
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.repository.local.LocalLinksRepo
import com.sakethh.linkora.domain.repository.local.PreferencesRepository
import com.sakethh.linkora.linkoraSpecificFolder
import com.sakethh.linkora.ui.screens.settings.section.data.ExportLocationType
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

actual val poppinsFontFamily: FontFamily = com.sakethh.linkora.ui.theme.poppinsFontFamily
actual val showDynamicThemingOption: Boolean = false


@Composable
actual fun PlatformSpecificBackHandler(init: () -> Unit) = Unit



actual fun platformSpecificLogging(string: String) {
    println("Linkora Log : $string")
}

actual class PermissionManager {
    actual suspend fun permittedToShowNotification(): PermissionStatus = PermissionStatus.Granted
    actual suspend fun isStorageAccessPermitted(): PermissionStatus = PermissionStatus.Granted
}

actual class FileManager {

    actual suspend fun writeRawExportStringToFile(
        exportLocation: String,
        exportFileType: ExportFileType,
        exportLocationType: ExportLocationType,
        rawExportString: RawExportString,
        onCompletion: suspend (String) -> Unit
    ) {

        val exportsFolder = File(exportLocation)

        exportsFolder.exists().ifNot {
            exportsFolder.mkdirs()
        }

        // kinda repeated in Expected.android, but alright
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = simpleDateFormat.format(Date())
        val exportFileName =
            "${if (exportLocationType == ExportLocationType.EXPORT) "LinkoraExport" else "LinkoraSnapshot"}-$timestamp.${if (exportFileType == ExportFileType.HTML) "html" else "json"}"

        val exportFilePath = Paths.get(exportsFolder.absolutePath, exportFileName)

        withContext(Dispatchers.IO) {
            Files.write(exportFilePath, rawExportString.toByteArray())
        }
        onCompletion(exportFileName)
        linkoraLog(exportFileName)
    }

    actual suspend fun pickAValidFileForImporting(
        importFileType: ImportFileType,
        onStart: () -> Unit
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

    actual suspend fun saveSyncServerCertificateInternally(
        file: File,
        onCompletion: () -> Unit
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
            onCompletion = onCompletion,
            exportLocationType = ExportLocationType.SNAPSHOT
        )
    }

    actual suspend fun pickADirectory(): String? {
        return "https://music.youtube.com/watch?v=LWUgT34GYhU"
    }

    actual fun getDefaultExportLocation(): String? {
        val userHomeDir = System.getProperty("user.home")
        return File(userHomeDir, "/Documents/Linkora/Exports").absolutePath
    }

    actual suspend fun deleteAutoBackups(
        backupLocation: String,
        threshold: Int,
        onCompletion: (Int) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                File(backupLocation).listFiles {
                    it.nameWithoutExtension.startsWith("LinkoraSnapshot-")
                }?.let { snapshots ->
                    val snapshotsCount = snapshots.count()
                    if (snapshotsCount > threshold) {
                        snapshots.sortBy {
                            it.lastModified()
                        }
                        snapshots.take(snapshotsCount - threshold).apply {
                            forEach {
                                it.delete()
                            }
                            onCompletion(this.count())
                        }
                    } else {
                        onCompletion(0)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            UIEvent.pushUIEvent(UIEvent.Type.ShowSnackbar(e.message.toString()))
        }
    }
}

actual class NativeUtils {

    actual fun onShare(url: String) = Unit

    actual suspend fun onRefreshAllLinks(
        localLinksRepo: LocalLinksRepo,
        preferencesRepository: PreferencesRepository
    ) {
        RefreshAllLinksService.invoke(localLinksRepo)
    }

    actual suspend fun isAnyRefreshingScheduled(): Flow<Boolean?> {
        return emptyFlow()
    }

    actual fun cancelRefreshingLinks() {
        RefreshAllLinksService.cancel()
    }

    actual class DataSyncingNotificationService {
        actual fun showNotification() = Unit
        actual fun clearNotification() = Unit
    }
}