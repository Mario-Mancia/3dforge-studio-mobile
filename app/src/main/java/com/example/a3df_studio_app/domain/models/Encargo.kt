package com.example.a3df_studio_app.domain.models

import java.time.OffsetDateTime

data class Encargo(
    val id: Long,
    val codigo: String,
    val fechaSolicitud: OffsetDateTime?,
    val fechaEntregaEstimada: OffsetDateTime?,
    val fechaEntregaReal: OffsetDateTime?,
    val descripcion: String,
    val subtotal: Double,
    val descuento: Double,
    val impuesto: Double,
    val total: Double,
    val estado: EstadoEncargo,
    val observaciones: String?,
    val detalles: List<EncargoDetalle>,
    val historial: List<HistorialEncargo>
)

data class EncargoDetalle(
    val id: Long,
    val idServicio: Long?,
    val idProducto: Long?,
    val descripcion: String,
    val cantidad: Double,
    val precioUnitario: Double,
    val subtotal: Double
)

data class HistorialEncargo(
    val id: Long,
    val estadoAnterior: EstadoEncargo?,
    val estadoNuevo: EstadoEncargo,
    val comentario: String?,
    val fecha: OffsetDateTime?
)

data class ItemPedido(
    val producto: Producto,
    val cantidad: Int = 1
) {
    val subtotal: Double
        get() = producto.precio * cantidad
}

data class PedidoCreado(
    val idEncargo: Long,
    val codigo: String,
    val idFactura: Long,
    val numeroFactura: String,
    val total: Double
)
