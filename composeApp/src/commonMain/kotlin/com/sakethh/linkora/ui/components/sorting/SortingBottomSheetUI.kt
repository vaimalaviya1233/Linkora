package com.sakethh.linkora.ui.components.sorting

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.common.utils.rememberLocalizedString
import com.sakethh.linkora.domain.ComposableContent
import com.sakethh.linkora.ui.domain.Sorting
import com.sakethh.linkora.ui.domain.SortingBtmSheetType
import com.sakethh.linkora.ui.utils.genericViewModelFactory
import com.sakethh.linkora.ui.utils.pulsateEffect
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SortingBottomSheetUI(
    sortingBottomSheetParam: SortingBottomSheetParam
) {
    val coroutineScope = rememberCoroutineScope()
    val sortingBtmSheetVM: SortingBtmSheetVM = viewModel(factory = genericViewModelFactory {
        SortingBtmSheetVM(DependencyContainer.preferencesRepo.value)
    })
    LaunchedEffect(key1 = sortingBottomSheetParam.shouldBottomSheetBeVisible.value) {
        if (sortingBottomSheetParam.shouldBottomSheetBeVisible.value) {
            sortingBottomSheetParam.bottomModalSheetState.expand()
        }
    }
    val linksSortingSelectedState = rememberSaveable {
        mutableStateOf(sortingBottomSheetParam.shouldLinksSelectionBeVisible.value)
    }
    val foldersSortingSelectedState = rememberSaveable {
        mutableStateOf(sortingBottomSheetParam.shouldFoldersSelectionBeVisible.value)
    }
    if (sortingBottomSheetParam.shouldBottomSheetBeVisible.value) {
        val didAnyCheckBoxStateChanged = rememberSaveable {
            mutableStateOf(false)
        }
        val sortByUI: ComposableContent = {
            sortingBtmSheetVM.sortingBtmSheetData().forEach {
                Column(
                    modifier = Modifier.combinedClickable(interactionSource = remember {
                        MutableInteractionSource()
                    }, indication = null, onClick = {
                        sortingBottomSheetParam.onSelected(
                            it.sortingType,
                            linksSortingSelectedState.value,
                            foldersSortingSelectedState.value
                        )
                        it.onClick()
                        coroutineScope.launch {
                            sortingBottomSheetParam.bottomModalSheetState.hide()
                            sortingBottomSheetParam.shouldBottomSheetBeVisible.value = false
                        }
                    }, onLongClick = {}).pulsateEffect().fillMaxWidth().wrapContentHeight()
                ) {
                    Row(
                        modifier = Modifier.padding(start = 15.dp).fillMaxWidth()
                            .wrapContentHeight(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Text(
                            text = it.sortingName,
                            fontSize = 16.sp,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (it.sortingType == Sorting.valueOf(
                                    AppPreferences.selectedSortingType.value
                                ) && !didAnyCheckBoxStateChanged.value
                            ) MaterialTheme.colorScheme.primary else LocalTextStyle.current.color
                        )
                        RadioButton(
                            selected = it.sortingType.name == AppPreferences.selectedSortingType.value && !didAnyCheckBoxStateChanged.value,
                            onClick = {
                                sortingBottomSheetParam.onSelected(
                                    it.sortingType,
                                    linksSortingSelectedState.value,
                                    foldersSortingSelectedState.value
                                )
                                it.onClick()
                                coroutineScope.launch {
                                    sortingBottomSheetParam.bottomModalSheetState.hide()
                                    sortingBottomSheetParam.shouldBottomSheetBeVisible.value = false
                                }
                            },
                            modifier = Modifier.padding(end = 5.dp)
                        )
                    }
                }
            }
        }
        ModalBottomSheet(
            sheetState = sortingBottomSheetParam.bottomModalSheetState, onDismissRequest = {
                coroutineScope.launch {
                    sortingBottomSheetParam.bottomModalSheetState.hide()
                }
                sortingBottomSheetParam.shouldBottomSheetBeVisible.value = false
            }) {
            Column(
                modifier = Modifier.animateContentSize()
            ) {
                androidx.compose.material3.Text(
                    text = Localization.Key.SortBy.rememberLocalizedString(),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(start = 15.dp)
                )
                if (sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.REGULAR_FOLDER_SCREEN) {
                    Spacer(modifier = Modifier.height(10.dp))
                }/*if ((sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.REGULAR_FOLDER_SCREEN || sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.ARCHIVE_FOLDER_SCREEN) && sortingBottomSheetParam.shouldFoldersSelectionBeVisible.value) {
                    FolderIndividualComponent(
                        folderName = LocalizedStrings.folders.value,
                        folderNote = "",
                        onMoreIconClick = { },
                        onFolderClick = {
                            didAnyCheckBoxStateChanged.value = true
                        },
                        showMoreIcon = false,
                        showCheckBoxInsteadOfMoreIcon = mutableStateOf(true),
                        checkBoxState = {
                            didAnyCheckBoxStateChanged.value = true
                            foldersSortingSelectedState.value = it
                        },
                        isCheckBoxChecked = foldersSortingSelectedState
                    )
                }
                if ((sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.REGULAR_FOLDER_SCREEN || sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.ARCHIVE_FOLDER_SCREEN) && sortingBottomSheetParam.shouldLinksSelectionBeVisible.value) {
                    FolderIndividualComponent(
                        folderName = LocalizedStrings.links.value,
                        folderNote = "",
                        folderIcon = Icons.Default.Link,
                        onMoreIconClick = { },
                        onFolderClick = {
                            didAnyCheckBoxStateChanged.value = true
                        },
                        showMoreIcon = false,
                        showCheckBoxInsteadOfMoreIcon = mutableStateOf(true),
                        checkBoxState = {
                            linksSortingSelectedState.value = it
                            didAnyCheckBoxStateChanged.value = true
                        },
                        isCheckBoxChecked = linksSortingSelectedState
                    )
                }*/
                if (((sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.REGULAR_FOLDER_SCREEN && foldersSortingSelectedState.value) || (sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.REGULAR_FOLDER_SCREEN && linksSortingSelectedState.value)) || ((sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.ARCHIVE_FOLDER_SCREEN && foldersSortingSelectedState.value) || sortingBottomSheetParam.sortingBtmSheetType == SortingBtmSheetType.ARCHIVE_FOLDER_SCREEN && linksSortingSelectedState.value)) {
                    androidx.compose.material3.Text(
                        text = Localization.Key.SortBy.rememberLocalizedString(),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(start = 15.dp, top = 20.dp)
                    )
                    sortByUI()
                }
                if (sortingBottomSheetParam.sortingBtmSheetType != SortingBtmSheetType.REGULAR_FOLDER_SCREEN && sortingBottomSheetParam.sortingBtmSheetType != SortingBtmSheetType.ARCHIVE_FOLDER_SCREEN) {
                    sortByUI()
                }
                Spacer(
                    modifier = Modifier.navigationBarsPadding()
                )
            }
        }
    }
}