package com.example.proyectapp

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.Menu
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


class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false


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



}