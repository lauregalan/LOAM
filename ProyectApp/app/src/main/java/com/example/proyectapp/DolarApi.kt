package com.example.proyectapp
import retrofit2.Call;
import retrofit2.Response
import retrofit2.http.GET;

//creo la interfaz de retrofit para añadir endpoints de la url
// cada método de la interfaz representa un endpoint
interface DolarApi {
    @GET("v1/dolares/blue")
    suspend fun getDolarBlue(): Response<Dolar>
    //el response es la respuesta de la funcion
    //al ser suspend solo puede ser llamada de otra funcion suspend o de coroutines
}