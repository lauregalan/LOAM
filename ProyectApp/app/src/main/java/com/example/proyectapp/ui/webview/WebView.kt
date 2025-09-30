package com.example.proyectapp.ui.webview

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.example.proyectapp.databinding.FragmentWebviewBinding

class WebViewFragment : Fragment() {

    private var _binding: FragmentWebviewBinding? = null
    private val binding get() = _binding!!
    private lateinit var webView: android.webkit.WebView  // aclarar tipo completo

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWebviewBinding.inflate(inflater, container, false)


        webView = binding.webview

        val webSettings: WebSettings = webView.settings
        webSettings.javaScriptEnabled = true  // Habilitar JS si lo necesita tu HTML
        webSettings.domStorageEnabled = true
        webSettings.useWideViewPort = true
        webSettings.loadWithOverviewMode = true
        webSettings.javaScriptCanOpenWindowsAutomatically = true

        webView.webViewClient = android.webkit.WebViewClient()
        webView.webChromeClient = WebChromeClient()
        // OPCIÓN A: cargar desde internet
        webView.loadUrl("http://chat.openai.com/")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }
}