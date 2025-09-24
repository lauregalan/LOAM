package com.example.proyectapp.ui.gallery

import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.proyectapp.databinding.FragmentGalleryBinding
import com.example.proyectapp.location.LocationActionListener
import com.example.proyectapp.location.LocationServiceManager
import com.google.android.gms.location.LocationServices
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.database.database
import com.google.firebase.Firebase
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay

// Paso 1: Implementar la interfaz LocationActionListener
class GalleryFragment : Fragment(), LocationActionListener {

    private var _binding: FragmentGalleryBinding? = null
    private val binding get() = _binding!!
    private lateinit var map: MapView

    // La única instancia necesaria del manager de ubicación
    private lateinit var locationServiceManager: LocationServiceManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Configuración del Mapa (OSMDroid) ---
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
        map = binding.map
        map.setMultiTouchControls(true)
        val mapController = map.controller
        mapController.setZoom(15.0)
        mapController.setCenter(GeoPoint(-34.6037, -58.3816)) // Punto de inicio
        enableMyLocationOverlay()

        // --- Configuración de la Lógica de Ubicación ---
        // Paso 2: Inicializar todo aquí, en onViewCreated
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        val database = Firebase.database("https://loam-5a61f-default-rtdb.firebaseio.com")
        val ubicacionesRef = database.getReference("ubicaciones")

        // Inicializamos el manager, pasándole este fragment como contexto y listener
        locationServiceManager = LocationServiceManager(this, fusedLocationClient, ubicacionesRef, this)

        // Paso 3: Asignar el OnClickListener al FAB que está DENTRO de este fragment
        binding.fabRegistrarUbicacion.setOnClickListener {
            locationServiceManager.requestLocationUpdate()
        }
    }

    private fun enableMyLocationOverlay() {
        // Solo muestra la capa de ubicación si ya tenemos permisos
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
            locationOverlay.enableMyLocation()
            locationOverlay.enableFollowLocation()
            map.overlays.add(locationOverlay)
        }
        // Si no hay permisos, LocationServiceManager los pedirá al hacer clic en el FAB
    }

    // Paso 4: Sobrescribir y delegar el resultado de los permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Delegamos el resultado al manager para que se encargue de la lógica
        locationServiceManager.handlePermissionsResult(requestCode, grantResults)
    }

    // --- Paso 5: Implementar los métodos de la interfaz ---
    override fun onLocationRegistered(referencia: String, lat: Double, lon: Double) {
        // Muestra un feedback visual al usuario
        Snackbar.make(binding.root, "Ubicación '$referencia' registrada.", Snackbar.LENGTH_LONG).show()
        Log.d("GalleryFragment", "Ubicación registrada: $referencia ($lat, $lon)")
    }

    override fun onLocationRegistrationFailed(message: String) {
        Snackbar.make(binding.root, "Error: $message", Snackbar.LENGTH_LONG).show()
        Log.e("GalleryFragment", "Fallo al registrar ubicación: $message")
    }

    override fun onPermissionDenied() {
        Snackbar.make(binding.root, "Permiso de ubicación denegado.", Snackbar.LENGTH_LONG).show()
    }

    override fun onGpsDisabled() {
        Snackbar.make(binding.root, "Por favor, activa el GPS para registrar tu ubicación.", Snackbar.LENGTH_LONG).show()
    }

    // --- Paso 6: Manejar el ciclo de vida del mapa y el binding ---
    override fun onResume() {
        super.onResume()
        map.onResume() // Necesario para el mapa de OSMDroid
    }

    override fun onPause() {
        super.onPause()
        map.onPause() // Necesario para el mapa de OSMDroid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null // Limpiar la referencia al binding
    }
}

