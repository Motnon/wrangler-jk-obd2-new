package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [ScanRecordEntity::class], version = 1, exportSchema = false)
abstract class JkObdDatabase : RoomDatabase() {
    abstract fun scanRecordDao(): ScanRecordDao

    companion object {
        @Volatile
        private var instance: JkObdDatabase? = null

        fun getInstance(context: Context): JkObdDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    JkObdDatabase::class.java,
                    "wrangler_jk_obd.db"
                ).build().also { instance = it }
            }
        }
    }
}
