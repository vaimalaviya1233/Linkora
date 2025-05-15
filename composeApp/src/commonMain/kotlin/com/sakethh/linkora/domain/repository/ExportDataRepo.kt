package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.Result
import kotlinx.coroutines.flow.Flow

interface ExportDataRepo {

    /**
     * This function is supposed to export data as JSON from the local database.
     */
    suspend fun rawExportDataAsJSON(): Flow<Result<RawExportString>>

    /**
     * This function is supposed to export data as HTML from the local database.
     */
    suspend fun rawExportDataAsHTML(): Flow<Result<RawExportString>>
}