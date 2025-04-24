package com.maverick.adminapp.model

data class Device(
    val id: String = "",
    val imei: String = "",
    val cliente: String = "",
    val ciudad: String = "",
    val telefono: String = "",
    val marca: String = "",
    val modelo: String = "",
    val precio: Double? = null,
    val frecuenciaPago: String = "",
    val periodoPago: String = "",
    val fechaInicio: String = "",
    val fechaFin: String = "",
    val montoAPagar: Double? = null,
    val estado: Boolean = true, // true = Desbloqueado, false = Bloqueado
    val pagos: List<Pago> = emptyList()  // Campo para almacenar los pagos como una lista
)
