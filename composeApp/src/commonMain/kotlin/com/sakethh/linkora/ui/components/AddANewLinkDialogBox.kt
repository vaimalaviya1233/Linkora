package com.sakethh.linkora.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.Localization
import com.sakethh.linkora.di.DependencyContainer
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.Platform
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.onSuccess
import com.sakethh.linkora.platform.platform
import com.sakethh.linkora.preferences.AppPreferences
import com.sakethh.linkora.ui.components.folder.SelectableFolderUIComponent
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.domain.model.AddNewFolderDialogBoxParam
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.linkora.ui.utils.rememberDeserializableMutableObject
import com.sakethh.linkora.utils.Constants
import com.sakethh.linkora.utils.defaultFolderIds
import com.sakethh.linkora.utils.defaultImpLinksFolder
import com.sakethh.linkora.utils.defaultSavedLinksFolder
import com.sakethh.linkora.utils.getLocalizedString
import com.sakethh.linkora.utils.isNull
import com.sakethh.linkora.utils.pushSnackbarOnFailure
import com.sakethh.linkora.utils.rememberLocalizedString
import com.sakethh.linkora.utils.replaceFirstPlaceHolderWith
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddANewLinkDialogBox(
    shouldBeVisible: MutableState<Boolean>,
    screenType: ScreenType, currentFolder: Folder?,
    collectionsScreenVM: CollectionsScreenVM,
    url: String = "",
) {
    val isDataExtractingForTheLink = rememberSaveable {
        mutableStateOf(false)
    }

    val isAutoDetectTitleEnabled = rememberSaveable {
        mutableStateOf(AppPreferences.isAutoDetectTitleForLinksEnabled.value)
    }
    val isForceSaveWithoutFetchingMetaDataEnabled = rememberSaveable {
        mutableStateOf(AppPreferences.forceSaveWithoutFetchingAnyMetaData.value)
    }
    val addTheFolderInRoot = rememberSaveable {
        mutableStateOf(false)
    }
    val isChildFoldersBottomSheetExpanded = rememberSaveable {
        mutableStateOf(false)
    }
    val btmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val lifecycleOwner = LocalLifecycleOwner.current
    if (shouldBeVisible.value) {
        val isDropDownMenuIconClicked = rememberSaveable {
            mutableStateOf(false)
        }
        LaunchedEffect(isDataExtractingForTheLink.value) {
            if (isDataExtractingForTheLink.value) {
                isDropDownMenuIconClicked.value = false
            }
        }
        val linkTextFieldValue = rememberSaveable {
            mutableStateOf(url)
        }
        LaunchedEffect(Unit) {
            isDataExtractingForTheLink.value = false
        }
        val titleTextFieldValue = rememberSaveable {
            mutableStateOf("")
        }
        val noteTextFieldValue = rememberSaveable {
            mutableStateOf("")
        }

        val lazyRowState = rememberLazyListState()
        BasicAlertDialog(
            onDismissRequest = {
                if (!isDataExtractingForTheLink.value) {
                    shouldBeVisible.value = false
                }
            },
            modifier = Modifier.fillMaxSize(if (platform() is Platform.Android.Mobile) 1f else 0.9f)
                .then(
                    if (platform() !is Platform.Android.Mobile) Modifier.clip(
                        RoundedCornerShape(10.dp)
                    ) else Modifier
                ).background(AlertDialogDefaults.containerColor),
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Surface(
                modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface
            ) {
                if (platform() == Platform.Android.Mobile) {
                    Column(
                        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())
                    ) {
                        TopPartOfAddANewLinkDialogBox(
                            isDataExtractingForTheLink = isDataExtractingForTheLink.value,
                            linkTextFieldValue = linkTextFieldValue,
                            titleTextFieldValue = titleTextFieldValue,
                            noteTextFieldValue = noteTextFieldValue,
                            isAutoDetectTitleEnabled = isAutoDetectTitleEnabled,
                            isForceSaveWithoutFetchingMetaDataEnabled = isForceSaveWithoutFetchingMetaDataEnabled,
                            currentFolder
                        )
                        BottomPartOfAddANewLinkDialogBox(
                            shouldBeVisible = shouldBeVisible,
                            isDataExtractingForTheLink = isDataExtractingForTheLink,
                            screenType = screenType,
                            linkTextFieldValue = linkTextFieldValue,
                            titleTextFieldValue = titleTextFieldValue,
                            noteTextFieldValue = noteTextFieldValue,
                            isAutoDetectTitleEnabled = isAutoDetectTitleEnabled,
                            isForceSaveWithoutFetchingMetaDataEnabled = isForceSaveWithoutFetchingMetaDataEnabled,
                            isDropDownMenuIconClicked = isDropDownMenuIconClicked,
                            isChildFoldersBottomSheetExpanded = isChildFoldersBottomSheetExpanded,
                            btmSheetState = btmSheetState,
                            lazyRowState = lazyRowState,
                            addTheFolderInRoot = addTheFolderInRoot,
                            collectionsScreenVM = collectionsScreenVM,
                            currentlyInFolder = currentFolder,
                        )
                        Spacer(Modifier.height(50.dp))
                    }
                } else {
                    Box(Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier.animateContentSize().fillMaxSize()
                                .navigationBarsPadding()
                        ) {
                            TopPartOfAddANewLinkDialogBox(
                                isDataExtractingForTheLink = isDataExtractingForTheLink.value,
                                linkTextFieldValue = linkTextFieldValue,
                                titleTextFieldValue = titleTextFieldValue,
                                noteTextFieldValue = noteTextFieldValue,
                                isAutoDetectTitleEnabled = isAutoDetectTitleEnabled,
                                isForceSaveWithoutFetchingMetaDataEnabled = isForceSaveWithoutFetchingMetaDataEnabled,
                                currentFolder
                            )
                            VerticalDivider(
                                modifier = Modifier.padding(
                                    start = 20.dp, end = 20.dp
                                ), color = LocalContentColor.current.copy(0.01f), thickness = 1.dp
                            )
                            BottomPartOfAddANewLinkDialogBox(
                                shouldBeVisible = shouldBeVisible,
                                isDataExtractingForTheLink = isDataExtractingForTheLink,
                                screenType = screenType,
                                linkTextFieldValue = linkTextFieldValue,
                                titleTextFieldValue = titleTextFieldValue,
                                noteTextFieldValue = noteTextFieldValue,
                                isAutoDetectTitleEnabled = isAutoDetectTitleEnabled,
                                isForceSaveWithoutFetchingMetaDataEnabled = isForceSaveWithoutFetchingMetaDataEnabled,
                                isDropDownMenuIconClicked = isDropDownMenuIconClicked,
                                isChildFoldersBottomSheetExpanded = isChildFoldersBottomSheetExpanded,
                                btmSheetState = btmSheetState,
                                lazyRowState = lazyRowState,
                                addTheFolderInRoot = addTheFolderInRoot,
                                collectionsScreenVM = collectionsScreenVM,
                                currentFolder
                            )
                        }
                        if (!isDataExtractingForTheLink.value) {
                            IconButton(
                                modifier = Modifier.align(Alignment.TopEnd).padding(15.dp),
                                onClick = {
                                    shouldBeVisible.value = false
                                }) {
                                Icon(
                                    imageVector = Icons.Default.Close, contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TopPartOfAddANewLinkDialogBox(
    isDataExtractingForTheLink: Boolean,
    linkTextFieldValue: MutableState<String>,
    titleTextFieldValue: MutableState<String>,
    noteTextFieldValue: MutableState<String>,
    isAutoDetectTitleEnabled: MutableState<Boolean>,
    isForceSaveWithoutFetchingMetaDataEnabled: MutableState<Boolean>,
    currentFolder: Folder?
) {
    Column(
        modifier = Modifier.fillMaxWidth(if (platform() is Platform.Android.Mobile) 1f else 0.5f)
            .then(if (platform() is Platform.Android.Mobile) Modifier else Modifier.fillMaxHeight()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            color = AlertDialogDefaults.titleContentColor,
            text = when (currentFolder) {
                null -> Localization.rememberLocalizedString(
                    Localization.Key.AddANewLink
                )

                else -> Localization.rememberLocalizedString(Localization.Key.AddANewLinkIn)
                    .replaceFirstPlaceHolderWith(currentFolder.name)
            },
            style = MaterialTheme.typography.titleMedium,
            fontSize = 22.sp,
            modifier = Modifier.padding(
                start = 20.dp, top = 30.dp, end = 20.dp
            ),
            lineHeight = 28.sp
        )

        OutlinedTextField(
            readOnly = isDataExtractingForTheLink,
            modifier = Modifier.padding(
                start = 20.dp, end = 20.dp, top = 20.dp
            ).fillMaxWidth(),
            label = {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.LinkAddress),
                    color = AlertDialogDefaults.textContentColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.sp
                )
            },
            textStyle = MaterialTheme.typography.titleSmall,
            singleLine = true,
            shape = RoundedCornerShape(5.dp),
            value = linkTextFieldValue.value,
            onValueChange = {
                linkTextFieldValue.value = it/*AddANewLinkDialogBox.updateUserAgent(
                    linkTextFieldValue.value,
                    context
                )*/
            })

        Box(modifier = Modifier.animateContentSize()) {
            if (!AppPreferences.isAutoDetectTitleForLinksEnabled.value && !isAutoDetectTitleEnabled.value) {
                OutlinedTextField(
                    readOnly = isDataExtractingForTheLink,
                    modifier = Modifier.padding(
                        start = 20.dp, end = 20.dp, top = 15.dp
                    ).fillMaxWidth(),
                    label = {
                        Text(
                            text = Localization.rememberLocalizedString(
                                Localization.Key.TitleForTheLink
                            ),
                            color = AlertDialogDefaults.textContentColor,
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 12.sp
                        )
                    },
                    textStyle = MaterialTheme.typography.titleSmall,
                    singleLine = true,
                    value = titleTextFieldValue.value,
                    onValueChange = {
                        titleTextFieldValue.value = it
                    })
            }
        }
        OutlinedTextField(
            readOnly = isDataExtractingForTheLink,
            modifier = Modifier.padding(
                start = 20.dp, end = 20.dp, top = 15.dp
            ).fillMaxWidth(),
            label = {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.NoteForSavingTheLink),
                    color = AlertDialogDefaults.textContentColor,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 12.sp
                )
            },
            textStyle = MaterialTheme.typography.titleSmall,
            singleLine = true,
            value = noteTextFieldValue.value,
            onValueChange = {
                noteTextFieldValue.value = it
            })
        if (AppPreferences.isAutoDetectTitleForLinksEnabled.value || AppPreferences.forceSaveWithoutFetchingAnyMetaData.value) {
            InfoCard(
                if (AppPreferences.isAutoDetectTitleForLinksEnabled.value) Localization.rememberLocalizedString(
                    Localization.Key.AutoDetectTitleIsEnabled
                ) else Localization.rememberLocalizedString(
                    Localization.Key.DataRetrievalDisabled
                )
            )
        }
        Box(modifier = Modifier.fillMaxWidth().animateContentSize()) {
            if (!isForceSaveWithoutFetchingMetaDataEnabled.value && !AppPreferences.isAutoDetectTitleForLinksEnabled.value && !AppPreferences.forceSaveWithoutFetchingAnyMetaData.value) {
                Row(
                    modifier = Modifier.padding(top = if (AppPreferences.isAutoDetectTitleForLinksEnabled.value) 0.dp else 10.dp)
                        .fillMaxWidth().clickable {
                            if (!isDataExtractingForTheLink) {
                                isAutoDetectTitleEnabled.value = !isAutoDetectTitleEnabled.value
                            }
                        }.padding(
                            start = 10.dp, end = 20.dp
                        ), verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        enabled = !isDataExtractingForTheLink,
                        checked = isAutoDetectTitleEnabled.value,
                        onCheckedChange = {
                            isAutoDetectTitleEnabled.value = it
                            if (it) {
                                isForceSaveWithoutFetchingMetaDataEnabled.value = false
                            }
                        })
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.ForceAutoDetectTitle),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp
                    )
                }
            }
        }

        if (!isAutoDetectTitleEnabled.value && !AppPreferences.isAutoDetectTitleForLinksEnabled.value && !AppPreferences.forceSaveWithoutFetchingAnyMetaData.value) {
            Row(
                modifier = Modifier.padding(top = 10.dp).fillMaxWidth().clickable {
                    if (!isDataExtractingForTheLink) {
                        isForceSaveWithoutFetchingMetaDataEnabled.value =
                            !isForceSaveWithoutFetchingMetaDataEnabled.value
                    }
                }.padding(
                    start = 10.dp, end = 20.dp
                ), verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    enabled = !isDataExtractingForTheLink,
                    checked = isForceSaveWithoutFetchingMetaDataEnabled.value,
                    onCheckedChange = {
                        isForceSaveWithoutFetchingMetaDataEnabled.value = it
                        if (it) {
                            isAutoDetectTitleEnabled.value = false
                        }
                    })
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.ForceSaveWithoutRetrievingMetadata),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@OptIn(
    ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class, ExperimentalLayoutApi::class
)
@Composable
private fun BottomPartOfAddANewLinkDialogBox(
    shouldBeVisible: MutableState<Boolean>,
    isDataExtractingForTheLink: MutableState<Boolean>,
    screenType: ScreenType,
    linkTextFieldValue: MutableState<String>,
    titleTextFieldValue: MutableState<String>,
    noteTextFieldValue: MutableState<String>,
    isAutoDetectTitleEnabled: MutableState<Boolean>,
    isForceSaveWithoutFetchingMetaDataEnabled: MutableState<Boolean>,
    isDropDownMenuIconClicked: MutableState<Boolean>,
    isChildFoldersBottomSheetExpanded: MutableState<Boolean>,
    btmSheetState: SheetState,
    lazyRowState: LazyListState,
    addTheFolderInRoot: MutableState<Boolean>,
    collectionsScreenVM: CollectionsScreenVM,
    currentlyInFolder: Folder?,
) {
    val coroutineScope = rememberCoroutineScope()
    val rootFolders = collectionsScreenVM.rootRegularFolders.collectAsStateWithLifecycle()
    val shouldShowNewFolderDialog = rememberSaveable {
        mutableStateOf(false)
    }

    val allTags by collectionsScreenVM.allTags.collectAsStateWithLifecycle()
    val selectedFolderForSavingTheLink = rememberDeserializableMutableObject {
        mutableStateOf(
            Folder(
                name = Localization.Key.SavedLinks.getLocalizedString(),
                note = "",
                parentFolderId = null,
                localId = Constants.SAVED_LINKS_ID,
                remoteId = null,
                isArchived = false
            )
        )
    }
    Column(
        modifier = Modifier.fillMaxSize().then(
            if (platform() is Platform.Android.Mobile) Modifier else Modifier.verticalScroll(
                rememberScrollState()
            )
        ),
        verticalArrangement = if (platform() is Platform.Android.Mobile) Arrangement.Top else Arrangement.Center
    ) {
        Text(
            text = "Attach Tags",
            color = MaterialTheme.colorScheme.secondary,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 18.sp,
            modifier = Modifier.padding(
                start = 20.dp, top = 10.dp, end = 20.dp
            )
        )

        TagSelectionComponent(
            allTags = allTags,
            selectedTags = collectionsScreenVM.selectedTags,
            onClick = {
                if (collectionsScreenVM.selectedTags.contains(it)) {
                    collectionsScreenVM.unSelectATag(it)
                } else {
                    collectionsScreenVM.selectATag(it)
                }
            }
        )

        if (currentlyInFolder.isNull()) {
            Text(
                text = Localization.rememberLocalizedString(Localization.Key.AddIn),
                color = MaterialTheme.colorScheme.secondary,
                style = MaterialTheme.typography.titleSmall,
                fontSize = 18.sp,
                modifier = Modifier.padding(
                    start = 20.dp, top = 10.dp, end = 20.dp
                )
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp, top = 10.dp)
            ) {
                FilledTonalButton(
                    modifier = Modifier.pulsateEffect().fillMaxWidth(0.8f), onClick = {
                        if (!isDataExtractingForTheLink.value) {
                            isDropDownMenuIconClicked.value = !isDropDownMenuIconClicked.value
                            AddANewLinkDialogBox.subFoldersList.clear()
                        }
                    }) {
                    Text(
                        text = selectedFolderForSavingTheLink.value.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 18.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                    )
                }
                Spacer(modifier = Modifier.width(5.dp))
                FilledTonalIconButton(
                    modifier = Modifier.pulsateEffect(
                        0.75f
                    ), onClick = {
                        if (!isDataExtractingForTheLink.value) {
                            isDropDownMenuIconClicked.value = !isDropDownMenuIconClicked.value
                            AddANewLinkDialogBox.subFoldersList.clear()
                        }
                    }) {
                    Icon(
                        imageVector = if (isDropDownMenuIconClicked.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null
                    )
                }
            }
        }


        if (isDropDownMenuIconClicked.value) {

            SelectableFolderUIComponent(
                onClick = {
                    isDropDownMenuIconClicked.value = false
                    selectedFolderForSavingTheLink.value = defaultSavedLinksFolder()
                },
                folderName = Localization.rememberLocalizedString(Localization.Key.SavedLinks),
                imageVector = Icons.Outlined.Link,
                isComponentSelected = selectedFolderForSavingTheLink.value.localId == Constants.SAVED_LINKS_ID
            )

            SelectableFolderUIComponent(
                onClick = {
                    isDropDownMenuIconClicked.value = false
                    selectedFolderForSavingTheLink.value = defaultImpLinksFolder()
                },
                folderName = Localization.rememberLocalizedString(Localization.Key.ImportantLinks),
                imageVector = Icons.Outlined.StarOutline,
                isComponentSelected = selectedFolderForSavingTheLink.value.localId == Constants.IMPORTANT_LINKS_ID
            )

            rootFolders.value.forEach {
                key(it) {
                    FolderSelectorComponent(
                        onItemClick = {
                        isDropDownMenuIconClicked.value = false
                        selectedFolderForSavingTheLink.value = it
                    },
                        isCurrentFolderSelected = rememberSaveable(it.localId == selectedFolderForSavingTheLink.value.localId) {
                            mutableStateOf(it.localId == selectedFolderForSavingTheLink.value.localId)
                        },
                        folderName = it.name,
                        onSubDirectoryIconClick = {
                            AddANewLinkDialogBox.changeParentFolderId(
                                it.localId, collectionsScreenVM.viewModelScope
                            )
                            AddANewLinkDialogBox.subFoldersList.add(it)
                            isChildFoldersBottomSheetExpanded.value = true
                            coroutineScope.launch {
                                btmSheetState.expand()
                                try {
                                    if (lazyRowState.layoutInfo.totalItemsCount - 1 < 0) return@launch
                                    lazyRowState.animateScrollToItem(lazyRowState.layoutInfo.totalItemsCount - 1)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            selectedFolderForSavingTheLink.value = it
                        })
                }

            }
            if (!isDropDownMenuIconClicked.value) {
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
        if (isDataExtractingForTheLink.value.not() && screenType == ScreenType.INTENT_ACTIVITY) {
            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ), modifier = Modifier.padding(
                    end = 20.dp,
                    top = if (isDropDownMenuIconClicked.value) 20.dp else 5.dp,
                    start = 20.dp
                ).fillMaxWidth().pulsateEffect(), onClick = {
                    addTheFolderInRoot.value = true
                    shouldShowNewFolderDialog.value = true
                }) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CreateNewFolder, null)
                    Spacer(Modifier.width(5.dp))
                    Text(
                        text = Localization.rememberLocalizedString(Localization.Key.CreateANewFolder),
                        style = MaterialTheme.typography.titleSmall,
                        fontSize = 16.sp
                    )
                }
            }

            if (AppPreferences.isAutoDetectTitleForLinksEnabled.value) {
                HorizontalDivider(
                    modifier = Modifier.padding(20.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(0.25f)
                )
            }

        }

        if (!isDataExtractingForTheLink.value) {
            if (isDropDownMenuIconClicked.value) {
                Spacer(Modifier.height(10.dp))
            }
            if (currentlyInFolder.isNull()) {
                Button(
                    modifier = Modifier.padding(
                        end = 20.dp,
                        start = 20.dp,
                        top = if (isDropDownMenuIconClicked.value) 0.dp else 5.dp
                    ).fillMaxWidth().pulsateEffect(), onClick = {
                        addTheFolderInRoot.value = false
                        shouldShowNewFolderDialog.value = true
                    }, colors = ButtonDefaults.filledTonalButtonColors()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.CreateNewFolder, contentDescription = null)
                        Spacer(Modifier.width(5.dp))
                        Text(
                            text = Localization.Key.CreateANewFolder.rememberLocalizedString(),
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            HorizontalDivider(
                modifier = Modifier.fillMaxWidth()
                    .padding(start = 25.dp, end = 25.dp, top = 10.dp, bottom = 10.dp),
                color = DividerDefaults.color.copy(0.25f)
            )
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(), border = BorderStroke(
                    width = 1.dp, color = MaterialTheme.colorScheme.secondary
                ), modifier = Modifier.padding(
                    end = 20.dp, start = 20.dp
                ).fillMaxWidth().pulsateEffect(), onClick = {
                    collectionsScreenVM.clearSelectedTags()
                    shouldBeVisible.value = false
                    isForceSaveWithoutFetchingMetaDataEnabled.value = false
                }) {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.Cancel),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
            Button(
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary),
                modifier = Modifier.padding(
                    end = 20.dp, top = 10.dp, start = 20.dp
                ).fillMaxWidth().pulsateEffect(),
                onClick = {
                    isDataExtractingForTheLink.value = true
                    val linkType =
                        when (if (currentlyInFolder.isNull()) selectedFolderForSavingTheLink.value.localId else currentlyInFolder?.localId) {
                            Constants.SAVED_LINKS_ID -> LinkType.SAVED_LINK
                            Constants.IMPORTANT_LINKS_ID -> LinkType.IMPORTANT_LINK
                            else -> LinkType.FOLDER_LINK
                        }
                    collectionsScreenVM.addANewLink(
                        link = Link(
                            linkType = linkType,
                            title = titleTextFieldValue.value,
                            url = linkTextFieldValue.value,
                            imgURL = "",
                            note = noteTextFieldValue.value,
                            idOfLinkedFolder = currentlyInFolder?.localId
                                ?: selectedFolderForSavingTheLink.value.localId,
                            userAgent = AppPreferences.primaryJsoupUserAgent.value
                        ), linkSaveConfig = LinkSaveConfig(
                            forceAutoDetectTitle = isAutoDetectTitleEnabled.value || AppPreferences.isAutoDetectTitleForLinksEnabled.value,
                            forceSaveWithoutRetrievingData = isForceSaveWithoutFetchingMetaDataEnabled.value || AppPreferences.forceSaveWithoutFetchingAnyMetaData.value
                        ), onCompletion = {
                            shouldBeVisible.value = false
                        }, selectedTags = collectionsScreenVM.selectedTags
                    )
                }) {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.Save),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
        } else {
            Spacer(modifier = Modifier.height(30.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp)
            )
        }
    }
    if (isChildFoldersBottomSheetExpanded.value) {
        val childFolders = AddANewLinkDialogBox.childFolders.collectAsStateWithLifecycle()
        ModalBottomSheet(sheetState = btmSheetState, onDismissRequest = {
            AddANewLinkDialogBox.subFoldersList.clear()
            isChildFoldersBottomSheetExpanded.value = false
        }) {
            LazyColumn(
                Modifier.fillMaxWidth().wrapContentHeight()
            ) {
                stickyHeader {
                    Column(
                        modifier = Modifier.fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface)
                    ) {
                        TopAppBar(title = {
                            Text(
                                text = AddANewLinkDialogBox.subFoldersList.last().name,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 24.sp
                            )
                        })
                        LazyRow(
                            state = lazyRowState, modifier = Modifier.padding(
                                start = 15.dp, end = 15.dp, bottom = 15.dp
                            ), verticalAlignment = Alignment.CenterVertically
                        ) {
                            item {
                                Text(
                                    text = "/",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 16.sp
                                )
                            }
                            itemsIndexed(AddANewLinkDialogBox.subFoldersList.toMutableStateList()) { index, subFolder ->
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.ArrowRight,
                                    contentDescription = ""
                                )
                                Text(
                                    text = subFolder.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 16.sp,
                                    modifier = Modifier.clickable {
                                        AddANewLinkDialogBox.changeParentFolderId(
                                            subFolder.localId, collectionsScreenVM.viewModelScope
                                        )
                                        selectedFolderForSavingTheLink.value = subFolder
                                        if (AddANewLinkDialogBox.subFoldersList.indexOf(
                                                subFolder
                                            ) != AddANewLinkDialogBox.subFoldersList.indexOf(
                                                AddANewLinkDialogBox.subFoldersList.last()
                                            )
                                        ) {
                                            AddANewLinkDialogBox.subFoldersList.removeAll(
                                                AddANewLinkDialogBox.subFoldersList.toList()
                                                    .subList(
                                                        fromIndex = AddANewLinkDialogBox.subFoldersList.indexOf(
                                                            AddANewLinkDialogBox.subFoldersList.find {
                                                                it.localId == selectedFolderForSavingTheLink.value.localId
                                                            }) + 1,
                                                        toIndex = AddANewLinkDialogBox.subFoldersList.size
                                                    ).toSet()
                                            )
                                        }
                                    })
                            }
                        }
                        HorizontalDivider(color = LocalContentColor.current.copy(0.25f))
                        Spacer(modifier = Modifier.height(15.dp))
                    }
                }
                if (childFolders.value.isNotEmpty()) {
                    items(childFolders.value) {
                        FolderSelectorComponent(
                            onItemClick = {
                            selectedFolderForSavingTheLink.value = it
                            isDropDownMenuIconClicked.value = false
                            AddANewLinkDialogBox.subFoldersList.clear()
                            coroutineScope.launch {
                                btmSheetState.hide()
                            }
                            isChildFoldersBottomSheetExpanded.value = false
                        },
                            isCurrentFolderSelected = rememberSaveable(it.localId == selectedFolderForSavingTheLink.value.localId) {
                                mutableStateOf(it.localId == selectedFolderForSavingTheLink.value.localId)
                            },
                            folderName = it.name,
                            onSubDirectoryIconClick = {
                                selectedFolderForSavingTheLink.value = it
                                AddANewLinkDialogBox.subFoldersList.add(it)
                                AddANewLinkDialogBox.changeParentFolderId(
                                    selectedFolderForSavingTheLink.value.localId,
                                    collectionsScreenVM.viewModelScope
                                )
                                coroutineScope.launch {
                                    try {
                                        if (lazyRowState.layoutInfo.totalItemsCount - 1 < 0) return@launch
                                        lazyRowState.animateScrollToItem(lazyRowState.layoutInfo.totalItemsCount - 1)
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            })
                    }
                } else {
                    item {
                        Text(
                            text = Localization.Key.ThisFolderHasNoSubfolders.rememberLocalizedString(),
                            color = MaterialTheme.colorScheme.onSurface,
                            style = MaterialTheme.typography.titleMedium,
                            fontSize = 24.sp,
                            lineHeight = 36.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.fillMaxSize().padding(15.dp)
                        )
                    }
                    item {
                        FilledTonalButton(
                            modifier = Modifier.padding(
                                top = 5.dp, end = 15.dp, start = 15.dp
                            ).fillMaxWidth().pulsateEffect(), onClick = {
                                addTheFolderInRoot.value = false
                                shouldShowNewFolderDialog.value = true
                            }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreateNewFolder, null)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = Localization.Key.CreateANewFolder.rememberLocalizedString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontSize = 16.sp
                                )
                            }
                        }
                        Button(
                            modifier = Modifier.fillMaxWidth().padding(start = 15.dp, end = 15.dp),
                            onClick = {
                                isDropDownMenuIconClicked.value = false
                                AddANewLinkDialogBox.subFoldersList.clear()
                                coroutineScope.launch {
                                    btmSheetState.hide()
                                }
                                isChildFoldersBottomSheetExpanded.value = false
                            }) {
                            Text(
                                text = Localization.Key.SaveInThisFolder.rememberLocalizedString(),
                                style = MaterialTheme.typography.titleSmall
                            )
                        }
                    }
                    item {
                        Spacer(Modifier.height(5.dp))
                    }
                }
                if (AddANewLinkDialogBox.childFolders.value.isNotEmpty()) {
                    item {
                        FilledTonalButton(
                            modifier = Modifier.padding(
                                end = 20.dp, top = 15.dp, start = 20.dp, bottom = 15.dp
                            ).fillMaxWidth().pulsateEffect(), onClick = {
                                addTheFolderInRoot.value = false
                                shouldShowNewFolderDialog.value = true
                            }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CreateNewFolder, null)
                                Spacer(Modifier.width(5.dp))
                                Text(
                                    text = Localization.Key.CreateANewFolder.rememberLocalizedString(),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontSize = 16.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
    AddANewFolderDialogBox(
        AddNewFolderDialogBoxParam(
            shouldBeVisible = shouldShowNewFolderDialog,
            inAChildFolderScreen = addTheFolderInRoot.value.not(),
            onFolderCreateClick = { folderName, folderNote, onCompletion ->
                collectionsScreenVM.insertANewFolder(
                    folder = Folder(
                        name = folderName,
                        note = folderNote,
                        parentFolderId = if (addTheFolderInRoot.value || selectedFolderForSavingTheLink.value.localId in defaultFolderIds()) null else selectedFolderForSavingTheLink.value.localId,
                    ), onCompletion = onCompletion, ignoreFolderAlreadyExistsThrowable = true
                )
            },
            thisFolder = if (selectedFolderForSavingTheLink.value.localId in defaultFolderIds()) null else selectedFolderForSavingTheLink.value
        )
    )
}


@Composable
private fun FolderSelectorComponent(
    onItemClick: () -> Unit,
    isCurrentFolderSelected: MutableState<Boolean>,
    folderName: String,
    onSubDirectoryIconClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth().clickable {
            onItemClick()
        }) {
        Row(
            modifier = Modifier.fillMaxWidth().wrapContentHeight(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                tint = if (isCurrentFolderSelected.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                imageVector = Icons.Outlined.Folder,
                contentDescription = null,
                modifier = Modifier.padding(
                    start = 20.dp, end = 20.dp, top = 0.dp
                ).size(28.dp)
            )
            Box(
                modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isCurrentFolderSelected.value) {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            tint = if (isCurrentFolderSelected.value) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                    IconButton(onClick = {
                        onSubDirectoryIconClick()
                    }) {
                        Icon(
                            imageVector = Icons.Filled.SubdirectoryArrowRight,
                            contentDescription = null
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                }
            }
        }
        Text(
            text = folderName,
            color = if (isCurrentFolderSelected.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,
            style = MaterialTheme.typography.titleSmall,
            fontSize = 16.sp,
            lineHeight = 20.sp,
            maxLines = 1,
            modifier = Modifier.padding(
                start = 20.dp, end = 20.dp
            ),
            overflow = TextOverflow.Ellipsis
        )
        HorizontalDivider(
            modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 10.dp),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(0.1f)
        )
    }
}

object AddANewLinkDialogBox {
    val subFoldersList = mutableStateListOf<Folder>()

    private val _childFolders = MutableStateFlow(emptyList<Folder>())
    val childFolders = _childFolders.asStateFlow()

    private var changeParentFolderIdJob: Job? = null

    fun changeParentFolderId(parentFolderId: Long, coroutineScope: CoroutineScope) {
        changeParentFolderIdJob?.cancel()
        linkoraLog(parentFolderId)
        changeParentFolderIdJob = coroutineScope.launch {
            DependencyContainer.localFoldersRepo.getChildFoldersOfThisParentIDAsFlow(
                parentFolderId
            ).cancellable().collectLatest {
                it.onSuccess {
                    _childFolders.emit(it.data)
                }.pushSnackbarOnFailure()
            }
        }
    }
}