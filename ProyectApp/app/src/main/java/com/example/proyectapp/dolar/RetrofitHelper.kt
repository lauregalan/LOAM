package com.example.proyectapp.dolar

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

//si creamos una unica clase que centralzia la creacion del retrofit entonces no hay que repetir la config
// cada vez que hacemos get a un endpoint
object RetrofitHelper { //usamos patron singleton hard
    val baseUrl = "https://dolarapi.com"

    fun getInstance(): Retrofit { //nos devuelve un objeto retrofit para usar
        return Retrofit.Builder().baseUrl(baseUrl) //le pasamos la url base
            .addConverterFactory(GsonConverterFactory.create())
            //aca el gson hace algo re inteligente... parsea el json a un data class automaticametne
            .build()
    }
}