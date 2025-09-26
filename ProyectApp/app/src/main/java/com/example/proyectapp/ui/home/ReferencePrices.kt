package com.example.proyectapp.ui.home

import com.google.firebase.database.PropertyName

data class ReferencePrices(
    @get:PropertyName("construccion_m2") @set:PropertyName("construccion_m2") var construccionM2: Double = 0.0,
    @get:PropertyName("honorarios_hora") @set:PropertyName("honorarios_hora") var honorariosHora: Double = 0.0,
    @get:PropertyName("calculo_estructural_m2") @set:PropertyName("calculo_estructural_m2") var calculoEstructuralM2: Double = 0.0,
    @get:PropertyName("direccion_obra_porcentaje") @set:PropertyName("direccion_obra_porcentaje") var direccionObraPorcentaje: Double = 0.0
) {
    // Constructor vacío requerido por Firebase
    constructor() : this(0.0, 0.0, 0.0, 0.0)
}