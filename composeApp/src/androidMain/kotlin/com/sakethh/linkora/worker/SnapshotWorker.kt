package com.sakethh.linkora.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.domain.ExportFileType
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.writeRawExportStringToFile

class SnapshotWorker(appContext: Context, workerParameters: WorkerParameters) :
    CoroutineWorker(appContext, workerParameters) {

    override suspend fun doWork(): Result {
        return try {
            val rawExportStringID = inputData.getLong(key = "rawExportStringID", defaultValue = 0)
            val rawExportString =
                DependencyContainer.snapshotRepo.value.getASnapshot(rawExportStringID)
            val fileType = inputData.getString(key = "fileType")!!
            writeRawExportStringToFile(
                exportFileType = ExportFileType.valueOf(fileType),
                rawExportString = rawExportString.content,
                onCompletion = {
                    try {
                        DependencyContainer.snapshotRepo.value.deleteASnapshot(rawExportStringID)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                    linkoraLog("Snapshot saved as: $it")
                })
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}