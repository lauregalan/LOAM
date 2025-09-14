package com.example.proyectapp.ui.gallery

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyectapp.databinding.FragmentGalleryBinding
//imports para el mapa

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class GalleryFragment : Fragment() {

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private lateinit var map: MapView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val galleryViewModel =
            ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root

        /*val textView: TextView = binding.textGallery
        galleryViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }*/
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa configuración de OSMDroid
        Configuration.getInstance().load(requireContext(), requireContext().getSharedPreferences("osmdroid", Context.MODE_PRIVATE))

        map = binding.map
        map.setMultiTouchControls(true)

        val mapController = map.controller
        mapController.setZoom(15.0)

        val startPoint = GeoPoint(-34.6037, -58.3816)
        mapController.setCenter(startPoint)

        habilitarMiUbicacion()
    }

    private fun habilitarMiUbicacion() {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(requireContext()), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        map.overlays.add(locationOverlay)
    }

    // Cuando el usuario acepte permisos
    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            habilitarMiUbicacion()
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}