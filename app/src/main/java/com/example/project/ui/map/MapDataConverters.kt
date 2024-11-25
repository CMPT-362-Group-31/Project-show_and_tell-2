package com.example.project.ui.map

import androidx.room.TypeConverter
import com.google.android.gms.maps.model.LatLng
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONArray
import org.json.JSONObject

class MapDataConverters {

    private val gson = Gson()

    // Converter for `LatLng`
    @TypeConverter
    fun fromLatLng(latLng: LatLng): String {
        val jsonObject = JSONObject()
        jsonObject.put("latitude", latLng.latitude)
        jsonObject.put("longitude", latLng.longitude)
        return jsonObject.toString()
    }

    @TypeConverter
    fun toLatLng(json: String): LatLng {
        val jsonObject = JSONObject(json)
        return LatLng(
            jsonObject.getDouble("latitude"),
            jsonObject.getDouble("longitude")
        )
    }

    // Converter for `List<LatLng>`
    @TypeConverter
    fun fromLatLngList(latLngList: List<LatLng>): String {
        val jsonArray = JSONArray()
        latLngList.forEach { latLng ->
            val jsonObject = JSONObject()
            jsonObject.put("latitude", latLng.latitude)
            jsonObject.put("longitude", latLng.longitude)
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toLatLngList(json: String): List<LatLng> {
        val jsonArray = JSONArray(json)
        val latLngList = mutableListOf<LatLng>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            val latLng = LatLng(
                jsonObject.getDouble("latitude"),
                jsonObject.getDouble("longitude")
            )
            latLngList.add(latLng)
        }
        return latLngList
    }

    // Converter for `List<RouteInfo>` using Gson
    @TypeConverter
    fun fromRouteInfoList(routeInfoList: List<RouteInfo>): String {
        return gson.toJson(routeInfoList)
    }

    @TypeConverter
    fun toRouteInfoList(json: String): List<RouteInfo> {
        val type = object : TypeToken<List<RouteInfo>>() {}.type
        return gson.fromJson(json, type)
    }
}
