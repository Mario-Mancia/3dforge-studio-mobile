package com.example.a3df_studio_app.domain.models

import java.time.OffsetDateTime

data class Producto(
    val id: Long,
    val sku: String,
    val nombre: String,
    val descripcion: String?,
    val precio: Double,
    val stock: Int,
    val imagenUrl: String?
) {
    val disponible: Boolean
        get() = stock > 0
}

data class CategoriaServicio(
    val id: Long,
    val nombre: String,
    val descripcion: String?
)

data class Servicio(
    val id: Long,
    val idCategoria: Long,
    val categoriaNombre: String?,
    val nombre: String,
    val descripcion: String,
    val precioBase: Double,
    val requiereArchivo: Boolean
)

data class Promocion(
    val id: Long,
    val nombre: String,
    val descripcion: String?,
    val tipoDescuento: TipoDescuento,
    val valorDescuento: Double,
    val fechaFin: OffsetDateTime?
)

data class NegocioInfo(
    val nombre: String,
    val correo: String?,
    val telefono: String?,
    val direccion: String?,
    val sitioWeb: String?,
    val logoUrl: String?
)
