package com.example.proyectapp.ui.home

import android.annotation.SuppressLint
import android.content.Intent
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
import com.example.proyectapp.dolar.DolarApi
import com.example.proyectapp.RetrofitHelper
import com.example.proyectapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Configura el listener para el botón de llamada
        binding.callEngineeringCouncilButton.setOnClickListener {
            irMarcadorTelefono()
        }

        // Obtiene el precio del dólar de forma segura
        obtenerPrecioDolar()
    }

    private fun obtenerPrecioDolar() {
        // Usamos lifecycleScope: la corrutina se cancela si el fragmento se destruye
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Hacemos la llamada a la API en el hilo de IO (Entrada/Salida)
                val result = withContext(Dispatchers.IO) {
                    RetrofitHelper.getInstance().create(DolarApi::class.java).getDolarBlue()
                }

                // Volvemos al hilo Principal (Main) para actualizar la UI
                if (result != null) {
                    binding.precioDolar.text = "$${result.compra}" // Formato más legible
                    Log.d("DOLAR_BLUE", "Precio de compra: ${result.compra}")
                } else {
                    binding.precioDolar.text = "N/A"
                    Log.w("DOLAR_BLUE", "La respuesta de la API fue nula.")
                }
            } catch (e: IOException) {
                // Manejo de errores de red (ej: sin internet)
                binding.precioDolar.text = "Error"
                Log.e("DOLAR_BLUE", "Error de red: ${e.message}")
                Toast.makeText(context, "Error de conexión", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                // Manejo de otros errores (ej: parsing, etc.)
                binding.precioDolar.text = "Error"
                Log.e("DOLAR_BLUE", "Error inesperado: ${e.message}")
                Toast.makeText(context, "Ocurrió un error", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun irMarcadorTelefono() {
        try {
            val intent = Intent(Intent.ACTION_DIAL)
            intent.data = Uri.parse("tel:2302354597")
            startActivity(intent)
        } catch (e: Exception) {
            // Maneja el caso en que no se pueda abrir el marcador (ej: en una tablet sin teléfono)
            Toast.makeText(requireContext(), "No se puede realizar la llamada", Toast.LENGTH_SHORT).show()
            Log.e("HomeFragment", "Error al intentar abrir el marcador: ${e.message}")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
