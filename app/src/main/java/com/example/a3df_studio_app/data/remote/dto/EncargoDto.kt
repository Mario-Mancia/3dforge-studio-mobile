package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class EncargoDto(
    val id: Long,
    val codigo: String,
    @SerialName("fecha_solicitud") val fechaSolicitud: String? = null,
    @SerialName("fecha_entrega_estimada") val fechaEntregaEstimada: String? = null,
    @SerialName("fecha_entrega_real") val fechaEntregaReal: String? = null,
    val descripcion: String = "",
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("encargos_detalles") val detalles: List<EncargoDetalleDto> = emptyList(),
    @SerialName("historial_encargos") val historial: List<HistorialEncargoDto> = emptyList()
)

@Serializable
data class EncargoDetalleDto(
    val id: Long,
    @SerialName("id_servicio") val idServicio: Long? = null,
    @SerialName("id_producto") val idProducto: Long? = null,
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Double,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val subtotal: Double
)

@Serializable
data class HistorialEncargoDto(
    val id: Long,
    @SerialName("estado_anterior") val estadoAnterior: String? = null,
    @SerialName("estado_nuevo") val estadoNuevo: String,
    val comentario: String? = null,
    @SerialName("fecha_cambio") val fechaCambio: String? = null
)
