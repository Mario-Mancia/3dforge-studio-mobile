package com.example.a3df_studio_app.domain.models

import java.time.OffsetDateTime

data class Factura(
    val id: Long,
    val idEncargo: Long?,
    val numero: String,
    val fechaEmision: OffsetDateTime?,
    val fechaVencimiento: OffsetDateTime?,
    val nombreFacturacion: String,
    val subtotal: Double,
    val descuento: Double,
    val impuesto: Double,
    val total: Double,
    val saldoPendiente: Double,
    val estado: EstadoFactura,
    val detalles: List<FacturaDetalle>
) {
    val pagable: Boolean
        get() = estado.pagable && saldoPendiente > 0.0
}

data class FacturaDetalle(
    val id: Long,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)

data class Pago(
    val id: Long,
    val idFactura: Long,
    val referencia: String?,
    val monto: Double,
    val fecha: OffsetDateTime?,
    val estado: EstadoPago,
    val metodo: String?,
    val observaciones: String?
)

data class PagoRegistrado(
    val idPago: Long,
    val referencia: String,
    val estado: EstadoPago,
    val saldoPendiente: Double,
    val estadoFactura: EstadoFactura
)

data class TarjetaPago(
    val numero: String,
    val titular: String,
    val mesExpiracion: Int,
    val anioExpiracion: Int,
    val cvv: String
)
