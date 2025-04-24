package com.maverick.adminapp.model

data class Pago(
    val fechaPago: String= "",  // Fecha en que se realizó el pago
    val montoAPagar: Double = 0.0,
    var estado: String = "",
    val deviceId: String = ""
)