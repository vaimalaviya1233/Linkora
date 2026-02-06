package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ImportDataRepo {
    suspend fun importDataFromAJSONFile(importFile: File): Flow<Result<Unit>>

    suspend fun importDataFromAHTMLFile(importFile: File): Flow<Result<Unit>>
}