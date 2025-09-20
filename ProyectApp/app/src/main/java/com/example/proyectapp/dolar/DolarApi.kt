package com.example.proyectapp.dolar

import retrofit2.http.GET

//creo la interfaz de retrofit para añadir endpoints de la url
// cada método de la interfaz representa un endpoint
interface DolarApi {
    @GET("v1/dolares/blue")
    suspend fun getDolarBlue(): Dolar
    //el response es la respuesta de la funcion
    //al ser suspend solo puede ser llamada de otra funcion suspend o de coroutines
}