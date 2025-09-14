package com.example.proyectapp.ui.home

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyectapp.DolarApi
import com.example.proyectapp.RetrofitHelper
import com.example.proyectapp.databinding.FragmentHomeBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    //inciamos la logica aca
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textHome
        homeViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root



    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val dolarApi = RetrofitHelper.getInstance().create(DolarApi::class.java)


        // de una corrutina, que es un bloque asíncrono que maneja la pausa y reanudación.
        GlobalScope.launch {
            val result = dolarApi.getDolarBlue() // request al endpoint
            if (result != null)

                binding.precioDolar.text = "Precio del dolar blue 💶: " + result.compra.toString();
                Log.d("DOLAR BLUE: ", result.toString())

        }

    }
    //al destruir la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}