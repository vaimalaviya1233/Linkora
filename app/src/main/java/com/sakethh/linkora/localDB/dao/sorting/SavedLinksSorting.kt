package com.sakethh.linkora.localDB.dao.sorting

import androidx.room.Dao
import androidx.room.Query
import com.sakethh.linkora.localDB.LinksTable
import kotlinx.coroutines.flow.Flow

@Dao
interface SavedLinksSorting {

    @Query("SELECT * FROM links_table WHERE isLinkedWithSavedLinks=1 ORDER BY title COLLATE NOCASE ASC")
    fun sortByAToZ(): Flow<List<LinksTable>>

    @Query("SELECT * FROM links_table WHERE isLinkedWithSavedLinks=1 ORDER BY title COLLATE NOCASE DESC")
    fun sortByZToA(): Flow<List<LinksTable>>

    @Query("SELECT * FROM links_table WHERE isLinkedWithSavedLinks=1 ORDER BY id COLLATE NOCASE DESC")
    fun sortByLatestToOldest(): Flow<List<LinksTable>>

    @Query("SELECT * FROM links_table WHERE isLinkedWithSavedLinks=1 ORDER BY id COLLATE NOCASE ASC")
    fun sortByOldestToLatest(): Flow<List<LinksTable>>

}