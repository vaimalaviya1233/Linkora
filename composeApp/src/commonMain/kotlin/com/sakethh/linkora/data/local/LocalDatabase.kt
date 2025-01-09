package com.sakethh.linkora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sakethh.linkora.data.local.dao.FoldersDao
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.data.local.dao.LocalizationDao
import com.sakethh.linkora.data.local.dao.sorting.FoldersSortingDao
import com.sakethh.linkora.data.local.dao.sorting.LinksSortingDao
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.link.Link
import com.sakethh.linkora.domain.model.localization.LocalizedLanguage
import com.sakethh.linkora.domain.model.localization.LocalizedString

@Database(
    version = 8,
    exportSchema = true,
    entities = [Link::class, Folder::class, LocalizedString::class, LocalizedLanguage::class]
)
@TypeConverters(TypeConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    companion object {
        const val NAME = "linkora_db"
    }

    abstract val linksDao: LinksDao
    abstract val linksSortingDao: LinksSortingDao
    abstract val foldersDao: FoldersDao
    abstract val foldersSortingDao: FoldersSortingDao
    abstract val localizationDao: LocalizationDao
}