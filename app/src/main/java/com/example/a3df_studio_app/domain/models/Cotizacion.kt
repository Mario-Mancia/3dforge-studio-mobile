package com.example.a3df_studio_app.domain.models

import java.time.OffsetDateTime

data class Cotizacion(
    val id: Long,
    val codigo: String,
    val fechaEmision: OffsetDateTime?,
    val fechaVencimiento: OffsetDateTime?,
    val subtotal: Double,
    val descuento: Double,
    val impuesto: Double,
    val total: Double,
    val estado: EstadoCotizacion,
    val observaciones: String?,
    val detalles: List<CotizacionDetalle>
)

data class CotizacionDetalle(
    val id: Long,
    val idServicio: Long,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)

data class CotizacionCreada(
    val id: Long,
    val codigo: String,
    val total: Double
)

data class ItemCotizacion(
    val servicio: Servicio,
    val cantidad: Double = 1.0,
    val detalle: String? = null
) {
    val subtotalEstimado: Double
        get() = servicio.precioBase * cantidad
}
