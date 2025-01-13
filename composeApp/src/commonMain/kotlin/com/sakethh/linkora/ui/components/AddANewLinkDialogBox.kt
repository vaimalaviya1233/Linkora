package com.sakethh.linkora.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.SubdirectoryArrowRight
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.contentColorFor
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableLongState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.sakethh.linkora.Platform
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.Constants
import com.sakethh.linkora.common.utils.isAValidURL
import com.sakethh.linkora.common.utils.isNull
import com.sakethh.linkora.common.utils.replaceFirstPlaceHolderWith
import com.sakethh.linkora.domain.LinkSaveConfig
import com.sakethh.linkora.domain.LinkType
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.ui.components.folder.SelectableFolderUIComponent
import com.sakethh.linkora.ui.domain.ScreenType
import com.sakethh.linkora.ui.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.ui.utils.linkoraLog
import com.sakethh.linkora.ui.utils.pulsateEffect
import com.sakethh.platform
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddANewLinkDialogBox(
    shouldBeVisible: MutableState<Boolean>,
    screenType: ScreenType, currentFolder: Folder?,
    collectionsScreenVM: CollectionsScreenVM
) {
    val isDataExtractingForTheLink = rememberSaveable {
        mutableStateOf(false)
    }
    val isDropDownMenuIconClicked = rememberSaveable {
        mutableStateOf(false)
    }
    val isAutoDetectTitleEnabled = rememberSaveable {
        mutableStateOf(AppPreferences.isAutoDetectTitleForLinksEnabled.value)
    }
    val isForceSaveWithoutFetchingMetaDataEnabled = rememberSaveable {
        mutableStateOf(AppPreferences.forceSaveWithoutFetchingAnyMetaData.value)
    }
    val isCreateANewFolderIconClicked = rememberSaveable {
        mutableStateOf(false)
    }
    val addTheFolderInRoot = rememberSaveable {
        mutableStateOf(false)
    }
    LaunchedEffect(isDataExtractingForTheLink.value) {
        if (isDataExtractingForTheLink.value) {
            isDropDownMenuIconClicked.value = false
        }
    }
    val coroutineScope = rememberCoroutineScope()
    val isChildFoldersBottomSheetExpanded = mutableStateOf(false)
    val btmSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)/*LaunchedEffect(key1 = Unit) {
        AddANewLinkDialogBox.currentUserAgent.value = AppPreferences.primaryJsoupUserAgent.value
        awaitAll(async {
            if (screenType == ScreenType.INTENT_ACTIVITY) {
                this.launch {
                    AppPreferences.readAllPreferencesValues(context)
                }
            }
        })
    }*/
    val lifecycleOwner = LocalLifecycleOwner.current
    if (shouldBeVisible.value) {
        val linkTextFieldValue = rememberSaveable {
            mutableStateOf("")
        }
        LaunchedEffect(Unit) {
            isDataExtractingForTheLink.value = false
            lifecycleOwner.lifecycle.currentStateFlow.collectLatest {
                when (it) {
                    Lifecycle.State.DESTROYED -> {}
                    Lifecycle.State.INITIALIZED -> {

                    }

                    Lifecycle.State.CREATED -> {}
                    Lifecycle.State.STARTED -> {}
                    Lifecycle.State.RESUMED -> {/* linkoraLog(it.name)
                         AddANewLinkDialogBox.updateUserAgent(
                             linkTextFieldValue.value,
                             context
                         )*/
                    }
                }
            }
        }
        val titleTextFieldValue = rememberSaveable {
            mutableStateOf("")
        }
        val noteTextFieldValue = rememberSaveable {
            mutableStateOf("")
        }
        val savedLinksLocalizedText =
            Localization.rememberLocalizedString(Localization.Key.SavedLinks)
        val selectedFolderName = rememberSaveable(savedLinksLocalizedText) {
            mutableStateOf(savedLinksLocalizedText)
        }
        val selectedFolderID = rememberSaveable {
            mutableLongStateOf(-2)
        }/* val childFolders =
             AddANewLinkDialogBox.childFolders.collectAsStateWithLifecycle()*/

        val lazyRowState = rememberLazyListState()
        val rootFolders = collectionsScreenVM.rootRegularFolders.collectAsStateWithLifecycle()
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
                )
                .background(AlertDialogDefaults.containerColor),
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
                            selectedFolderName = selectedFolderName,
                            selectedFolderIDForSaving = selectedFolderID,
                            isChildFoldersBottomSheetExpanded = isChildFoldersBottomSheetExpanded,
                            btmSheetState = btmSheetState,
                            lazyRowState = lazyRowState,
                            addTheFolderInRoot = addTheFolderInRoot,
                            isCreateANewFolderIconClicked = isCreateANewFolderIconClicked,
                            collectionsScreenVM = collectionsScreenVM,
                            currentFolder
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
                                selectedFolderName = selectedFolderName,
                                selectedFolderIDForSaving = selectedFolderID,
                                isChildFoldersBottomSheetExpanded = isChildFoldersBottomSheetExpanded,
                                btmSheetState = btmSheetState,
                                lazyRowState = lazyRowState,
                                addTheFolderInRoot = addTheFolderInRoot,
                                isCreateANewFolderIconClicked = isCreateANewFolderIconClicked,
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
                                Icon(imageVector = Icons.Default.Close, contentDescription = null)
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
    isForceSaveWithoutFetchingMetaDataEnabled: MutableState<Boolean>, currentFolder: Folder?
) {
    Column(
        modifier = Modifier.fillMaxWidth(if (platform() is Platform.Android.Mobile) 1f else 0.5f)
            .then(if (platform() is Platform.Android.Mobile) Modifier else Modifier.fillMaxHeight()),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            color = AlertDialogDefaults.titleContentColor, text = when (currentFolder) {
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

@OptIn(ExperimentalMaterial3Api::class)
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
    selectedFolderName: MutableState<String>,
    selectedFolderIDForSaving: MutableLongState,
    isChildFoldersBottomSheetExpanded: MutableState<Boolean>,
    btmSheetState: SheetState,
    lazyRowState: LazyListState,
    addTheFolderInRoot: MutableState<Boolean>,
    isCreateANewFolderIconClicked: MutableState<Boolean>,
    collectionsScreenVM: CollectionsScreenVM,
    currentFolder: Folder?
) {
    val coroutineScope = rememberCoroutineScope()
    val rootFolders = collectionsScreenVM.rootRegularFolders.collectAsStateWithLifecycle()
    val childFolders = collectionsScreenVM.childFolders.collectAsStateWithLifecycle()
    Column(
        modifier = Modifier.fillMaxSize().then(
            if (platform() is Platform.Android.Mobile) Modifier else Modifier.verticalScroll(
                rememberScrollState()
            )
        ),
        verticalArrangement = if (platform() is Platform.Android.Mobile) Arrangement.Top else Arrangement.Center
    ) {
        if (currentFolder.isNull()) {
            Text(
                text = Localization.rememberLocalizedString(Localization.Key.AddIn),
                color = contentColorFor(backgroundColor = AlertDialogDefaults.containerColor),
                style = MaterialTheme.typography.titleSmall,
                fontSize = 18.sp,
                modifier = Modifier.padding(
                    start = 20.dp,
                    top = 20.dp,
                    end = 20.dp
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
                            //  AddANewLinkDialogBox.subFoldersList.clear()
                        }
                    }) {
                    Text(
                        text = selectedFolderName.value,
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
                            // AddANewLinkDialogBox.subFoldersList.clear()
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
                    selectedFolderName.value =
                        Localization.getLocalizedString(Localization.Key.SavedLinks)
                    selectedFolderIDForSaving.longValue = Constants.SAVED_LINKS_ID
                },
                folderName = Localization.rememberLocalizedString(Localization.Key.SavedLinks),
                imageVector = Icons.Outlined.Link,
                isComponentSelected = selectedFolderIDForSaving.longValue == Constants.SAVED_LINKS_ID
            )

            SelectableFolderUIComponent(
                onClick = {
                    selectedFolderName.value =
                        Localization.getLocalizedString(Localization.Key.ImportantLinks)
                    isDropDownMenuIconClicked.value = false
                    selectedFolderIDForSaving.longValue = Constants.IMPORTANT_LINKS_ID
                },
                folderName = Localization.rememberLocalizedString(Localization.Key.ImportantLinks),
                imageVector = Icons.Outlined.StarOutline,
                isComponentSelected = selectedFolderIDForSaving.longValue == Constants.IMPORTANT_LINKS_ID
            )

            // Not good, but I’m not creating another file just for another platform. This works for now.
            rootFolders.value.forEach {
                FolderSelectorComponent(
                    onItemClick = {
                        selectedFolderName.value = it.name
                        selectedFolderIDForSaving.longValue = it.localId
                        isDropDownMenuIconClicked.value = false
                    },
                    isCurrentFolderSelected = mutableStateOf(it.localId == selectedFolderIDForSaving.longValue),
                    folderName = it.name,
                    onSubDirectoryIconClick = {
                        /*AddANewLinkDialogBox.changeParentFolderId(it.id, context)
                        AddANewLinkDialogBox.subFoldersList.add(it)*/
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
                        selectedFolderName.value = it.name
                        selectedFolderIDForSaving.longValue = it.localId
                    })

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
                    isCreateANewFolderIconClicked.value = true
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

        Spacer(Modifier.height(10.dp))

        if (!isDataExtractingForTheLink.value) {
            OutlinedButton(
                colors = ButtonDefaults.outlinedButtonColors(), border = BorderStroke(
                    width = 1.dp, color = MaterialTheme.colorScheme.secondary
                ), modifier = Modifier.padding(
                    end = 20.dp, start = 20.dp
                ).fillMaxWidth().pulsateEffect(), onClick = {
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
                        when (if (currentFolder.isNull()) selectedFolderIDForSaving.longValue else currentFolder?.localId) {
                            Constants.SAVED_LINKS_ID -> LinkType.SAVED_LINK
                            Constants.IMPORTANT_LINKS_ID -> LinkType.IMPORTANT_LINK
                            else -> LinkType.FOLDER_LINK
                        }
                    linkoraLog(linkType)
                    collectionsScreenVM.addANewLink(
                        link = Link(
                            linkType = linkType,
                            title = titleTextFieldValue.value,
                            url = linkTextFieldValue.value,
                            imgURL = "",
                            note = noteTextFieldValue.value,
                            idOfLinkedFolder = currentFolder?.localId
                                ?: selectedFolderIDForSaving.longValue,
                            userAgent = AppPreferences.primaryJsoupUserAgent.value
                        ), linkSaveConfig = LinkSaveConfig(
                            forceAutoDetectTitle = isAutoDetectTitleEnabled.value || AppPreferences.isAutoDetectTitleForLinksEnabled.value,
                            forceSaveWithoutRetrievingData = isForceSaveWithoutFetchingMetaDataEnabled.value || AppPreferences.forceSaveWithoutFetchingAnyMetaData.value
                        ), onCompletion = {
                            collectionsScreenVM.triggerLinksSorting()
                            shouldBeVisible.value = false
                        })
                }) {
                Text(
                    text = Localization.rememberLocalizedString(Localization.Key.Save),
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp
                )
            }
            if (isAValidURL(linkTextFieldValue.value) && !AppPreferences.forceSaveWithoutFetchingAnyMetaData.value && !isForceSaveWithoutFetchingMetaDataEnabled.value) {
                HorizontalDivider(
                    modifier = Modifier.padding(20.dp),
                    thickness = 1.dp,
                    color = MaterialTheme.colorScheme.outline.copy(0.25f)
                )
                Card(
                    border = BorderStroke(
                        1.dp, contentColorFor(MaterialTheme.colorScheme.surface)
                    ),
                    colors = CardDefaults.cardColors(containerColor = AlertDialogDefaults.containerColor),
                    modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().wrapContentHeight().padding(
                            top = 10.dp, bottom = 10.dp
                        ), verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterStart
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Info,
                                contentDescription = null,
                                modifier = Modifier.padding(
                                    start = 10.dp, end = 10.dp
                                )
                            )
                        }
                        Text(
                            text = if (linkTextFieldValue.value.trim()
                                    .startsWith("https://x.com/") || linkTextFieldValue.value.trim()
                                    .startsWith("http://x.com/") || linkTextFieldValue.value.trim()
                                    .startsWith("https://twitter.com/") || linkTextFieldValue.value.trim()
                                    .startsWith("http://twitter.com/")
                            ) {
                                buildAnnotatedString {
                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append("vxTwitter API")
                                    }
                                    append(" " + "LocalizedStrings.willBeUsedToRetrieveMetadata.value")
                                }
                            } else {
                                buildAnnotatedString {
                                    append("LocalizedStrings.userAgent.value" + " ")
                                    withStyle(SpanStyle(fontWeight = FontWeight.SemiBold)) {
                                        append("AddANewLinkDialogBox.currentUserAgent.value")
                                    }
                                    append(" " + "LocalizedStrings.willBeUsedToRetrieveMetadata.value")
                                }
                            },
                            style = MaterialTheme.typography.titleSmall,
                            fontSize = 14.sp,
                            lineHeight = 18.sp,
                            textAlign = TextAlign.Start,
                            modifier = Modifier.padding(end = 10.dp))
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.height(30.dp))
            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth().padding(start = 20.dp, end = 20.dp)
            )
        }
    }
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