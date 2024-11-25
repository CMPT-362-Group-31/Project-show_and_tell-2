package com.example.project.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MapDataViewModel(private val repository: MapDataRepository) : ViewModel() {

    fun insertMapData(mapData: MapData) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.insertMapData(mapData)
        }
    }

    fun deleteMapDataById(mapId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteMapDataById(mapId)
        }
    }

    suspend fun getMapDataById(mapId: Long): MapData? {
        return repository.getMapDataById(mapId)
    }
}
