package com.example.project.ui.map

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch

class MapDataRepository(private val mapDataDao: MapDataDao) {

    suspend fun insertMapData(mapData: MapData) {
        CoroutineScope(IO).launch {
            mapDataDao.insertMapData(mapData)
        }
    }

    suspend fun deleteMapDataById(mapId: Long) {
        CoroutineScope(IO).launch {
            mapDataDao.deleteMapDataById(mapId)
        }
    }

    suspend fun getMapDataById(mapId: Long): MapData? {
        return mapDataDao.getMapDataById(mapId)
    }
}

