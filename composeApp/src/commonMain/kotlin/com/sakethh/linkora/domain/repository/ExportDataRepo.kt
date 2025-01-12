package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.RawExportString
import com.sakethh.linkora.domain.Result
import kotlinx.coroutines.flow.Flow

interface ExportDataRepo {

    suspend fun exportDataAsJSON(): Flow<Result<RawExportString>>

    suspend fun exportDataAsHTMl(): Flow<Result<RawExportString>>

}