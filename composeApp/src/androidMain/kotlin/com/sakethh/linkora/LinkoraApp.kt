package com.sakethh.linkora

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.driver.AndroidSQLiteDriver
import com.sakethh.linkora.common.DependencyContainer
import com.sakethh.linkora.common.Localization
import com.sakethh.linkora.common.preferences.AppPreferences
import com.sakethh.linkora.data.local.LinkoraDataStoreName
import com.sakethh.linkora.data.local.LocalDatabase
import com.sakethh.linkora.data.local.createDataStore
import kotlinx.coroutines.Dispatchers

class LinkoraApp : Application() {

    companion object {
        private var localDatabase: LocalDatabase? = null
        private var applicationContext: Context? = null
        fun getLocalDb(): LocalDatabase? {
            return localDatabase
        }

        fun getContext(): Context = applicationContext!!
    }

    override fun onCreate() {
        super.onCreate()
        LinkoraApp.applicationContext = this.applicationContext
        DependencyContainer.dataStorePref = createDataStore {
            applicationContext.filesDir.resolve(LinkoraDataStoreName).absolutePath
        }
        localDatabase = buildLocalDatabase()
        AppPreferences.readAll(DependencyContainer.preferencesRepo.value)
        Localization.loadLocalizedStrings(
            AppPreferences.preferredAppLanguageCode.value
        )
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel =
                NotificationChannel(
                    "1",
                    "Links Refreshing",
                    NotificationManager.IMPORTANCE_HIGH
                )
            notificationChannel.description =
                "Used for notifying about links refreshing status."
            val notificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(notificationChannel)
        }
    }

    private fun buildLocalDatabase(): LocalDatabase {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("DROP TABLE IF EXISTS new_folders_table;")
                db.execSQL("CREATE TABLE IF NOT EXISTS new_folders_table (folderName TEXT NOT NULL, infoForSaving TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);")
                db.execSQL("INSERT INTO new_folders_table (folderName, infoForSaving) SELECT folderName, infoForSaving FROM folders_table;")
                db.execSQL("DROP TABLE IF EXISTS folders_table;")
                db.execSQL("ALTER TABLE new_folders_table RENAME TO folders_table;")

                db.execSQL("DROP TABLE IF EXISTS new_archived_links_table;")
                db.execSQL("CREATE TABLE IF NOT EXISTS new_archived_links_table (title TEXT NOT NULL, webURL TEXT NOT NULL, baseURL TEXT NOT NULL, imgURL TEXT NOT NULL, infoForSaving TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);")
                db.execSQL("INSERT INTO new_archived_links_table (title, webURL, baseURL, imgURL, infoForSaving) SELECT title, webURL, baseURL, imgURL, infoForSaving FROM archived_links_table;")
                db.execSQL("DROP TABLE IF EXISTS archived_links_table;")
                db.execSQL("ALTER TABLE new_archived_links_table RENAME TO archived_links_table;")

                db.execSQL("DROP TABLE IF EXISTS new_archived_folders_table;")
                db.execSQL("CREATE TABLE IF NOT EXISTS new_archived_folders_table (archiveFolderName TEXT NOT NULL, infoForSaving TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);")
                db.execSQL("INSERT INTO new_archived_folders_table (archiveFolderName, infoForSaving) SELECT archiveFolderName, infoForSaving FROM archived_folders_table;")
                db.execSQL("DROP TABLE IF EXISTS archived_folders_table;")
                db.execSQL("ALTER TABLE new_archived_folders_table RENAME TO archived_folders_table;")

                db.execSQL("DROP TABLE IF EXISTS new_important_links_table;")
                db.execSQL("CREATE TABLE IF NOT EXISTS new_important_links_table (title TEXT NOT NULL, webURL TEXT NOT NULL, baseURL TEXT NOT NULL, imgURL TEXT NOT NULL, infoForSaving TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);")
                db.execSQL("INSERT INTO new_important_links_table (title, webURL, baseURL, imgURL, infoForSaving) SELECT title, webURL, baseURL, imgURL, infoForSaving FROM important_links_table;")
                db.execSQL("DROP TABLE IF EXISTS important_links_table;")
                db.execSQL("ALTER TABLE new_important_links_table RENAME TO important_links_table;")

                db.execSQL("DROP TABLE IF EXISTS new_important_folders_table;")
                db.execSQL("CREATE TABLE IF NOT EXISTS new_important_folders_table (impFolderName TEXT NOT NULL, infoForSaving TEXT NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL);")
                db.execSQL("INSERT INTO new_important_folders_table (impFolderName, infoForSaving) SELECT impFolderName, infoForSaving FROM important_folders_table;")
                db.execSQL("DROP TABLE IF EXISTS important_folders_table;")
                db.execSQL("ALTER TABLE new_important_folders_table RENAME TO important_folders_table;")

            }

        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("DROP TABLE IF EXISTS folders_table_new")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `folders_table_new` (`folderName` TEXT NOT NULL, `infoForSaving` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `parentFolderID` INTEGER DEFAULT NULL, `childFolderIDs` TEXT DEFAULT NULL, `isFolderArchived` INTEGER NOT NULL DEFAULT 0, `isMarkedAsImportant` INTEGER NOT NULL DEFAULT 0)"
                )
                db.execSQL(
                    "INSERT INTO folders_table_new (folderName, infoForSaving, id) " + "SELECT folderName, infoForSaving, id FROM folders_table"
                )
                db.execSQL("DROP TABLE folders_table")
                db.execSQL("ALTER TABLE folders_table_new RENAME TO folders_table")


                db.execSQL("DROP TABLE IF EXISTS links_table_new")
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `links_table_new` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `title` TEXT NOT NULL, `webURL` TEXT NOT NULL, `baseURL` TEXT NOT NULL, `imgURL` TEXT NOT NULL, `infoForSaving` TEXT NOT NULL, `isLinkedWithSavedLinks` INTEGER NOT NULL, `isLinkedWithFolders` INTEGER NOT NULL, `keyOfLinkedFolderV10` INTEGER DEFAULT NULL, `keyOfLinkedFolder` TEXT, `isLinkedWithImpFolder` INTEGER NOT NULL, `keyOfImpLinkedFolder` TEXT NOT NULL, `keyOfImpLinkedFolderV10` INTEGER DEFAULT NULL, `isLinkedWithArchivedFolder` INTEGER NOT NULL, `keyOfArchiveLinkedFolderV10` INTEGER DEFAULT NULL, `keyOfArchiveLinkedFolder` TEXT)"
                )
                db.execSQL(
                    "INSERT INTO links_table_new (id, title, webURL, baseURL, imgURL, infoForSaving, " + "isLinkedWithSavedLinks, isLinkedWithFolders, keyOfLinkedFolder, " + "isLinkedWithImpFolder, keyOfImpLinkedFolder, " + "isLinkedWithArchivedFolder, keyOfArchiveLinkedFolder) " + "SELECT id, title, webURL, baseURL, imgURL, infoForSaving, " + "isLinkedWithSavedLinks, isLinkedWithFolders, keyOfLinkedFolder, " + "isLinkedWithImpFolder, keyOfImpLinkedFolder," + "isLinkedWithArchivedFolder, keyOfArchiveLinkedFolder " + "FROM links_table"
                )
                db.execSQL("DROP TABLE links_table")
                db.execSQL("ALTER TABLE links_table_new RENAME TO links_table")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `home_screen_list_table` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `position` INTEGER NOT NULL, `folderName` TEXT NOT NULL, `shouldSavedLinksTabVisible` INTEGER NOT NULL, `shouldImpLinksTabVisible` INTEGER NOT NULL)")
            }
        }
        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("DROP TABLE IF EXISTS home_screen_list_table")
                db.execSQL("CREATE TABLE IF NOT EXISTS `shelf` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `shelfName` TEXT NOT NULL, `shelfIconName` TEXT NOT NULL, `folderIds` TEXT NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS `home_screen_list_table` (`primaryKey` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `id` INTEGER NOT NULL, `position` INTEGER NOT NULL, `folderName` TEXT NOT NULL, `parentShelfID` INTEGER NOT NULL)")
            }
        }
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `language` (`languageCode` TEXT NOT NULL, `languageName` TEXT NOT NULL, `localizedStringsCount` INTEGER NOT NULL, `contributionLink` TEXT NOT NULL, PRIMARY KEY(`languageCode`))")
                db.execSQL("CREATE TABLE IF NOT EXISTS `translation` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `languageCode` TEXT NOT NULL, `stringName` TEXT NOT NULL, `stringValue` TEXT NOT NULL)")
            }
        }
        val MIGRATION_6_7 = object : Migration(6, 7) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS `site_specific_user_agent` (`domain` TEXT NOT NULL, `userAgent` TEXT NOT NULL, `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_site_specific_user_agent_domain` ON `site_specific_user_agent` (`domain`)")
                db.execSQL("ALTER TABLE links_table ADD COLUMN userAgent TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE archived_links_table ADD COLUMN userAgent TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE important_links_table ADD COLUMN userAgent TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE recently_visited_table ADD COLUMN userAgent TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {

                db.execSQL("CREATE TABLE IF NOT EXISTS `panel` (`panelId` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `panelName` TEXT NOT NULL)")

                db.execSQL(
                    """
            INSERT INTO panel (panelId, panelName)
            SELECT id, shelfName FROM shelf
        """.trimIndent()
                )

                db.execSQL("DROP TABLE shelf")

                db.execSQL("CREATE TABLE IF NOT EXISTS `panel_folder` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `folderId` INTEGER NOT NULL, `panelPosition` INTEGER NOT NULL, `folderName` TEXT NOT NULL, `connectedPanelId` INTEGER NOT NULL)")


                db.execSQL(
                    """
            INSERT INTO panel_folder (folderId, panelPosition, folderName, connectedPanelId)
            SELECT id, position, folderName, parentShelfID FROM home_screen_list_table
            """.trimIndent()
                )

                db.execSQL("DROP TABLE home_screen_list_table")
            }
        }

        val dbFile = applicationContext.getDatabasePath("${LocalDatabase.NAME}.db")
        return Room.databaseBuilder(
            applicationContext, LocalDatabase::class.java, name = dbFile.absolutePath
        ).setDriver(AndroidSQLiteDriver()).setQueryCoroutineContext(Dispatchers.IO).addMigrations(
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8
        ).build()
    }
}