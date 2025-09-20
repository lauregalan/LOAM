package com.example.proyectapp.ui.chat

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proyectapp.R

class ChatFragment : Fragment(), ChatUpdateListener {

    // Cambia el tipo de sendButton a ImageButton
    private lateinit var messageEditText: EditText
    private lateinit var sendButton: ImageButton
    private lateinit var chatManager: ChatManager

    private lateinit var recyclerView: RecyclerView
    private lateinit var chatAdapter: ChatAdapter
    private val messageList = mutableListOf<Message>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // **ACTUALIZACIÓN DE IDS**
        // Los IDs del XML han cambiado, los actualizamos aquí
        messageEditText = view.findViewById(R.id.editTextMessage)
        sendButton = view.findViewById(R.id.imageButtonSend)
        recyclerView = view.findViewById(R.id.chatRecyclerView)

        // Inicializar el RecyclerView y su adaptador
        chatAdapter = ChatAdapter(messageList)
        recyclerView.adapter = chatAdapter
        recyclerView.layoutManager = LinearLayoutManager(context)

        chatManager = ChatManager(this)

        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                chatManager.sendMessage("usuario", messageText)
                messageEditText.setText("")
            }
        }
        return view
    }

    override fun onMessagesReceived(messages: List<Message>) {
        chatAdapter.updateMessages(messages)
        recyclerView.scrollToPosition(messages.size - 1)
    }

    override fun onError(error: String) {
        Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
    }
}