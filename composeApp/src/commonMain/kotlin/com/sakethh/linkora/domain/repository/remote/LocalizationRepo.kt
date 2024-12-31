package com.sakethh.linkora.domain.repository.remote

import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.dto.localization.LocalizationInfoDTO
import kotlinx.coroutines.flow.Flow

interface LocalizationRepo {
    fun getRemoteLanguages(): Flow<Result<LocalizationInfoDTO>>
}