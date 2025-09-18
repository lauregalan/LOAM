package com.example.proyectapp

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment

class ChatFragment : Fragment(), ChatUpdateListener {

    private lateinit var messageEditText: EditText
    private lateinit var sendButton: Button
    private lateinit var chatTextView: TextView
    private lateinit var chatManager: ChatManager // Instancia de ChatManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflar el layout para este fragmento
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Inicializar vistas del layout del fragmento
        messageEditText = view.findViewById(R.id.messageEditText)
        sendButton = view.findViewById(R.id.sendButton)
        chatTextView = view.findViewById(R.id.chatTextView)

        // Inicializar ChatManager, pasándole este fragmento como listener
        chatManager = ChatManager(this)

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                chatManager.sendMessage("usuario", messageText) // El ChatManager se encarga de enviarlo
                messageEditText.setText("") // Limpiar el campo
            }
        }

        return view
    }

    // Implementación de la interfaz ChatUpdateListener
    override fun onMessagesReceived(messages: List<Message>) {
        // Actualizar la UI con los nuevos mensajes
        val chatLog = StringBuilder()
        for (msg in messages) {
            chatLog.append("${msg.sender}: ${msg.text}\n")
        }
        chatTextView.text = chatLog.toString()
        // Opcional: para que el scroll vaya al final si hay muchos mensajes
        // chatTextView.post { chatTextView.fullScroll(View.FOCUS_DOWN) }
    }

    override fun onError(error: String) {
        // Mostrar un mensaje de error al usuario
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
    }
}
