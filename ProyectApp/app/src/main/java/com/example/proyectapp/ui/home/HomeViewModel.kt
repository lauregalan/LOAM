package com.example.proyectapp.ui.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.database

class HomeViewModel : ViewModel() {

    // Referencia al nodo "precios_referencia" en tu Firebase Realtime Database
    private val database = Firebase.database.getReference("precios_referencia")

    // LiveData para exponer los precios a la vista (HomeFragment)
    private val _prices = MutableLiveData<ReferencePrices?>()
    val prices: LiveData<ReferencePrices?> = _prices

    // LiveData para manejar errores de conexión
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val valueEventListener = object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            // Cuando los datos cambian en Firebase, se convierten al objeto ReferencePrices
            val referencePrices = snapshot.getValue(ReferencePrices::class.java)
            _prices.value = referencePrices
            Log.d("HomeViewModel", "Datos de Firebase actualizados: $referencePrices")
        }

        override fun onCancelled(error: DatabaseError) {
            // Manejo de errores en caso de que la lectura sea cancelada
            _error.value = "Error al leer los precios de referencia: ${error.message}"
            Log.e("HomeViewModel", "Error en Firebase: ${error.message}")
        }
    }

    init { //cuando arrancas la pantallas
        // Al iniciar el ViewModel, se añade el listener para escuchar cambios en tiempo real.
        database.addValueEventListener(valueEventListener)
    }

    // Es importante remover el listener cuando el ViewModel se destruye para evitar fugas de memoria.
    override fun onCleared() {
        super.onCleared()
        database.removeEventListener(valueEventListener)
    }
}