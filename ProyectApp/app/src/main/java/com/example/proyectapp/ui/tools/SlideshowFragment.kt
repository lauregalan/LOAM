package com.example.proyectapp.ui.tools

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyectapp.ar.HelloArActivity
import com.example.proyectapp.databinding.FragmentSlideshowBinding
import com.example.proyectapp.ui.home.HomeViewModel


class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var cameraId: String? = null
    private var isFlashOn = false
    private lateinit var cameraManager: CameraManager

    //inciamos la logica aca
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        return root



    }

    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Inicializar CameraManager
        cameraManager = requireContext().getSystemService(Context.CAMERA_SERVICE) as CameraManager

        cameraId = cameraManager.cameraIdList.first {
            cameraManager.getCameraCharacteristics(it)
                .get(android.hardware.camera2.CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
        }

        //manejo de las acciones para el flash

        binding.switchFlashlight.setOnClickListener {
            if (isFlashOn) {
                apagarFlash()
            } else {
                encenderFlash()
            }
        }

        //la realidad aumentada
        binding.distanceMeter.setOnClickListener {

            val intent = Intent(activity, HelloArActivity::class.java)
            startActivity(intent)
        }

    }
    //al destruir la vista
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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

}