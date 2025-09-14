package com.example.proyectapp.ui.slideshow

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyectapp.databinding.FragmentSlideshowBinding
import com.google.ar.core.ArCoreApk
import com.google.ar.core.Config
import com.google.ar.core.Session
class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel = ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val textView: TextView = binding.textSlideshow
        slideshowViewModel.text.observe(viewLifecycleOwner) {
            textView.text = it
        }
        return root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        /*chequeo que el telefono tenga compatibilidad con arcore, si tiene entonces
        * le mandamos a que lo instale, una vez que lo instale creamos la session  */
        try {
            val availability = ArCoreApk.getInstance().checkAvailability(requireContext())
            if (availability.isSupported) {
                val installStatus = ArCoreApk.getInstance().requestInstall(requireActivity(), true)
                if (installStatus == ArCoreApk.InstallStatus.INSTALL_REQUESTED) {
                    // Se abre la Play Store → esperar a que el usuario instale
                    return
                }
                val session = Session(requireContext()) // encender el motor de ARCore.

                val config = session.config

                // Check whether the user's device supports the Depth API.
                val isDepthSupported = session.isDepthModeSupported(Config.DepthMode.AUTOMATIC)
                if (isDepthSupported) {
                    config.depthMode = Config.DepthMode.AUTOMATIC
                }
                session.configure(config)


            } else {
                binding.textSlideshow.text = "Este dispositivo no soporta ARCore"
            }
        } catch (e: com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException) {
            binding.textSlideshow.text = "ARCore no está instalado"
        } catch (e: com.google.ar.core.exceptions.UnavailableDeviceNotCompatibleException) {
            binding.textSlideshow.text = "El dispositivo no es compatible con ARCore"
        } catch (e: Exception) {
            e.printStackTrace()
            binding.textSlideshow.text = "Error inicializando ARCore: ${e.message}"
        }



        //val arFragment = childFragmentManager.findFragmentById(R.id.arFragment) as com.google.ar.sceneform.ux.ArFragment

        //var firstAnchor: com.google.ar.core.Anchor? = null

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


}