package com.example.proyectapp.ui.chat

interface ChatUpdateListener {
    fun onMessagesReceived(messages: List<Message>)
    fun onError(error: String)
}
