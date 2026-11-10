package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ProductoDto(
    val id: Long = 0,
    val sku: String = "",
    val nombre: String = "",
    val descripcion: String? = null,
    val precio: String? = null,
    val stock: Int = 0,
    @SerialName("stock_minimo") val stockMinimo: Int = 0,
    val disponibilidad: String = "disponible",
    @SerialName("imagen_url") val imagenUrl: String? = null
)

@Serializable
data class CategoriaDto(
    val id: Long = 0,
    val nombre: String = "",
    val descripcion: String? = null
)

@Serializable
data class ServicioDto(
    val id: Long = 0,
    @SerialName("categoria_id") val categoriaId: Long = 0,
    val categoria: String = "",
    val nombre: String = "",
    val descripcion: String? = null,
    @SerialName("precio_base") val precioBase: String? = null,
    @SerialName("requiere_archivo") val requiereArchivo: Boolean = false
)

@Serializable
data class ItemRequest(
    @SerialName("producto_id") val productoId: Long,
    val cantidad: Int
)

@Serializable
data class CarritoRequest(val items: List<ItemRequest>)

@Serializable
data class LineaCarritoDto(
    @SerialName("producto_id") val productoId: Long = 0,
    val producto: ProductoDto? = null,
    val cantidad: Int = 0,
    val disponible: Boolean = false
)

@Serializable
data class TotalesDto(
    val subtotal: String? = null,
    val descuento: String? = null,
    val impuesto: String? = null,
    val total: String? = null
)

@Serializable
data class CarritoValidadoDto(
    val lineas: List<LineaCarritoDto> = emptyList(),
    val totales: TotalesDto = TotalesDto(),
    @SerialName("hay_problemas") val hayProblemas: Boolean = false
)

@Serializable
data class CompraRequest(val items: List<ItemRequest>, val token: String)

@Serializable
data class CompraDto(
    val duplicado: Boolean = false,
    val pedido: EncargoDto = EncargoDto()
)
