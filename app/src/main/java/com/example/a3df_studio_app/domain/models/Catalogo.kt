package com.example.a3df_studio_app.domain.models

import java.math.BigDecimal

data class Producto(
    val id: Long,
    val sku: String,
    val nombre: String,
    val descripcion: String,
    val precio: BigDecimal,
    val stock: Int,
    val stockMinimo: Int,
    val disponibilidad: Disponibilidad,
    val imagenUrl: String?
) {
    val agotado: Boolean get() = disponibilidad == Disponibilidad.AGOTADO
}

data class CategoriaServicio(
    val id: Long,
    val nombre: String,
    val descripcion: String
)

data class Servicio(
    val id: Long,
    val categoriaId: Long,
    val categoria: String,
    val nombre: String,
    val descripcion: String,
    val precioBase: BigDecimal,
    val requiereArchivo: Boolean
)

data class ItemCarrito(
    val productoId: Long,
    val nombre: String,
    val precio: BigDecimal,
    val imagenUrl: String?,
    val stock: Int,
    val cantidad: Int
) {
    val subtotal: BigDecimal get() = precio.multiply(BigDecimal(cantidad))
}

data class TotalesCarrito(
    val subtotal: BigDecimal,
    val impuesto: BigDecimal,
    val total: BigDecimal
)

data class LineaValidada(
    val productoId: Long,
    val nombre: String,
    val cantidad: Int,
    val disponible: Boolean,
    val stock: Int,
    val precio: BigDecimal?
)

data class CarritoValidado(
    val lineas: List<LineaValidada>,
    val subtotal: BigDecimal,
    val descuento: BigDecimal,
    val impuesto: BigDecimal,
    val total: BigDecimal,
    val hayProblemas: Boolean
)
