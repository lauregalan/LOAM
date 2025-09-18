package com.example.proyectapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.Menu
import android.widget.Toast
import androidx.annotation.OptIn
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.example.proyectapp.databinding.ActivityMainBinding
import androidx.core.net.toUri
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.database.database


class MainActivity : AppCompatActivity(), LocationActionListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationServiceManager: LocationServiceManager

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar CameraManager
        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        cameraId = cameraManager.cameraIdList.first {
            cameraManager.getCameraCharacteristics(it)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        setSupportActionBar(binding.appBarMain.toolbar)

        //aca el manejo de las acciones del boton flotante
        binding.appBarMain.fab.setOnClickListener {

            //Toast.makeText(this, "Abriendo marcador...", Toast.LENGTH_SHORT).show()

            //irMarcadorTelefono()
            val intent = Intent(this, HelloArActivity::class.java)
            startActivity(intent)
        }

        //manejo de las acciones para el flash

        binding.appBarMain.fab2.setOnClickListener {
            if (isFlashOn) {
                apagarFlash()
            } else {
                encenderFlash()
            }
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        val firebaseDatabase = Firebase.database("https://loam-5a61f-default-rtdb.firebaseio.com")
        val ubicacionesRef = firebaseDatabase.getReference("ubicaciones")

        // Crear una instancia de LocationServiceManager, pasando 'this' (la Activity)
        // como Context y también como el listener para los callbacks.
        locationServiceManager = LocationServiceManager(this, fusedLocationClient, ubicacionesRef, this)

        // Asignar el OnClickListener al FAB de ubicación, delegando la acción
        val fabRegistrarUbicacion: FloatingActionButton = binding.appBarMain.fabRegistrarUbicacion
        fabRegistrarUbicacion.setOnClickListener {
            locationServiceManager.requestLocationUpdate()
        }

        val fabChat: FloatingActionButton = binding.root.findViewById(R.id.fab_chat)

        fabChat.setOnClickListener { view ->
            // Navegar al ChatFragment usando el NavController
            findNavController(R.id.nav_host_fragment_content_main).navigate(R.id.chatFragment)
            Snackbar.make(view, "Abriendo chat con especialista...", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show()
        }

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)

        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.


        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow, R.id.chatFragment
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

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // Delegar el resultado al LocationServiceManager
        locationServiceManager.handlePermissionsResult(requestCode, grantResults)
    }
    // ----------------------------------------------------------

    // ----- Implementación de los métodos de la interfaz LocationActionListener -----
    @OptIn(UnstableApi::class)
    override fun onLocationRegistered(referencia: String, lat: Double, lon: Double) {
        // Aquí puedes realizar acciones en la UI de tu MainActivity,
        // por ejemplo, mostrar un Snackbar más visible o actualizar un TextView.
        Snackbar.make(binding.root, "Ubicación de '$referencia' registrada con éxito: ($lat, $lon)", Snackbar.LENGTH_LONG).show()
        Log.d("MainActivity", "Ubicación registrada: $referencia ($lat, $lon)")
    }

    @OptIn(UnstableApi::class)
    override fun onLocationRegistrationFailed(message: String) {
        // Manejar errores específicos si es necesario en la UI de la Activity.
        // El Toast ya se muestra desde LocationServiceManager, pero aquí podrías añadir más logs o UI.
        Snackbar.make(binding.root, "Fallo al registrar ubicación: $message", Snackbar.LENGTH_LONG).show()
        Log.e("MainActivity", "Error al registrar ubicación: $message")
    }

    @OptIn(UnstableApi::class)
    override fun onPermissionDenied() {
        // Acciones específicas si los permisos son denegados (ej. deshabilitar un botón)
        Snackbar.make(binding.root, "Permiso de ubicación denegado. Función limitada.", Snackbar.LENGTH_LONG).show()
        Log.w("MainActivity", "Permiso de ubicación denegado.")
    }

    @OptIn(UnstableApi::class)
    override fun onGpsDisabled() {
        // Acciones específicas si el GPS está desactivado
        Snackbar.make(binding.root, "GPS está desactivado. Habilítalo para registrar ubicación.", Snackbar.LENGTH_LONG).show()
        Log.w("MainActivity", "GPS desactivado.")
    }
}