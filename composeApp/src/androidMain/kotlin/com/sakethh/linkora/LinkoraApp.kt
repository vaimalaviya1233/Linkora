package com.sakethh.linkora

import android.app.Application
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.sakethh.linkora.data.local.LocalDatabase
import kotlinx.coroutines.Dispatchers

class LinkoraApp : Application() {

    companion object {
        private var localDatabase: LocalDatabase? = null

        fun getLocalDb(): LocalDatabase? {
            return localDatabase
        }
    }

    override fun onCreate() {
        super.onCreate()
        val dbFile = applicationContext.getDatabasePath("${LocalDatabase.NAME}.db")
        localDatabase = Room.databaseBuilder(
            applicationContext, LocalDatabase::class.java, name = dbFile.absolutePath
        ).setDriver(BundledSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).build()
    }
}