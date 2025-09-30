package com.example.proyectapp.ui.home

import android.os.BatteryManager
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.proyectapp.R
import com.example.proyectapp.databinding.FragmentHomeBinding
import com.example.proyectapp.dolar.DolarApi
import com.example.proyectapp.dolar.RetrofitHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    var lastBatteryPct: Int = -1
    var lastTime: Long = 0

    // Declara la variable para el ViewModel
    private lateinit var homeViewModel: HomeViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("DefaultLocale")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializa el ViewModel
        homeViewModel = ViewModelProvider(this).get(HomeViewModel::class.java)

        // Asigna las acciones a cada widget del dashboard
        setupDashboardListeners()

        // Comienza a observar los datos de Firebase
        observeReferencePrices()

        // Obtiene el precio del dólar de la API
        obtenerPrecioDolar()

        calcularTiempoBateriaInicial(requireContext())

    }

    @SuppressLint("DefaultLocale")
    fun calcularTiempoBateriaInicial(context: Context) {
        val bm = context.getSystemService(Context.BATTERY_SERVICE) as BatteryManager

        // Nivel actual de batería (%)
        val nivel = bm.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)

        val tiempoBat = nivel * 0.0833333
        binding.batteryInfo.text = String.format("Duración estimada: %.2f horas", tiempoBat)
    }

    private fun setupDashboardListeners() {
        val navController = findNavController()

        binding.cardChat.setOnClickListener {
            navController.navigate(R.id.nav_chat)
        }

        binding.cardGallery.setOnClickListener {
            navController.navigate(R.id.nav_gallery)
        }


        binding.cardSlideshow.setOnClickListener {
             navController.navigate(R.id.nav_slideshow)
        }

        binding.callEngineeringCouncilButton.setOnClickListener {
            irMarcadorTelefono()
        }

        binding.cardWeb.setOnClickListener{
            navController.navigate(R.id.nav_web)
        }
    }
    private fun observeReferencePrices() {
        homeViewModel.prices.observe(viewLifecycleOwner) { prices ->
            if (prices != null) {
                // Actualiza la UI con los nuevos precios formateados
                binding.textPriceConstruction.text = String.format("$%.2f", prices.construccionM2)
                binding.textPriceFees.text = String.format("$%.2f", prices.honorariosHora)
                binding.textPriceStructural.text = String.format("$%.2f/m²", prices.calculoEstructuralM2)
                binding.textPriceManagement.text = String.format("%.0f%%", prices.direccionObraPorcentaje)
            } else {
                // Muestra un estado de carga mientras se obtienen los datos
                val loadingText = "Cargando..."
                binding.textPriceConstruction.text = loadingText
                binding.textPriceFees.text = loadingText
                binding.textPriceStructural.text = loadingText
                binding.textPriceManagement.text = loadingText
            }
        }

        homeViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                // Muestra un Toast si Firebase da un error
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }
        }
    }

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


    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            intent ?: return
            val nivel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) // el nivel de la bat actual

            //guardo el tiempo actual
            val currentTime = System.currentTimeMillis()

            //si bajo la bat hago el calculo
            if (lastBatteryPct != -1 && nivel < lastBatteryPct) {
                val tiempoPorciento = currentTime - lastTime
                lastTime = currentTime
                lastBatteryPct = nivel

                val tiempoHorasPorciento = tiempoPorciento / 1000.0 / 3600.0 // lo paso a horas
                val tiempoRestante = tiempoHorasPorciento * nivel

                binding.batteryInfo.text = String.format("Duración estimada: %.2f horas", tiempoRestante)
            } else if (lastBatteryPct == -1) {
                lastBatteryPct = nivel
                lastTime = currentTime
            }
        }
    }
    override fun onResume() {
        super.onResume()
        requireContext().registerReceiver(batteryReceiver, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
    }
    override fun onPause() {
        super.onPause()
        requireContext().unregisterReceiver(batteryReceiver)
    }






}
