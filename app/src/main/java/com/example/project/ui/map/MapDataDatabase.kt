package com.example.project.ui.map

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MapData::class], version = 2, exportSchema = false)
abstract class MapDataDatabase : RoomDatabase() {
    abstract fun mapDataDao(): MapDataDao

    companion object {
        @Volatile
        private var INSTANCE: MapDataDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Add migration logic if necessary. Example:
                // database.execSQL("ALTER TABLE MapData_table ADD COLUMN newColumn TEXT")
                // If no changes are required, leave this empty.
            }
        }

        fun getDatabase(context: Context): MapDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MapDataDatabase::class.java,
                    "MapData_database"
                )
                    .addMigrations(MIGRATION_1_2) // For preserving data
                    // .fallbackToDestructiveMigration() // Uncomment for development/testing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
