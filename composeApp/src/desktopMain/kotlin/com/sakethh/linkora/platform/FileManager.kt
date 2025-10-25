package com.sakethh.linkora.platform

import com.sakethh.linkora.Localization
import com.sakethh.linkora.utils.duplicate
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.utils.ifNot

import com.sakethh.linkora.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.LinkoraPlaceHolder
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.linkoraSpecificFolder
import com.sakethh.linkora.ui.screens.settings.section.data.ExportLocationType
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.linkoraLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.awt.FileDialog
import java.awt.Frame
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
        return if (sourceFile!= null && sourceFile!!.extension == importFileType.name.lowercase()) {
            onStart()
            sourceFile
        } else if (sourceFile!= null && sourceFile!!.extension != importFileType.name.lowercase()) {
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