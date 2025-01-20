package com.sakethh.linkora.common.utils

import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.Result
import com.sakethh.linkora.domain.model.Folder
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> wrappedResultFlow(init: suspend () -> T): Flow<Result<T>> {
    return flow {
        emit(Result.Loading())
        init().let {
            emit(Result.Success(it))
        }
    }.catchAsExceptionAndEmitFailure()
}

fun forceSaveWithoutRetrieving(): LinkSaveConfig {
    return LinkSaveConfig(forceAutoDetectTitle = false, forceSaveWithoutRetrievingData = true)
}

fun defaultSavedLinksFolder(): Folder = Folder(
    name = Localization.Key.SavedLinks.getLocalizedString(),
    note = "",
    parentFolderId = null,
    localId = Constants.SAVED_LINKS_ID,
    remoteId = null,
    isArchived = false
)

fun defaultImpLinksFolder(): Folder = Folder(
    name = Localization.Key.ImportantLinks.getLocalizedString(),
    note = "",
    parentFolderId = null,
    localId = Constants.IMPORTANT_LINKS_ID,
    remoteId = null,
    isArchived = false
)