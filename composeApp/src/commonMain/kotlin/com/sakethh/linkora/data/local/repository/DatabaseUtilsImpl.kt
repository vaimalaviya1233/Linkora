package com.sakethh.linkora.data.local.repository

import androidx.room.Transactor
import androidx.room.execSQL
import androidx.room.useWriterConnection
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.domain.repository.local.DatabaseUtils

class DatabaseUtilsImpl(private val localDatabase: LocalDatabase) : DatabaseUtils {

    /*
    Initial implementation was to drop everything and create it again,
    The problem with this is UI reactivity, I think Room only watches for
    changes related to data manipulation stuff than the data definition which
    is exactly what I did but well, Room doesn't trigger a new emission based
    on which UI can be updated, so this should do it.

    Now the obvious other way is to reset the state from VMs,
    but this seems just simpler than dealing with all that (at this time)
    * */
    private suspend fun Transactor.clearAllTablesAndResetCounters() {
        execSQL("DELETE FROM `link_tags`")
        execSQL("DELETE FROM `panel_folder`")
        execSQL("DELETE FROM `links`")
        execSQL("DELETE FROM `tags`")
        execSQL("DELETE FROM `panel`")
        execSQL("DELETE FROM `folders`")
        execSQL("DELETE FROM `localized_strings`")
        execSQL("DELETE FROM `localized_languages`")
        execSQL("DELETE FROM `pending_sync_queue`")
        execSQL("DELETE FROM `snapshot`")

        execSQL("DELETE FROM sqlite_sequence WHERE name IN ('links', 'folders', 'localized_strings', 'panel', 'panel_folder', 'pending_sync_queue', 'snapshot', 'tags')")

        /*
        https://stackoverflow.com/questions/27544006/sqlite-database-wal-file-size-keeps-growing#comment132566422_37865221

        that **SQLITE_CHECKPOINT_TRUNCATE** seems to be a constant used in some C function,
        its equivalent keyword is what we use here
        * */
        execSQL("PRAGMA wal_checkpoint(TRUNCATE)")
    }

    override suspend fun resetDatabase() {
        localDatabase.useWriterConnection { transactor ->
            transactor.clearAllTablesAndResetCounters()
        }
    }
}