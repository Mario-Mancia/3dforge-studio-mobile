package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class CotizacionDto(
    val id: Long,
    val codigo: String,
    @SerialName("fecha_emision") val fechaEmision: String? = null,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String? = null,
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("cotizaciones_detalles") val detalles: List<CotizacionDetalleDto> = emptyList()
)

@Serializable
data class CotizacionDetalleDto(
    val id: Long,
    @SerialName("id_servicio") val idServicio: Long,
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Double,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val subtotal: Double
)
