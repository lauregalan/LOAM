package com.example.proyectapp

interface ChatUpdateListener {
    fun onMessagesReceived(messages: List<Message>)
    fun onError(error: String)
}
