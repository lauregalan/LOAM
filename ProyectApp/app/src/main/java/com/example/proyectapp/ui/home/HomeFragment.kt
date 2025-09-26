package com.example.proyectapp.ui.home

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyectapp.R
import com.example.proyectapp.dolar.RetrofitHelper
import com.example.proyectapp.databinding.FragmentHomeBinding
import com.example.proyectapp.dolar.DolarApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    // Registramos el launcher
    /*
    private val batteryReceiver = object : BroadcastReceiver() { //escuchamos eventos del sistema
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) // el sistema envia cada vez q cambia la bateria
            val batteryPct = level

            Toast.makeText(requireContext(), batteryPct, Toast.LENGTH_SHORT).show()

        }
    */

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Asigna las acciones a cada widget del dashboard
        setupDashboardListeners()

        // Obtiene el precio del dólar de la API
        obtenerPrecioDolar()
    }

    private fun setupDashboardListeners() {
        val navController = findNavController()

        binding.cardChat.setOnClickListener {
            // Asegúrate de que el id 'nav_chat' exista en tu grafo de navegación
            navController.navigate(R.id.nav_chat)
        }

        binding.cardGallery.setOnClickListener {
            // Navega al fragment del mapa (Gallery)
            navController.navigate(R.id.nav_gallery)
        }

        binding.cardSlideshow.setOnClickListener {
            // Navega al fragment de multimedia (Slideshow)
            navController.navigate(R.id.nav_slideshow)
        }

        binding.callEngineeringCouncilButton.setOnClickListener {
            // Llama a la función para abrir el marcador del teléfono
            irMarcadorTelefono()
        }
    }
    /*
    override fun onResume() {
        super.onResume()
        val intentFilter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        requireContext().registerReceiver(batteryReceiver, intentFilter)

    }*/
    /*
    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(batteryReceiver)
    }*/

    @SuppressLint("SetTextI18n")
    private fun obtenerPrecioDolar() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    RetrofitHelper.getInstance().create(DolarApi::class.java).getDolarBlue()
                }

                if (result != null) {
                    binding.precioDolar.text = "$${result.compra}"
                    Log.d("DOLAR_BLUE", "Precio de compra: ${result.compra}")
                } else {
                    binding.precioDolar.text = "N/A"
                }
            } catch (e: IOException) {
                binding.precioDolar.text = "Error"
                Log.e("DOLAR_BLUE", "Error de red: ${e.message}")
            } catch (e: Exception) {
                binding.precioDolar.text = "Error"
                Log.e("DOLAR_BLUE", "Error inesperado: ${e.message}")
            }
        }
    }

    private fun irMarcadorTelefono() {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:2302354597") // Reemplaza con el número correcto
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "No se puede realizar la llamada", Toast.LENGTH_SHORT).show()
            Log.e("HomeFragment", "Error al intentar abrir el marcador: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

