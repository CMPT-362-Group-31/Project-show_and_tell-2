package com.example.project
import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.project.ui.map.FirebaseLatLng
import com.google.android.gms.maps.model.LatLng


object Util {
    const val PERMISSION_REQUEST_CODE = 1001

    // List of required permissions for the app
    private val requiredPermissions = arrayOf(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        android.Manifest.permission.READ_EXTERNAL_STORAGE,
        android.Manifest.permission.ACCESS_FINE_LOCATION,
        android.Manifest.permission.ACCESS_COARSE_LOCATION,
        android.Manifest.permission.FOREGROUND_SERVICE,
        android.Manifest.permission.FOREGROUND_SERVICE_LOCATION,
        android.Manifest.permission.POST_NOTIFICATIONS,
    )

    // Function to check if all permissions are granted
    fun hasAllPermissions(activity: Activity): Boolean {
        return requiredPermissions.all { permission ->
            ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Function to request permissions
    fun requestPermissions(activity: Activity) {
        val missingPermissions = requiredPermissions.filter { permission ->
            ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                activity,
                missingPermissions.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        }
    }

    // Function to handle permission request results
    fun handlePermissionResult(
        requestCode: Int,
        grantResults: IntArray,
        onPermissionsGranted: () -> Unit
    ) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                onPermissionsGranted()
            }
        }
    }
    // Convert FirebaseLatLng to LatLng
    fun FirebaseLatLng.toLatLng(): LatLng {
        return LatLng(latitude, longitude)
    }

    // Convert LatLng to FirebaseLatLng
    fun LatLng.toFirebaseLatLng(): FirebaseLatLng {
        return FirebaseLatLng(latitude, longitude)
    }

    // Convert a list of FirebaseLatLng to a list of LatLng
    fun List<FirebaseLatLng>.toLatLngList(): List<LatLng> {
        return map { it.toLatLng() }
    }

    // Convert a list of LatLng to a list of FirebaseLatLng
    fun List<LatLng>.toFirebaseLatLngList(): List<FirebaseLatLng> {
        return map { it.toFirebaseLatLng() }
    }
}