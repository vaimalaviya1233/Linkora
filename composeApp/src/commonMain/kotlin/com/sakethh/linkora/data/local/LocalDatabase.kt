package com.sakethh.linkora.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.sakethh.linkora.data.local.dao.LinksDao
import com.sakethh.linkora.domain.model.Folder
import com.sakethh.linkora.domain.model.Link

@Database(
    version = 8,
    exportSchema = true,
    entities = [Link::class, Folder::class]
)
@TypeConverters(TypeConverter::class)
abstract class LocalDatabase : RoomDatabase() {

    companion object {
        const val NAME = "linkora_db"
    }

    abstract val linksDao: LinksDao
}