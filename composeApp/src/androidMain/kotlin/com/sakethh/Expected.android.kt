package com.sakethh

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.os.Build
import android.os.Environment
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontFamily
import androidx.core.content.ContextCompat
import com.sakethh.linkora.LinkoraApp
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.domain.ImportFileType
import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.ui.theme.poppinsFontFamily
import com.sakethh.linkora.utils.AndroidUIEvent
import com.sakethh.linkora.utils.isTablet
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.io.File
import java.text.DateFormat
import java.util.Date

actual val showFollowSystemThemeOption: Boolean =
    Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
actual val platform: @Composable () -> Platform = {
    if (isTablet(LocalConfiguration.current) || LocalConfiguration.current.orientation == Configuration.ORIENTATION_LANDSCAPE) Platform.Android.Tablet else Platform.Android.Mobile
}
actual val BUILD_FLAVOUR: String = platform.toString()
actual val localDatabase: LocalDatabase? = LinkoraApp.getLocalDb()

actual val poppinsFontFamily: FontFamily = poppinsFontFamily
actual val showDynamicThemingOption: Boolean = Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
actual suspend fun writeRawExportStringToFile(
    exportFileType: ExportFileType,
    rawExportString: RawExportString,
    onCompletion: () -> Unit
) {
    val defaultFolder = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        File(Environment.getExternalStorageDirectory(), "Linkora/Exports")
    } else {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Linkora/Exports"
        )
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q && defaultFolder.exists().not()) {
        File(Environment.getExternalStorageDirectory(), "Linkora/Exports").mkdirs()
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && defaultFolder.exists().not()) {
        File(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS),
            "Linkora/Exports"
        ).mkdirs()
    }

    val exportFileName = "LinkoraExport-${
        DateFormat.getDateTimeInstance().format(Date()).replace(":", "").replace(" ", "")
    }.${if (exportFileType == ExportFileType.HTML) "html" else "json"}"

    val file = File(defaultFolder, exportFileName)
    file.writeText(rawExportString)
    onCompletion()
}

actual suspend fun isStoragePermissionPermittedOnAndroid(): Boolean {
    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
        if (ContextCompat.checkSelfPermission(
                LinkoraApp.getContext(), android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            true
        } else {
            AndroidUIEvent.pushUIEvent(AndroidUIEvent.Type.ShowRuntimePermissionForStorage)
            false
        }
    } else {
        true
    }

}

actual suspend fun pickAValidFileForImporting(importFileType: ImportFileType): File? {
    AndroidUIEvent.pushUIEvent(
        AndroidUIEvent.Type.ImportAFile(
            fileType = if (importFileType == ImportFileType.JSON) "application/json" else "text/html"
        )
    )
    val deferredFile = CompletableDeferred<File?>()
    CoroutineScope(Dispatchers.IO).launch {
        AndroidUIEvent.androidUIEventChannel.collectLatest {
            if (it is AndroidUIEvent.Type.UriOfTheFileForImporting) {
                try {
                    if (it.uri.isNull()) {
                        throw NullPointerException()
                    }
                    val file = createTempFile()
                    LinkoraApp.getContext().contentResolver.openInputStream(it.uri!!).use { input ->
                        file.outputStream().use { output ->
                            input?.copyTo(output)
                        }
                    }
                    deferredFile.complete(file)
                    this.cancel()
                } catch (e: Exception) {
                    e.printStackTrace()
                    deferredFile.complete(null)
                    this.cancel()
                }
            }
        }
    }
    return deferredFile.await()
}