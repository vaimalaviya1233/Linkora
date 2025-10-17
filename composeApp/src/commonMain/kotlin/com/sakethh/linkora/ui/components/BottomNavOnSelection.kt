package com.sakethh.linkora.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.DriveFileMove
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.CopyAll
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import com.sakethh.linkora.Localization
import com.sakethh.linkora.di.LinkoraSDK
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.ui.AppVM
import com.sakethh.linkora.ui.LocalNavController
import com.sakethh.linkora.ui.LocalPlatform
import com.sakethh.linkora.ui.domain.TransferActionType
import com.sakethh.linkora.ui.navigation.Navigation
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.utils.UIEvent
import com.sakethh.linkora.ui.utils.UIEvent.pushUIEvent
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.bottomNavPaddingAcrossPlatforms
import com.sakethh.linkora.utils.defaultFolderIds
import com.sakethh.linkora.utils.rememberLocalizedString
import com.sakethh.linkora.utils.replaceFirstPlaceHolderWith

@Composable
fun BottomNavOnSelection(
    showLoadingProgressBarOnTransferAction: MutableState<Boolean>,
    appVM: AppVM,
    selectedAndInRoot: MutableState<Boolean>,
    currentRoute: NavDestination?
) {
    val coroutineScope = rememberCoroutineScope()
    val localNavController = LocalNavController.current
    val platform = LocalPlatform.current
    Column(
        modifier = Modifier.fillMaxWidth().animateContentSize()
            .background(NavigationBarDefaults.containerColor).navigationBarsPadding()
    ) {
        HorizontalDivider()
        Spacer(modifier = Modifier.height(5.dp))
        if (showLoadingProgressBarOnTransferAction.value) {
            Text(
                text = if (appVM.transferActionType.value == TransferActionType.COPY) {
                    Localization.Key.Copying.rememberLocalizedString()
                } else {
                    Localization.Key.Moving.rememberLocalizedString()
                },
                style = MaterialTheme.typography.titleMedium,
                fontSize = 14.sp,
                modifier = Modifier.padding(
                    start = 15.dp, bottom = 10.dp, top = 5.dp
                )
            )
            LinearProgressIndicator(
                Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp)
            )
            Spacer(Modifier.bottomNavPaddingAcrossPlatforms())
            return@Column
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {
                CollectionsScreenVM.clearAllSelections()
            }) {
                Icon(
                    imageVector = Icons.Default.Close, contentDescription = null
                )
            }
            Column {
                Text(
                    text = Localization.Key.SelectedLinksCount.rememberLocalizedString()
                        .replaceFirstPlaceHolderWith(CollectionsScreenVM.selectedLinksViaLongClick.size.toString()),
                    style = MaterialTheme.typography.titleSmall
                )
                Text(
                    text = Localization.Key.SelectedFoldersCount.rememberLocalizedString()
                        .replaceFirstPlaceHolderWith(CollectionsScreenVM.selectedFoldersViaLongClick.size.toString()),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        val currentFolder = appVM.currentContextOfFAB.value.currentFolder
        val showPasteButton =
            (appVM.transferActionType.value != TransferActionType.NONE && !selectedAndInRoot.value||  currentFolder != null) && currentFolder?.localId != Constants.ALL_LINKS_ID
        if (!(CollectionsScreenVM.selectedFoldersViaLongClick.isNotEmpty() && currentFolder?.localId in defaultFolderIds().dropWhile {
                it == Constants.ARCHIVE_ID
            })) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                if ((appVM.transferActionType.value == TransferActionType.NONE && !showPasteButton) || (appVM.transferActionType.value != TransferActionType.NONE && showPasteButton)) {
                    Text(
                        text = Localization.Key.MultiActionsLabel.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(start = 15.dp)
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.animateContentSize()
                ) {
                    if (showPasteButton) {
                        IconButton(onClick = {

                            if (appVM.transferActionType.value == TransferActionType.COPY) {
                                appVM.copySelectedItems(
                                    folderId = 0/*CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId!!*/,
                                    onStart = {
                                        showLoadingProgressBarOnTransferAction.value = true
                                    },
                                    onCompletion = {
                                        showLoadingProgressBarOnTransferAction.value = false
                                    })
                            } else {
                                appVM.moveSelectedItems(
                                    folderId = 0/*CollectionsScreenVM.collectionDetailPaneInfo.value.currentFolder?.localId!!*/,
                                    onStart = {
                                        showLoadingProgressBarOnTransferAction.value = true
                                    },
                                    onCompletion = {
                                        showLoadingProgressBarOnTransferAction.value = false
                                    })
                            }
                        }, modifier = Modifier.padding(end = 6.5.dp)) {
                            Icon(
                                imageVector = Icons.Default.ContentPaste, contentDescription = null
                            )
                        }
                        return@Row
                    }
                    if (appVM.transferActionType.value != TransferActionType.NONE) {
                        return@Row
                    }
                    IconButton(onClick = {
                        coroutineScope.pushUIEvent(UIEvent.Type.ShowDeleteDialogBox)
                    }) {
                        Icon(
                            imageVector = Icons.Default.Delete, contentDescription = null
                        )
                    }
                    if (CollectionsScreenVM.selectedLinksViaLongClick.any {
                            it.linkType == LinkType.ARCHIVE_LINK
                        }.not() || CollectionsScreenVM.selectedFoldersViaLongClick.any {
                            it.isArchived.not()
                        }) {
                        IconButton(onClick = {
                            appVM.archiveSelectedItems(onStart = {
                                showLoadingProgressBarOnTransferAction.value = true
                            }, onCompletion = {
                                showLoadingProgressBarOnTransferAction.value = false
                            })
                        }) {
                            Icon(
                                imageVector = Icons.Default.Archive, contentDescription = null
                            )
                        }
                    }
                    if (CollectionsScreenVM.selectedFoldersViaLongClick.any {
                            it.isArchived
                        } || CollectionsScreenVM.selectedLinksViaLongClick.any {
                            it.linkType == LinkType.ARCHIVE_LINK
                        }) {
                        IconButton(onClick = {
                            appVM.markSelectedItemsAsRegular(onStart = {
                                showLoadingProgressBarOnTransferAction.value = true
                            }, onCompletion = {
                                showLoadingProgressBarOnTransferAction.value = false
                            })
                        }) {
                            Icon(
                                imageVector = Icons.Default.Unarchive, contentDescription = null
                            )
                        }
                    }
                    IconButton(onClick = {
                        appVM.transferActionType.value = TransferActionType.COPY
                    }) {
                        Icon(
                            imageVector = Icons.Default.CopyAll, contentDescription = null
                        )
                    }
                    IconButton(onClick = {
                        appVM.transferActionType.value = TransferActionType.MOVE
                    }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.DriveFileMove,
                            contentDescription = null
                        )
                    }
                    if (platform is Platform.Android) {
                        Spacer(
                            modifier = Modifier.height(20.dp).width(2.dp).background(
                                MaterialTheme.colorScheme.outline
                            )
                        )
                        IconButton(onClick = {
                            LinkoraSDK.getInstance().nativeUtils.onShare(
                                CollectionsScreenVM.selectedLinksViaLongClick.joinToString(
                                    "\n"
                                ) { it.url })
                        }) {
                            Icon(
                                imageVector = Icons.Default.Share, contentDescription = null
                            )
                        }
                    }
                }
            }
        }
        if (appVM.transferActionType.value != TransferActionType.NONE) {
            Text(
                text = if (appVM.transferActionType.value == TransferActionType.COPY) Localization.Key.NavigateAndCopyDesc.rememberLocalizedString() else Localization.Key.NavigateAndMoveDesc.rememberLocalizedString(),
                style = MaterialTheme.typography.titleSmall,
                modifier = Modifier.padding(start = 15.dp, end = 15.dp)
            )
        }
        val showNavigateToCollectionScreen =
            selectedAndInRoot.value && currentRoute?.hasRoute(Navigation.Root.CollectionsScreen::class) != true
        if (CollectionsScreenVM.selectedFoldersViaLongClick.isNotEmpty() && CollectionsScreenVM.selectedFoldersViaLongClick.any {
                it.parentFolderId != null
            }) {
            Button(
                onClick = {
                    appVM.markSelectedFoldersAsRoot(onStart = {
                        showLoadingProgressBarOnTransferAction.value = true
                    }, onCompletion = {
                        showLoadingProgressBarOnTransferAction.value = false
                    })
                }, modifier = Modifier.fillMaxWidth().padding(
                    start = 15.dp,
                    end = 15.dp,
                    top = 5.dp,
                    bottom = if (!showNavigateToCollectionScreen) 5.dp else 0.dp
                )
            ) {
                Text(
                    text = Localization.Key.MarkSelectedFoldersAsRoot.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
        if (showNavigateToCollectionScreen) {
            Button(
                onClick = {
                    localNavController.navigate(Navigation.Root.CollectionsScreen)
                }, modifier = Modifier.fillMaxWidth().padding(
                    start = 15.dp, end = 15.dp, top = 5.dp, bottom = 5.dp
                )
            ) {
                Text(
                    text = Localization.Key.NavigateToCollectionsScreen.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleSmall
                )
            }
        }
    }
}