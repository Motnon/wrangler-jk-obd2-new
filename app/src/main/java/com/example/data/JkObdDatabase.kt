package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.obd.WranglerDtcCatalog

@Database(
    entities = [ScanRecordEntity::class, DtcCodeEntity::class],
    version = 2,
    exportSchema = false
)
abstract class JkObdDatabase : RoomDatabase() {
    abstract fun scanRecordDao(): ScanRecordDao
    abstract fun dtcCodeDao(): DtcCodeDao

    companion object {
        @Volatile
        private var instance: JkObdDatabase? = null

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """
                    CREATE TABLE IF NOT EXISTS `dtc_codes` (
                        `code` TEXT NOT NULL,
                        `title` TEXT NOT NULL,
                        `subsystemName` TEXT NOT NULL,
                        `severityName` TEXT NOT NULL,
                        `affectedEngines` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `trailGuidance` TEXT NOT NULL,
                        `supportsJk` INTEGER NOT NULL,
                        `supportsJl` INTEGER NOT NULL,
                        PRIMARY KEY(`code`)
                    )
                    """.trimIndent()
                )
                prepopulateDtcTable(db)
            }
        }

        private val prepopulateCallback = object : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                prepopulateDtcTable(db)
            }
        }

        internal fun prepopulateDtcTable(db: SupportSQLiteDatabase) {
            val sql = """
                INSERT OR REPLACE INTO `dtc_codes` (
                    `code`, `title`, `subsystemName`, `severityName`,
                    `affectedEngines`, `description`, `trailGuidance`,
                    `supportsJk`, `supportsJl`
                ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
            """.trimIndent()
            val stmt = db.compileStatement(sql)
            db.beginTransaction()
            try {
                for (info in WranglerDtcCatalog.allCodes) {
                    val entity = DtcCodeEntity.fromDomain(info)
                    stmt.clearBindings()
                    stmt.bindString(1, entity.code)
                    stmt.bindString(2, entity.title)
                    stmt.bindString(3, entity.subsystemName)
                    stmt.bindString(4, entity.severityName)
                    stmt.bindString(5, entity.affectedEngines)
                    stmt.bindString(6, entity.description)
                    stmt.bindString(7, entity.trailGuidance)
                    stmt.bindLong(8, if (entity.supportsJk) 1L else 0L)
                    stmt.bindLong(9, if (entity.supportsJl) 1L else 0L)
                    stmt.executeInsert()
                }
                db.setTransactionSuccessful()
            } finally {
                db.endTransaction()
                stmt.close()
            }
        }

        fun getInstance(context: Context): JkObdDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    JkObdDatabase::class.java,
                    "wrangler_jk_obd.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(prepopulateCallback)
                    .build()
                    .also { instance = it }
            }
        }
    }
}
