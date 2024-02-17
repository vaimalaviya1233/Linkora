package com.sakethh.linkora.screens.collections

import android.content.Context
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sakethh.linkora.localDB.LocalDataBase
import com.sakethh.linkora.localDB.dto.FoldersTable
import com.sakethh.linkora.screens.settings.SettingsScreenVM
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

open class CollectionsScreenVM : ViewModel() {
    private val _foldersData = MutableStateFlow(
        emptyList<FoldersTable>()
    )
    val foldersData = _foldersData.asStateFlow()

    val selectedFoldersData = mutableStateListOf<FoldersTable>()
    val areAllFoldersChecked = mutableStateOf(false)

    fun changeAllFoldersSelectedData(
        folderComponent: List<FoldersTable> = emptyList()
    ) {
        if (areAllFoldersChecked.value) {
            selectedFoldersData.addAll(foldersData.value.map { it })
        } else {
            if (folderComponent.isEmpty()) {
                selectedFoldersData.removeAll(foldersData.value.map { it })
            } else {
                selectedFoldersData.removeAll(
                    folderComponent.map { it }
                )
            }
        }
    }

    open fun onDeleteMultipleFolders() {
        viewModelScope.launch(Dispatchers.IO) {
            LocalDataBase.localDB.deleteDao()
                .deleteMultipleFolders(selectedFoldersData.toList().toTypedArray())
        }
    }

    fun archiveMultipleFolders() {
        viewModelScope.launch {
            selectedFoldersData.forEach {
                LocalDataBase.localDB.updateDao().moveAFolderToArchivesV10(it.id)
            }
        }
    }

    companion object {
        var rootFolderID: Long = 0
        val selectedFolderData = mutableStateOf(
            FoldersTable(
                folderName = "",
                infoForSaving = "",
                parentFolderID = 0
            )
        )
        val currentClickedFolderData = mutableStateOf(
            FoldersTable(
                folderName = "",
                infoForSaving = "",
                parentFolderID = 0
            )
        )
    }

    init {
        changeRetrievedFoldersData(
            sortingPreferences = SettingsScreenVM.SortingPreferences.valueOf(
                SettingsScreenVM.Settings.selectedSortingType.value
            )
        )
    }

    fun changeRetrievedFoldersData(sortingPreferences: SettingsScreenVM.SortingPreferences) {
        when (sortingPreferences) {
            SettingsScreenVM.SortingPreferences.A_TO_Z -> {
                viewModelScope.launch {
                    LocalDataBase.localDB.regularFolderSorting().sortByAToZ()
                        .collect {
                            val mutableBooleanList = mutableListOf<MutableState<Boolean>>()
                            List(it.size) { index ->
                                mutableBooleanList.add(index, mutableStateOf(false))
                            }
                            _foldersData.emit(
                                it
                            )
                        }
                }
            }

            SettingsScreenVM.SortingPreferences.Z_TO_A -> {
                viewModelScope.launch {
                    LocalDataBase.localDB.regularFolderSorting().sortByZToA()
                        .collect {
                            val mutableBooleanList = mutableListOf<MutableState<Boolean>>()
                            List(it.size) { index ->
                                mutableBooleanList.add(index, mutableStateOf(false))
                            }
                            _foldersData.emit(
                                it
                            )
                        }
                }
            }

            SettingsScreenVM.SortingPreferences.NEW_TO_OLD -> {
                viewModelScope.launch {
                    LocalDataBase.localDB.regularFolderSorting()
                        .sortByLatestToOldest()
                        .collect {
                            val mutableBooleanList = mutableListOf<MutableState<Boolean>>()
                            List(it.size) { index ->
                                mutableBooleanList.add(index, mutableStateOf(false))
                            }
                            _foldersData.emit(
                                it
                            )
                        }
                }
            }

            SettingsScreenVM.SortingPreferences.OLD_TO_NEW -> {
                viewModelScope.launch {
                    LocalDataBase.localDB.regularFolderSorting()
                        .sortByOldestToLatest()
                        .collect {
                            val mutableBooleanList = mutableListOf<MutableState<Boolean>>()
                            List(it.size) { index ->
                                mutableBooleanList.add(index, mutableStateOf(false))
                            }
                            _foldersData.emit(
                                it
                            )
                        }
                }
            }
        }
    }

    fun onNoteDeleteClick(context: Context, clickedFolderID: Long) {
        viewModelScope.launch {
            LocalDataBase.localDB.deleteDao()
                .deleteAFolderNote(folderID = clickedFolderID)
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "deleted the note", Toast.LENGTH_SHORT).show()
            }
        }
    }

}