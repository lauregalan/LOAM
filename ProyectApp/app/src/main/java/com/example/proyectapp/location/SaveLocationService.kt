package com.example.proyectapp.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.provider.Settings
import android.util.Log
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*

interface LocationActionListener {
    fun onLocationRegistered(referencia: String, lat: Double, lon: Double)
    fun onLocationRegistrationFailed(message: String)
    fun onPermissionDenied()
    fun onGpsDisabled()
}

class LocationServiceManager(
    private val fragment: Fragment, // Usamos el Fragment para permisos y diálogos
    private val fusedLocationClient: FusedLocationProviderClient,
    private val ubicacionesRef: DatabaseReference,
    private val listener: LocationActionListener
) {

    private val context: Context = fragment.requireContext()
    private val activity: Activity = fragment.requireActivity()

    companion object {
        const val PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "LocationServiceManager"
    }

    fun requestLocationUpdate() {
        if (!checkLocationPermissions()) {
            requestLocationPermissions()
        } else if (!isGpsEnabled()) {
            showGPSDisabledAlert()
        } else {
            fetchLastLocation()
        }
    }

    private fun checkLocationPermissions(): Boolean {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermissions() {
        fragment.requestPermissions(
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            PERMISSION_REQUEST_CODE
        )
    }

    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocationUpdate() // Permiso concedido, reintentar
            } else {
                Toast.makeText(context, "Permiso de ubicación denegado.", Toast.LENGTH_SHORT).show()
                listener.onPermissionDenied()
            }
        }
    }

    private fun isGpsEnabled(): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun showGPSDisabledAlert() {
        AlertDialog.Builder(context)
            .setTitle("GPS Desactivado")
            .setMessage("Tu GPS está desactivado. ¿Deseas activarlo?")
            .setPositiveButton("Activar") { dialog, _ ->
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                Toast.makeText(context, "No se puede registrar ubicación sin GPS.", Toast.LENGTH_SHORT).show()
                listener.onGpsDisabled()
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    @SuppressLint("MissingPermission")
    private fun fetchLastLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    showReferenceDialog(location)
                } else {
                    val message = "No se pudo obtener la ubicación. Activa la ubicación e inténtalo de nuevo."
                    Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                    Log.w(TAG, message)
                    listener.onLocationRegistrationFailed(message)
                }
            }
            .addOnFailureListener { e ->
                val message = "Error al obtener ubicación: ${e.message}"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                Log.e(TAG, message, e)
                listener.onLocationRegistrationFailed(message)
            }
    }

    private fun showReferenceDialog(location: Location) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Referencia de la ubicación")
        builder.setMessage("Ingresa una referencia para esta ubicación (ej: Casa, Trabajo).")

        val input = EditText(context)
        input.hint = "Escribe aquí"
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val referencia = input.text.toString().trim()
            if (referencia.isNotEmpty()) {
                saveLocationToFirebase(referencia, location)
            } else {
                Toast.makeText(context, "La referencia no puede estar vacía.", Toast.LENGTH_SHORT).show()
            }
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
        }
        builder.show()
    }

    private fun saveLocationToFirebase(referencia: String, location: Location) {
        val ubicacionData = mapOf(
            "referencia" to referencia,
            "latitud" to location.latitude,
            "longitud" to location.longitude,
            "timestamp" to System.currentTimeMillis(),
            "fechaHora" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())
        )

        ubicacionesRef.push().setValue(ubicacionData)
            .addOnSuccessListener {
                Toast.makeText(context, "Ubicación '$referencia' registrada con éxito.", Toast.LENGTH_LONG).show()
                listener.onLocationRegistered(referencia, location.latitude, location.longitude)
            }
            .addOnFailureListener { e ->
                val message = "Error al guardar en Firebase: ${e.message}"
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                Log.e(TAG, message, e)
                listener.onLocationRegistrationFailed(message)
            }
    }
}
