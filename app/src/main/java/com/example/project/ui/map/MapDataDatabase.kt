package com.example.project.ui.map

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MapData::class], version = 3, exportSchema = false)
@TypeConverters(MapDataConverters::class)
abstract class MapDataDatabase : RoomDatabase() {
    abstract fun mapDataDao(): MapDataDao

    companion object {
        @Volatile
        private var INSTANCE: MapDataDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Example migration logic for version 1 to 2
                // You can leave this empty if no changes were made in version 2
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "ALTER TABLE MapData_table ADD COLUMN another_column_name INTEGER NOT NULL DEFAULT 0"
                )
            }
        }

        fun getDatabase(context: Context): MapDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MapDataDatabase::class.java,
                    "MapData_database"
                )
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3) // Add all migrations
                    // .fallbackToDestructiveMigration() // Uncomment for development/testing
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
