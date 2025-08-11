package com.sakethh.linkora.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.di.LinkoraSDK
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.ui.screens.settings.section.data.ExportLocationType
import com.sakethh.linkora.ui.utils.linkoraLog

class SnapshotWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            val rawExportStringID = inputData.getLong(key = "rawExportStringID", defaultValue = 0)
            val rawExportString =
                DependencyContainer.snapshotRepo.getASnapshot(rawExportStringID)
            val fileType = inputData.getString(key = "fileType")!!
            LinkoraSDK.getInstance().fileManager.writeRawExportStringToFile(
                exportLocation = AppPreferences.currentBackupLocation.value,
                exportFileType = ExportFileType.valueOf(fileType),
                rawExportString = rawExportString.content,
                onCompletion = {
                    try {
                        DependencyContainer.snapshotRepo.deleteASnapshot(rawExportStringID)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    linkoraLog("Snapshot saved as: $it")
                },
                exportLocationType = ExportLocationType.SNAPSHOT
            )
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}