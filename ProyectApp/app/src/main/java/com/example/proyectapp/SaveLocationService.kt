// app/src/main/java/com/example/proyectapp/LocationServiceManager.kt

package com.example.proyectapp

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
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.firebase.database.DatabaseReference
import java.text.SimpleDateFormat
import java.util.*

// 1. Interfaz de Callback para comunicar eventos a la Activity/Fragment
interface LocationActionListener {
    fun onLocationRegistered(referencia: String, lat: Double, lon: Double)
    fun onLocationRegistrationFailed(message: String)
    fun onPermissionDenied()
    fun onGpsDisabled()
}

class LocationServiceManager(
    private val activity: Activity, // Necesita una Activity para solicitar permisos y mostrar diálogos
    private val fusedLocationClient: FusedLocationProviderClient,
    private val ubicacionesRef: DatabaseReference, // Referencia a la base de datos Firebase
    private val listener: LocationActionListener // Objeto que implementa la interfaz de callback
) {

    companion object { // companion object para constantes estáticas
        const val PERMISSION_REQUEST_CODE = 1001
        private const val TAG = "LocationServiceManager" // Etiqueta para logs
    }

    // Función principal para iniciar el proceso de registro de ubicación
    fun requestLocationUpdate() {
        checkLocationPermissionsAndGPS()
    }

    // 1. Verifica si los permisos de ubicación están concedidos o los solicita
    private fun checkLocationPermissionsAndGPS() {
        if (ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                activity,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Permisos no concedidos, solicitarlos al usuario
            ActivityCompat.requestPermissions(
                activity,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                PERMISSION_REQUEST_CODE
            )
            return
        }

        // Si los permisos están concedidos, verificar el estado del GPS
        checkGPSStatus()
    }

    // 2. Maneja el resultado de la solicitud de permisos (llamado desde la Activity)
    fun handlePermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permiso concedido, ahora verificar el GPS
                checkGPSStatus()
            } else {
                // Permiso denegado
                Toast.makeText(activity, "Permiso de ubicación denegado. No se puede registrar la ubicación.", Toast.LENGTH_SHORT).show()
                listener.onPermissionDenied() // Informar a la Activity
            }
        }
    }

    // 3. Verifica el estado del GPS y, si está desactivado, muestra una alerta
    private fun checkGPSStatus() {
        val locationManager = activity.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            showGPSDisabledAlert()
        } else {
            // GPS activo, obtener ubicación
            getLocation()
        }
    }

    private fun showGPSDisabledAlert() {
        AlertDialog.Builder(activity)
            .setTitle("GPS Desactivado")
            .setMessage("Tu GPS está desactivado. ¿Deseas activarlo ahora para registrar tu ubicación?")
            .setPositiveButton("Sí") { dialog, _ ->
                activity.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                dialog.dismiss()
            }
            .setNegativeButton("No") { dialog, _ ->
                Toast.makeText(activity, "No se puede registrar la ubicación sin GPS.", Toast.LENGTH_SHORT).show()
                listener.onGpsDisabled() // Informar a la Activity
                dialog.dismiss()
            }
            .setCancelable(false)
            .show()
    }

    // 4. Obtiene la última ubicación conocida y luego pide la referencia
    @SuppressLint("MissingPermission") // La verificación de permisos se realiza en checkLocationPermissionsAndGPS
    private fun getLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    showReferenceDialog(location)
                } else {
                    val message = "No se pudo obtener la última ubicación conocida. Intenta de nuevo."
                    Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                    Log.w(TAG, message)
                    listener.onLocationRegistrationFailed(message)
                }
            }
            .addOnFailureListener { e ->
                val message = "Error al obtener ubicación: ${e.message}"
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                Log.e(TAG, message, e)
                listener.onLocationRegistrationFailed(message)
            }
    }

    // 5. Muestra un diálogo para que el usuario ingrese una referencia para la ubicación
    private fun showReferenceDialog(location: Location) {
        val builder = AlertDialog.Builder(activity)
        builder.setTitle("Referencia de la ubicación")
        builder.setMessage("Ingresa una referencia para esta ubicación (ej: Casa, Trabajo, Punto de Interés).")

        val input = EditText(activity)
        input.hint = "Referencia"
        builder.setView(input)

        builder.setPositiveButton("Guardar") { dialog, _ ->
            val referencia = input.text.toString().trim()
            if (referencia.isNotEmpty()) {
                saveLocationWithReference(referencia, location)
            } else {
                val message = "La referencia no puede estar vacía."
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
                listener.onLocationRegistrationFailed(message)
            }
            dialog.dismiss()
        }
        builder.setNegativeButton("Cancelar") { dialog, _ ->
            dialog.cancel()
            val message = "Registro de ubicación cancelado."
            Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            listener.onLocationRegistrationFailed(message)
        }
        builder.show()
    }

    // 6. Guarda la ubicación y su referencia en Firebase Realtime Database
    private fun saveLocationWithReference(referencia: String, location: Location) {
        val latitud = location.latitude
        val longitud = location.longitude
        val timestamp = System.currentTimeMillis()

        val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val fechaHora = sdf.format(Date(timestamp))

        val ubicacionData = mapOf(
            "referencia" to referencia,
            "latitud" to latitud,
            "longitud" to longitud,
            "timestamp" to timestamp,
            "fechaHora" to fechaHora
        )

        ubicacionesRef.push().setValue(ubicacionData)
            .addOnSuccessListener {
                Toast.makeText(activity, "Ubicación con referencia '$referencia' registrada con éxito en Firebase!", Toast.LENGTH_LONG).show()
                listener.onLocationRegistered(referencia, latitud, longitud) // Informar a la Activity
            }
            .addOnFailureListener { e ->
                val message = "Error al registrar ubicación en Firebase: ${e.message}"
                Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
                Log.e(TAG, message, e)
                listener.onLocationRegistrationFailed(message)
            }
    }
}
