package com.example.proyectapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.widget.Button
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectapp.databinding.ActivityMainBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import androidx.core.net.toUri
import androidx.lifecycle.ReportFragment.Companion.reportFragment
import android.Manifest
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import org.osmdroid.api.IMapController
import org.osmdroid.config.Configuration
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false
    private lateinit var map: MapView

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar CameraManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.first {
            cameraManager.getCameraCharacteristics(it)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        val dolarApi = RetrofitHelper.getInstance().create(DolarApi::class.java)


        // de una corrutina, que es un bloque asíncrono que maneja la pausa y reanudación.
        GlobalScope.launch {
            val result = dolarApi.getDolarBlue() // request al endpoint
            if (result != null)

                //binding.appBarMain.precioDolar.text = "Precio del dolar blue 💶: " + result.compra.toString();
                Log.d("DOLAR BLUE: ", result.toString())

        }

        
        setSupportActionBar(binding.appBarMain.toolbar)


        //aca el manejo de las acciones del boton flotante
        binding.appBarMain.fab.setOnClickListener {

            Toast.makeText(this, "Abriendo marcador...", Toast.LENGTH_SHORT).show()

            irMarcadorTelefono()
        }

        //manejo de las acciones para el flash

        binding.appBarMain.fab2.setOnClickListener {
            if (isFlashOn) {
                apagarFlash()
            } else {
                encenderFlash()
            }
        }

        //cosas del mapa

        // Inicializa configuración de OSMDroid
        Configuration.getInstance().load(this, getSharedPreferences("osmdroid", MODE_PRIVATE))

        map = findViewById(R.id.map)
        map.setMultiTouchControls(true)

        // Controlador del mapa
        val mapController: IMapController = map.controller
        mapController.setZoom(15.0)

        // Punto inicial (si aún no tenemos GPS, ponemos algo por defecto, ej: Buenos Aires)
        val startPoint = GeoPoint(-34.6037, -58.3816)
        mapController.setCenter(startPoint)

        // Verificar permisos de localización
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 1)
        } else {
            habilitarMiUbicacion()
        }


        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.

        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun encenderFlash() {
        cameraId?.let {
            cameraManager.setTorchMode(it, true)
            isFlashOn = true
        }
    }

    private fun apagarFlash() {
        cameraId?.let {
            cameraManager.setTorchMode(it, false)
            isFlashOn = false
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    private fun irMarcadorTelefono(){
        val intent = Intent(Intent.ACTION_DIAL)
        intent.data = "tel:2302354597".toUri() //identificador de recurso, aca tel
        startActivity(intent)
    }



    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }


    private fun habilitarMiUbicacion() {
        val locationOverlay = MyLocationNewOverlay(GpsMyLocationProvider(this), map)
        locationOverlay.enableMyLocation()
        locationOverlay.enableFollowLocation()
        map.overlays.add(locationOverlay)
    }

    // Verificar permisos de localización


    // Cuando el usuario acepte permisos
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            habilitarMiUbicacion()
        }
    }

}