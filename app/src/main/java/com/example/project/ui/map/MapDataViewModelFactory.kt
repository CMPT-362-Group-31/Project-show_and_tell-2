package com.example.project.ui.map

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MapDataViewModelFactory(private val repository: MapDataRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MapDataViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MapDataViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
