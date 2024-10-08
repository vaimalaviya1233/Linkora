package com.sakethh.linkora.ui.transferActions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.data.local.ImportantLinks
import com.sakethh.linkora.data.local.LinksTable
import com.sakethh.linkora.data.local.folders.FoldersRepo
import com.sakethh.linkora.data.local.links.LinksRepo
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.screens.collections.specific.SpecificScreenType
import com.sakethh.linkora.utils.linkoraLog
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransferActionsBtmBarVM @Inject constructor(
    private val foldersRepo: FoldersRepo,
    private val linksRepo: LinksRepo
) : ViewModel() {

    fun transferFolders(
        applyCopyImpl: Boolean, sourceFolderIds: List<Long>, targetParentId: Long?
    ) {
        viewModelScope.launch {
            if (applyCopyImpl) {
                sourceFolderIds.forEach { originalFolderId ->

                    foldersRepo.getChildFoldersOfThisParentIDAsList(originalFolderId)
                        .map { it.folderName }.toString().let {
                        linkoraLog(it)
                    }

                    foldersRepo.duplicateAFolder(originalFolderId, targetParentId)

                    val newlyDuplicatedFolderId = foldersRepo.getLastIDOfFoldersTable()
                    linksRepo.duplicateFolderBasedLinks(
                        currentIdOfLinkedFolder = originalFolderId,
                        newIdOfLinkedFolder = newlyDuplicatedFolderId
                    )

                    foldersRepo.getChildFoldersOfThisParentIDAsList(originalFolderId).map { it.id }
                        .forEach {
                            transferFolders(
                                applyCopyImpl = true,
                                sourceFolderIds = listOf(it),
                                targetParentId = newlyDuplicatedFolderId
                            )
                        }
                }
            } else {
                foldersRepo.changeTheParentIdOfASpecificFolder(sourceFolderIds, targetParentId)
            }
        }.invokeOnCompletion {
            if (TransferActionsBtmBarValues.sourceLinks.isEmpty()) {
                TransferActionsBtmBarValues.reset()
            }
        }
    }

    fun transferLinks(
        applyCopyImpl: Boolean,
        sourceLinks: List<LinksTable>,
        targetFolder: SpecificScreenType
    ) {
        linkoraLog("in")
        viewModelScope.launch {
            linkoraLog("also in")
            linkoraLog(sourceLinks.size.toString())
            sourceLinks.forEach { currentLink ->

                linkoraLog(currentLink.title)

                when (targetFolder) {
                SpecificScreenType.IMPORTANT_LINKS_SCREEN -> {
                    if (currentLink.isLinkedWithImpFolder) return@forEach

                        linksRepo.addANewLinkToImpLinks(
                            ImportantLinks(
                                title = currentLink.title,
                                webURL = currentLink.webURL,
                                baseURL = currentLink.baseURL,
                                imgURL = currentLink.imgURL,
                                infoForSaving = currentLink.infoForSaving
                            )
                        )

                    if (applyCopyImpl) {
                        return@forEach
                    }
                        linksRepo.deleteALinkFromLinksTable(currentLink.id)
                }

                SpecificScreenType.SAVED_LINKS_SCREEN -> {
                    if (currentLink.isLinkedWithSavedLinks && !applyCopyImpl) return@forEach

                    if (currentLink.isLinkedWithFolders) {
                        if (applyCopyImpl) {
                            linksRepo.addALinkInLinksTable(
                                LinksTable(
                                    title = currentLink.title,
                                    webURL = currentLink.webURL,
                                    baseURL = currentLink.baseURL,
                                    imgURL = currentLink.imgURL,
                                    infoForSaving = currentLink.imgURL,
                                    isLinkedWithSavedLinks = true,
                                    isLinkedWithFolders = false,
                                    isLinkedWithImpFolder = false,
                                    keyOfImpLinkedFolder = "",
                                    isLinkedWithArchivedFolder = false
                                )
                            )
                        } else {
                            linksRepo.markThisLinkFromLinksTableAsSavedLink(linkID = currentLink.id)
                        }
                        } else {

                            // if link is located in `Important Links`

                            linksRepo.addALinkInLinksTable(
                                LinksTable(
                                    title = currentLink.title,
                                    webURL = currentLink.webURL,
                                    baseURL = currentLink.baseURL,
                                    imgURL = currentLink.imgURL,
                                    infoForSaving = currentLink.infoForSaving,
                                    isLinkedWithSavedLinks = true,
                                    isLinkedWithFolders = false,
                                    isLinkedWithImpFolder = false,
                                    keyOfImpLinkedFolder = "",
                                    isLinkedWithArchivedFolder = false
                                )
                            )

                        if (applyCopyImpl) {
                            return@forEach
                        }
                            linksRepo.deleteALinkFromImpLinks(currentLink.id)
                        }
                }

                else /* else = SpecificScreenType.SPECIFIC_FOLDER_LINKS_SCREEN */ -> {
                    if (currentLink.isLinkedWithFolders && !applyCopyImpl && currentLink.keyOfLinkedFolderV10 == CollectionsScreenVM.currentClickedFolderData.value.id) return@forEach

                    if (currentLink.isLinkedWithSavedLinks || currentLink.isLinkedWithFolders) {
                        if (applyCopyImpl) {
                            linksRepo.addALinkInLinksTable(
                                LinksTable(
                                    title = currentLink.title,
                                    webURL = currentLink.webURL,
                                    baseURL = currentLink.baseURL,
                                    imgURL = currentLink.imgURL,
                                    infoForSaving = currentLink.imgURL,
                                    isLinkedWithSavedLinks = false,
                                    isLinkedWithFolders = true,
                                    isLinkedWithImpFolder = false,
                                    keyOfImpLinkedFolder = "",
                                    isLinkedWithArchivedFolder = false,
                                    keyOfLinkedFolderV10 = CollectionsScreenVM.currentClickedFolderData.value.id
                                )
                            )
                        } else {
                            linksRepo.markThisLinkFromLinksTableAsFolderLink(
                                linkID = currentLink.id,
                                targetFolderId = CollectionsScreenVM.currentClickedFolderData.value.id
                            )
                        }

                    } else {

                        // if link is originally located in `Important Links`

                            linksRepo.addALinkInLinksTable(
                                LinksTable(
                                    title = currentLink.title,
                                    webURL = currentLink.webURL,
                                    baseURL = currentLink.baseURL,
                                    imgURL = currentLink.imgURL,
                                    infoForSaving = currentLink.infoForSaving,
                                    isLinkedWithSavedLinks = false,
                                    isLinkedWithFolders = true,
                                    isLinkedWithImpFolder = false,
                                    keyOfImpLinkedFolder = "",
                                    isLinkedWithArchivedFolder = false,
                                    keyOfLinkedFolderV10 = CollectionsScreenVM.currentClickedFolderData.value.id
                                )
                            )

                        if (applyCopyImpl) return@forEach

                        linksRepo.deleteALinkFromImpLinks(currentLink.id)
                        }
                }
            }
        }
        }.invokeOnCompletion {
            TransferActionsBtmBarValues.reset()
        }
    }
}