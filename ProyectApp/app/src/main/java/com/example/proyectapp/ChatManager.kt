// Archivo: app/src/main/java/com/example/proyectapp/ChatManager.kt
package com.example.proyectapp

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ChatManager(private val listener: ChatUpdateListener) {

    private val database: DatabaseReference // Referencia a la DB

    init {
        // Inicializar Firebase Realtime Database y obtener la referencia al nodo de mensajes
        val firebaseDatabase = FirebaseDatabase.getInstance("https://loam-5a61f-default-rtdb.firebaseio.com/") // TU URL
        database = firebaseDatabase.getReference("chatSimulado").child("mensajes")
        setupMessageListener()
    }

    // Enviar un mensaje al Realtime Database
    fun sendMessage(sender: String, messageText: String) {
        if (messageText.isNotEmpty()) {
            val message = Message(sender, messageText, System.currentTimeMillis())
            database.push().setValue(message) // .push() genera un ID único
        }
    }

    // Configurar el listener para recibir mensajes en tiempo real
    private fun setupMessageListener() {
        database.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val messages = mutableListOf<Message>()
                for (messageSnapshot in snapshot.children) {
                    val message = messageSnapshot.getValue(Message::class.java)
                    message?.let { messages.add(it) }
                }
                // Ordenar mensajes por timestamp (más recientes al final)
                messages.sortBy { it.timestamp }
                listener.onMessagesReceived(messages) // Informar a la UI
            }

            override fun onCancelled(error: DatabaseError) {
                // Manejar errores
                listener.onError("Error al leer la base de datos: ${error.message}")
            }
        })
    }
}
