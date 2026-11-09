package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FacturaDto(
    val id: Long,
    @SerialName("id_encargo") val idEncargo: Long? = null,
    @SerialName("numero_factura") val numeroFactura: String,
    @SerialName("fecha_emision") val fechaEmision: String? = null,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String? = null,
    @SerialName("nombre_facturacion") val nombreFacturacion: String = "",
    val subtotal: Double = 0.0,
    val descuento: Double = 0.0,
    val impuesto: Double = 0.0,
    val total: Double = 0.0,
    @SerialName("saldo_pendiente") val saldoPendiente: Double = 0.0,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("facturas_detalles") val detalles: List<FacturaDetalleDto> = emptyList()
)

@Serializable
data class FacturaDetalleDto(
    val id: Long,
    val descripcion: String,
    val cantidad: Double,
    @SerialName("precio_unitario") val precioUnitario: Double,
    val subtotal: Double
)

@Serializable
data class PagoDto(
    val id: Long,
    @SerialName("id_factura") val idFactura: Long,
    val referencia: String? = null,
    val monto: Double,
    @SerialName("fecha_pago") val fechaPago: String? = null,
    val estado: String,
    val observaciones: String? = null,
    @SerialName("metodos_pago") val metodo: NombreRefDto? = null
)
