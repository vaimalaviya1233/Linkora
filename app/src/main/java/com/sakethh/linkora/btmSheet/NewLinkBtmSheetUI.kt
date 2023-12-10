package com.sakethh.linkora.btmSheet

import android.app.Activity
import android.content.Intent
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalTextStyle
import androidx.compose.material.Surface
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Link
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.AlertDialogDefaults
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.sakethh.linkora.IntentActivityData
import com.sakethh.linkora.customComposables.AddNewFolderDialogBox
import com.sakethh.linkora.customComposables.AddNewFolderDialogBoxParam
import com.sakethh.linkora.localDB.LocalDataBase
import com.sakethh.linkora.localDB.commonVMs.CreateVM
import com.sakethh.linkora.localDB.commonVMs.DeleteVM
import com.sakethh.linkora.localDB.commonVMs.ReadVM
import com.sakethh.linkora.localDB.commonVMs.UpdateVM
import com.sakethh.linkora.screens.collections.CollectionsScreenVM
import com.sakethh.linkora.screens.collections.specificCollectionScreen.SpecificScreenType
import com.sakethh.linkora.screens.settings.SettingsScreenVM
import com.sakethh.linkora.ui.theme.LinkoraTheme
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch


data class NewLinkBtmSheetUIParam @OptIn(ExperimentalMaterial3Api::class) constructor(
    val inIntentActivity: Boolean,
    val shouldUIBeVisible: MutableState<Boolean>,
    val screenType: SpecificScreenType,
    val btmSheetState: SheetState,
    val onLinkSaveClick: () -> Unit,
    val parentFolderID: Long?,
    val onFolderCreated: () -> Unit
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewLinkBtmSheet(
    newLinkBtmSheetUIParam: NewLinkBtmSheetUIParam
) {
    val isDataExtractingForTheLink = rememberSaveable {
        mutableStateOf(false)
    }
    val inIntentActivity =
        rememberSaveable(inputs = arrayOf(newLinkBtmSheetUIParam.inIntentActivity)) {
            mutableStateOf(newLinkBtmSheetUIParam.inIntentActivity)
        }
    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val activity = context as? Activity
    val intent = activity?.intent
    val intentData = rememberSaveable(inputs = arrayOf(intent)) {
        mutableStateOf(intent)
    }
    val shouldNewFolderDialogBoxAppear = rememberSaveable {
        mutableStateOf(false)
    }
    val folderName = rememberSaveable {
        mutableStateOf("")
    }
    val createVM: CreateVM = viewModel()
    val updateVM: UpdateVM = viewModel()
    val deleteVM: DeleteVM = viewModel()
    val readVM: ReadVM = viewModel()
    LaunchedEffect(key1 = Unit) {
        this.launch {
            awaitAll(async {
                if (inIntentActivity.value) {
                    newLinkBtmSheetUIParam.btmSheetState.show()
                }
                newLinkBtmSheetUIParam.btmSheetState.expand()
            }, async {
                if (inIntentActivity.value) {
                    newLinkBtmSheetUIParam.shouldUIBeVisible.value = true
                }
            }, async {
                if (inIntentActivity.value) {
                    coroutineScope.launch {
                        SettingsScreenVM.Settings.readAllPreferencesValues(context)
                    }.invokeOnCompletion {
                        if (SettingsScreenVM.Settings.isSendCrashReportsEnabled.value) {
                            val firebaseCrashlytics = FirebaseCrashlytics.getInstance()
                            firebaseCrashlytics.setCrashlyticsCollectionEnabled(true)
                        }
                    }
                }
            }, async {
                coroutineScope.launch {
                    if (inIntentActivity.value) {
                        LocalDataBase.localDB = LocalDataBase.getLocalDB(context)
                    }
                }.invokeOnCompletion {
                    coroutineScope.launch {
                        LocalDataBase.localDB.readDao().getAllRootFolders().collect {
                            IntentActivityData.foldersData.value = it
                        }
                    }
                }
            })
        }
    }
    val isAutoDetectTitleEnabled = rememberSaveable {
        mutableStateOf(SettingsScreenVM.Settings.isAutoDetectTitleForLinksEnabled.value)
    }
    LinkoraTheme {
        if (newLinkBtmSheetUIParam.shouldUIBeVisible.value) {
            val noteTextFieldValue = rememberSaveable {
                mutableStateOf("")
            }
            val linkTextFieldValue = if (inIntentActivity.value) {
                rememberSaveable(
                    inputs = arrayOf(
                        intentData.value?.getStringExtra(
                            Intent.EXTRA_TEXT
                        ).toString()
                    )
                ) {
                    mutableStateOf(intentData.value?.getStringExtra(Intent.EXTRA_TEXT).toString())
                }
            } else {
                rememberSaveable {
                    mutableStateOf("")
                }
            }
            val titleTextFieldValue = rememberSaveable {
                mutableStateOf("")
            }
            val selectedFolder = rememberSaveable {
                mutableStateOf("Saved Links")
            }
            ModalBottomSheet(onDismissRequest = {
                if (!isDataExtractingForTheLink.value) {
                    newLinkBtmSheetUIParam.shouldUIBeVisible.value = false
                    coroutineScope.launch {
                        if (newLinkBtmSheetUIParam.btmSheetState.isVisible) {
                            newLinkBtmSheetUIParam.btmSheetState.hide()
                        }
                    }.invokeOnCompletion {
                        if (inIntentActivity.value) {
                            activity?.finishAndRemoveTask()
                        }
                    }
                }
            }, sheetState = newLinkBtmSheetUIParam.btmSheetState) {
                Scaffold(bottomBar = {
                    Surface(
                        color = BottomAppBarDefaults.containerColor,
                        contentColor = contentColorFor(BottomAppBarDefaults.containerColor),
                        modifier = Modifier
                            .background(BottomAppBarDefaults.containerColor)
                            .fillMaxWidth()
                            .wrapContentHeight()
                            .animateContentSize()
                    ) {
                        Column(modifier = Modifier.fillMaxWidth()) {
                            Spacer(modifier = Modifier.requiredHeight(15.dp))
                            Text(
                                text = if (inIntentActivity.value || newLinkBtmSheetUIParam.screenType == SpecificScreenType.ROOT_SCREEN) "Selected folder:" else "Will be saved in:",
                                style = MaterialTheme.typography.titleSmall,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 10.dp)
                            )
                            Spacer(modifier = Modifier.requiredHeight(8.dp))
                            Text(
                                text = if (inIntentActivity.value || newLinkBtmSheetUIParam.screenType == SpecificScreenType.ROOT_SCREEN) selectedFolder.value else folderName.value,
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 20.sp,
                                maxLines = 3,
                                modifier = Modifier
                                    .padding(start = 10.dp)
                                    .fillMaxWidth(0.90f),
                                lineHeight = 24.sp,
                                overflow = TextOverflow.Ellipsis
                            )
                            Divider(
                                thickness = 0.5.dp,
                                modifier = Modifier.padding(15.dp),
                                color = MaterialTheme.colorScheme.outline.copy(0.25f)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentHeight(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (!SettingsScreenVM.Settings.isAutoDetectTitleForLinksEnabled.value) {
                                    Row(
                                        modifier = Modifier.clickable {
                                            if (!isDataExtractingForTheLink.value) {
                                                isAutoDetectTitleEnabled.value =
                                                    !isAutoDetectTitleEnabled.value
                                            }
                                        }, verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        androidx.compose.material3.Checkbox(enabled = !isDataExtractingForTheLink.value,
                                            checked = isAutoDetectTitleEnabled.value,
                                            onCheckedChange = {
                                                isAutoDetectTitleEnabled.value = it
                                            })
                                        Text(
                                            text = "Force Auto-detect title",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontSize = 16.sp
                                        )
                                    }
                                }
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .wrapContentHeight()
                                ) {
                                    Button(modifier = Modifier
                                        .align(Alignment.CenterEnd)
                                        .padding(end = 20.dp), onClick = {}) {
                                        if (isDataExtractingForTheLink.value) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                strokeWidth = 2.5.dp,
                                                color = LocalContentColor.current
                                            )
                                        } else {
                                            Text(
                                                text = "Save",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontSize = 16.sp
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(20.dp))
                        }
                    }
                }) {
                    LazyColumn(
                        modifier = Modifier
                            .padding(it)
                            .fillMaxWidth()
                    ) {
                        item {
                            Text(
                                text = "Save a new link",
                                style = MaterialTheme.typography.titleMedium,
                                fontSize = 24.sp,
                                modifier = Modifier.padding(start = 20.dp)
                            )
                        }
                        item {
                            OutlinedTextField(readOnly = isDataExtractingForTheLink.value,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(
                                        start = 20.dp, end = 20.dp, top = 20.dp
                                    ),
                                label = {
                                    Text(
                                        text = "URL",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontSize = 12.sp
                                    )
                                },
                                textStyle = MaterialTheme.typography.titleSmall,
                                shape = RoundedCornerShape(5.dp),
                                value = linkTextFieldValue.value,
                                onValueChange = {
                                    linkTextFieldValue.value = it
                                })
                        }
                        item {
                            Box(modifier = Modifier.animateContentSize()) {
                                if (!SettingsScreenVM.Settings.isAutoDetectTitleForLinksEnabled.value && !isAutoDetectTitleEnabled.value) {
                                    OutlinedTextField(modifier = Modifier
                                        .padding(
                                            start = 20.dp, end = 20.dp, top = 20.dp
                                        )
                                        .fillMaxWidth(),
                                        readOnly = isDataExtractingForTheLink.value,
                                        label = {
                                            Text(
                                                text = "title of the link you're saving",
                                                style = MaterialTheme.typography.titleSmall,
                                                fontSize = 12.sp
                                            )
                                        },
                                        textStyle = LocalTextStyle.current.copy(lineHeight = 22.sp),
                                        shape = RoundedCornerShape(5.dp),
                                        value = titleTextFieldValue.value,
                                        onValueChange = {
                                            titleTextFieldValue.value = it
                                        })
                                }
                            }
                        }
                        item {
                            OutlinedTextField(readOnly = isDataExtractingForTheLink.value,
                                modifier = Modifier
                                    .padding(
                                        start = 20.dp, end = 20.dp, top = 15.dp
                                    )
                                    .fillMaxWidth(),
                                label = {
                                    Text(
                                        text = "add a note for why you're saving this link",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontSize = 12.sp
                                    )
                                },
                                textStyle = LocalTextStyle.current.copy(lineHeight = 22.sp),
                                shape = RoundedCornerShape(5.dp),
                                value = noteTextFieldValue.value,
                                onValueChange = {
                                    noteTextFieldValue.value = it
                                })
                        }
                        if (SettingsScreenVM.Settings.isAutoDetectTitleForLinksEnabled.value) {
                            item {
                                Card(
                                    border = BorderStroke(
                                        1.dp,
                                        contentColorFor(MaterialTheme.colorScheme.surface)
                                    ),
                                    colors = CardDefaults.cardColors(containerColor = AlertDialogDefaults.containerColor),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 20.dp, end = 20.dp, top = 15.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .wrapContentHeight()
                                            .padding(
                                                top = 10.dp, bottom = 10.dp
                                            ),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Box(
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            androidx.compose.material3.Icon(
                                                imageVector = Icons.Outlined.Info,
                                                contentDescription = null,
                                                modifier = Modifier
                                                    .padding(
                                                        start = 10.dp, end = 10.dp
                                                    )
                                            )
                                        }
                                        Text(
                                            text = "Title will be automatically detected as this setting is enabled.",
                                            style = MaterialTheme.typography.titleSmall,
                                            fontSize = 14.sp,
                                            lineHeight = 18.sp,
                                            textAlign = TextAlign.Start,
                                            modifier = Modifier
                                                .padding(end = 10.dp)
                                        )
                                    }
                                }
                            }
                        }
                        if (inIntentActivity.value || newLinkBtmSheetUIParam.screenType == SpecificScreenType.ROOT_SCREEN) {
                            item {
                                Text(
                                    text = "Save in:",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontSize = 24.sp,
                                    modifier = Modifier.padding(top = 20.dp, start = 20.dp)
                                )
                            }
                            item {
                                Spacer(modifier = Modifier.requiredHeight(20.dp))
                            }
                            item {
                                OutlinedButton(modifier = Modifier.padding(
                                    start = 20.dp, end = 20.dp
                                ), onClick = {
                                    shouldNewFolderDialogBoxAppear.value = true
                                }) {
                                    Text(
                                        text = "Create a new folder",
                                        style = MaterialTheme.typography.titleSmall,
                                        fontSize = 16.sp,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 18.sp,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .fillMaxWidth()
                                    )

                                }

                            }
                            item {
                                Divider(
                                    thickness = 1.dp, modifier = Modifier.padding(
                                        start = 25.dp, top = 20.dp, end = 25.dp
                                    )
                                )
                            }
                            item {
                                SelectableFolderUIComponent(
                                    onClick = { selectedFolder.value = "Saved Links" },
                                    folderName = "Saved Links",
                                    imageVector = Icons.Outlined.Link,
                                    _isComponentSelected = selectedFolder.value == "Saved Links"
                                )
                            }
                            item {
                                SelectableFolderUIComponent(
                                    onClick = { selectedFolder.value = "Important Links" },
                                    folderName = "Important Links",
                                    imageVector = Icons.Outlined.StarOutline,
                                    _isComponentSelected = selectedFolder.value == "Important Links"
                                )
                            }
                            items(IntentActivityData.foldersData.value) {
                                SelectableFolderUIComponent(
                                    onClick = {
                                        selectedFolder.value = it.folderName
                                        CollectionsScreenVM.selectedFolderData.value.id = it.id
                                    },
                                    folderName = it.folderName,
                                    imageVector = Icons.Outlined.Folder,
                                    _isComponentSelected = selectedFolder.value == it.folderName
                                )
                            }
                        }
                        item {
                            Spacer(modifier = Modifier.requiredHeight(20.dp))
                        }
                    }
                }
            }
            AddNewFolderDialogBox(
                AddNewFolderDialogBoxParam(
                    shouldDialogBoxAppear = shouldNewFolderDialogBoxAppear,
                    newFolderData = { folderName, folderID ->
                        selectedFolder.value = folderName
                        CollectionsScreenVM.selectedFolderData.value.id = folderID
                    },
                    onCreated = {
                        newLinkBtmSheetUIParam.onFolderCreated()
                    },
                    parentFolderID = newLinkBtmSheetUIParam.parentFolderID
                )
            )
        }
        BackHandler {
            if (!isDataExtractingForTheLink.value && inIntentActivity.value) {
                newLinkBtmSheetUIParam.shouldUIBeVisible.value = false
                coroutineScope.launch {
                    if (newLinkBtmSheetUIParam.btmSheetState.isVisible) {
                        newLinkBtmSheetUIParam.btmSheetState.hide()
                    }
                }.invokeOnCompletion {
                    if (inIntentActivity.value) {
                        activity?.finishAndRemoveTask()
                    }
                }
            }
        }
    }
}

@Composable
fun SelectableFolderUIComponent(
    onClick: () -> Unit,
    folderName: String,
    imageVector: ImageVector,
    _isComponentSelected: Boolean,
    _forBtmSheetUI: Boolean = false,
) {
    val isComponentSelected = rememberSaveable(inputs = arrayOf(_isComponentSelected)) {
        mutableStateOf(_isComponentSelected)
    }
    val forBtmSheetUI = rememberSaveable(inputs = arrayOf(_forBtmSheetUI)) {
        mutableStateOf(_forBtmSheetUI)
    }
    Column {
        Row(modifier = Modifier
            .clickable {
                onClick()
            }
            .fillMaxWidth()
            .requiredHeight(75.dp)) {
            Box(
                modifier = Modifier.fillMaxHeight(), contentAlignment = Alignment.CenterStart
            ) {
                Icon(
                    tint = if (isComponentSelected.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    imageVector = imageVector,
                    contentDescription = null,
                    modifier = Modifier
                        .padding(
                            start = 20.dp,
                            bottom = 20.dp,
                            end = 20.dp,
                            top = if (forBtmSheetUI.value) 0.dp else 20.dp
                        )
                        .size(28.dp)
                )
            }
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(0.80f),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = folderName,
                    color = if (isComponentSelected.value) MaterialTheme.colorScheme.primary else LocalContentColor.current,
                    style = MaterialTheme.typography.titleSmall,
                    fontSize = 16.sp,
                    lineHeight = 20.sp,
                    maxLines = if (forBtmSheetUI.value) 6 else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            if (isComponentSelected.value) {
                Box(
                    modifier = Modifier
                        .requiredHeight(75.dp)
                        .fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Row {
                        Icon(
                            imageVector = Icons.Filled.CheckCircle,
                            contentDescription = null,
                            modifier = Modifier.size(26.dp),
                            tint = if (isComponentSelected.value) MaterialTheme.colorScheme.primary else LocalContentColor.current
                        )
                        Spacer(modifier = Modifier.width(20.dp))
                    }
                }
            }
        }
        Divider(
            thickness = 1.dp,
            modifier = Modifier.padding(start = 25.dp, end = 25.dp),
            color = MaterialTheme.colorScheme.outline.copy(0.25f)
        )
    }
}