package com.example.proyectapp.ui.chat

data class Message(
    var sender: String = "",
    var text: String = "",
    var timestamp: Long = 0L
)
