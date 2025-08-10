package com.sakethh.linkora.platform

import android.content.Context
import android.provider.OpenableColumns
import androidx.core.net.toUri
import androidx.documentfile.provider.DocumentFile
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.sakethh.linkora.utils.pushSnackbar
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.model.Snapshot
import com.sakethh.linkora.ui.screens.settings.section.data.ExportLocationType
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.utils.AndroidUIEvent
import com.sakethh.linkora.worker.SnapshotWorker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.resume

actual class FileManager(private val context: Context) {
    actual suspend fun writeRawExportStringToFile(
        exportLocation: String,
        exportFileType: ExportFileType,
        exportLocationType: ExportLocationType,
        rawExportString: RawExportString,
        onCompletion: suspend (String) -> Unit
    ) {

        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.US)
        val timestamp = simpleDateFormat.format(Date())
        val exportFileName =
            "${if (exportLocationType == ExportLocationType.EXPORT) "LinkoraExport" else "LinkoraSnapshot"}-$timestamp.${if (exportFileType == ExportFileType.HTML) "html" else "json"}"

        val directoryUri = exportLocation.toUri()
        val directory = DocumentFile.fromTreeUri(context, directoryUri)
        val newFile = directory?.createFile(
            if (exportFileType == ExportFileType.HTML) "text/html" else "application/json",
            exportFileName
        )
        newFile?.uri?.let { fileUri ->
            try {
                context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                    outputStream.write(rawExportString.toByteArray())
                }
                onCompletion(exportFileName)
            } catch (e: Exception) {
                withContext(coroutineContext) {
                    pushUIEvent(UIEvent.Type.ShowSnackbar(e.message.toString()))
                }
                e.printStackTrace()
            }
        }
    }

    actual suspend fun pickAValidFileForImporting(
        importFileType: ImportFileType,
        onStart: () -> Unit
    ): File? {

        AndroidUIEvent.pushUIEvent(
            AndroidUIEvent.Type.ImportAFile(
                fileType = when (importFileType) {
                    ImportFileType.JSON -> "application/json"
                    ImportFileType.HTML -> "text/html"
                    else -> "*/*"
                }
            )
        )
        return suspendCancellableCoroutine { continuation ->
            val listenerJob = CoroutineScope(continuation.context).launch {
                try {
                    val uriEvent =
                        AndroidUIEvent.androidUIEventChannel.first() as AndroidUIEvent.Type.UriOfTheFileForImporting
                    if (uriEvent.uri == null) {
                        // if picking the file didn't go as expected, then just return null
                        // we can throw and catch and then resume with null but this is aight
                        continuation.resume(null)
                        return@launch
                    }
                    onStart()
                    val fileName = context.contentResolver.query(
                        uriEvent.uri, null, null, null, null
                    )?.use {
                        val nameColumnIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        it.moveToFirst()
                        it.getString(nameColumnIndex)
                    } ?: ""
                    val file = createTempFile(
                        prefix = fileName.substringBeforeLast("."),
                        suffix = fileName.substringAfterLast(".")
                    )
                    context.contentResolver.openInputStream(uriEvent.uri).use { input ->
                        file.outputStream().use { output ->
                            input?.copyTo(output)
                        }
                    }
                    continuation.resume(file)
                } catch (e: Exception) {
                    e.printStackTrace()
                    continuation.cancel()
                }
            }
            continuation.invokeOnCancellation {
                listenerJob.cancel()
            }
        }
    }

    actual suspend fun saveSyncServerCertificateInternally(
        file: File,
        onCompletion: () -> Unit
    ) {
        file.inputStream().use { inputStream ->
            context.filesDir.resolve("sync-server-cert.cer").outputStream().use {
                inputStream.copyTo(it)
            }
        }
        onCompletion()
    }

    actual suspend fun loadSyncServerCertificate(): File {
        return context.filesDir.resolve("sync-server-cert.cer")
    }

    actual suspend fun exportSnapshotData(
        exportLocation: String,
        rawExportString: String,
        fileType: ExportFileType,
        onCompletion: suspend (String) -> Unit
    ) {
        val snapshotWorker = OneTimeWorkRequestBuilder<SnapshotWorker>()
        val rawExportStringID: Long =
            DependencyContainer.snapshotRepo.addASnapshot(Snapshot(content = rawExportString))

        val parameters = Data.Builder().putLong(key = "rawExportStringID", value = rawExportStringID)
            .putString(key = "fileType", value = fileType.name).build()
        snapshotWorker.setInputData(parameters)
        WorkManager.getInstance(context).enqueue(snapshotWorker.build())
    }

    actual suspend fun pickADirectory(): String? {
        AndroidUIEvent.pushUIEvent(AndroidUIEvent.Type.PickADirectory)
        return suspendCancellableCoroutine { continuation ->
            val listenerJob = CoroutineScope(continuation.context).launch {
                val eventDirectoryPick =
                    AndroidUIEvent.androidUIEventChannel.first() as AndroidUIEvent.Type.PickedDirectory
                try {
                    continuation.resume(eventDirectoryPick.uri?.toString())
                } catch (e: Exception) {
                    e.printStackTrace()
                    continuation.cancel()
                }
            }
            continuation.invokeOnCancellation {
                listenerJob.cancel()
            }
        }
    }

    actual fun getDefaultExportLocation(): String? {
        return null
    }

    actual suspend fun deleteAutoBackups(
        backupLocation: String,
        threshold: Int,
        onCompletion: (Int) -> Unit
    ) {
        try {
            withContext(Dispatchers.IO) {
                DocumentFile.fromTreeUri(context, backupLocation.toUri())?.listFiles()
                    ?.filter {
                        it.name?.startsWith("LinkoraSnapshot-") == true
                    }?.let { snapshots ->
                        val snapshotsCount = snapshots.count()
                        if (snapshotsCount > threshold) {
                            snapshots.sortedBy {
                                it.lastModified()
                            }.take(snapshotsCount - threshold).apply {
                                forEach {
                                    it.delete()
                                }
                                onCompletion(count())
                            }
                        } else {
                            onCompletion(0)
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            e.pushSnackbar()
        }
    }
}