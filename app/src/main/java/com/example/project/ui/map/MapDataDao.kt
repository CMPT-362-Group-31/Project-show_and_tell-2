package com.example.project.ui.map

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MapDataDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMapData(mapData: MapData)

    @Query("SELECT * FROM MapData_table")
    fun getAllMapData(): Flow<List<MapData>>

    @Query("DELETE FROM MapData_table")
    suspend fun deleteAllMapData()

    @Query("SELECT * FROM MapData_table WHERE id = :id")
    suspend fun getMapDataById(id: Long): MapData?

    @Query("DELETE FROM MapData_table WHERE id = :id")
    suspend fun deleteMapDataById(id: Long)
}
