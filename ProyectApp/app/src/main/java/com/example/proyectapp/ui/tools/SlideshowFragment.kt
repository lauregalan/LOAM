package com.example.proyectapp.ui.tools

import android.annotation.SuppressLint
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.text.intl.Locale
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.proyectapp.ar.HelloArActivity
import com.example.proyectapp.databinding.FragmentSlideshowBinding
import com.example.proyectapp.ui.home.HomeViewModel
import com.google.ai.edge.litert.Environment
import com.google.type.Date
import java.io.File
import java.io.IOException


class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    private var cameraId: String? = null
    private var isFlashOn = false
    private lateinit var cameraManager: CameraManager
    private lateinit var tempPhotoUri: Uri
    private val REQUEST_VIDEO_CAPTURE = 1
    private var videoUri: Uri? = null
    private var mediaRecorder: MediaRecorder? = null
    private var audioUri: Uri? = null
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

        //la grabacion de camara

        binding.videoRecorder.setOnClickListener{
            grabarVideo()
        }

        //el audio


        binding.audioRecorder.setOnClickListener{
            grabarAudio()
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

    // para la grabacion
    private val launchCamera =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK) {
                Toast.makeText(requireContext(), "¡Video guardado en la galería!", Toast.LENGTH_LONG).show()
            }
        }
    /*iniciar una actividad secundaria y obtener un resultado de ella de una manera más escalable
     y organizada en el desarrollo de aplicaciones Android */

    /*ActivityResultContracts.StartActivityForResult() indica que vamos a lanzar un
        Intent cualquiera (en este caso la cámara).
        El callback result -> { ... } se ejecuta cuando la cámara vuelve:
     */

    private fun grabarVideo() {

        val values = ContentValues().apply {
            put(MediaStore.Video.Media.TITLE, "mi_video")
            put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        }
        /*creo una entrada de mediastore */

        videoUri = requireContext().contentResolver.insert(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            values
        )

        /* con contentResolver hacemos
        acceso a los datos asociados a un Uri; en este caso los modificamos*/

        view?.post {
            val intent = Intent(MediaStore.ACTION_VIDEO_CAPTURE).apply {
                putExtra(MediaStore.EXTRA_OUTPUT, videoUri)

            }
            launchCamera.launch(intent)
        }
        /* le meto retraso para que el wlm no me tire error pore
        * estar dentro del callback de ui*/
    }

    private fun grabarAudio() {
        val values = ContentValues().apply {
            put(MediaStore.Audio.Media.TITLE, "mi_audio")
            put(MediaStore.Audio.Media.DISPLAY_NAME, "mi_audio.m4a")
            put(MediaStore.Audio.Media.MIME_TYPE, "audio/mp4") // AAC en contenedor mp4/m4a
        }

        audioUri = requireContext().contentResolver.insert(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            values
        )

        val fd = requireContext().contentResolver.openFileDescriptor(audioUri!!, "w")?.fileDescriptor

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(fd)
            prepare()
            start()
        }

        Toast.makeText(requireContext(), "Grabando audio...", Toast.LENGTH_SHORT).show()
    }

    private fun detenerAudio() {
        mediaRecorder?.apply {
            stop()
            release()
        }
        mediaRecorder = null
        Toast.makeText(requireContext(), "¡Audio guardado en galería!", Toast.LENGTH_SHORT).show()
    }

}
