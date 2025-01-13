package com.sakethh.linkora.domain.repository

import com.sakethh.linkora.domain.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

interface ImportDataRepo {
    suspend fun importDataFromAJSONFile(file: File): Flow<Result<Unit>>

    suspend fun importDataFromAHTMLFile(file: File): Flow<Result<Unit>>
}