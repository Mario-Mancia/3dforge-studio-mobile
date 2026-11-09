package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NombreRefDto(
    val nombre: String = ""
)

@Serializable
data class CategoriaServicioDto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    val activo: Boolean = true
)

@Serializable
data class ServicioDto(
    val id: Long,
    @SerialName("id_categoria") val idCategoria: Long,
    val nombre: String,
    val descripcion: String = "",
    @SerialName("precio_base") val precioBase: Double = 0.0,
    @SerialName("requiere_archivo") val requiereArchivo: Boolean = false,
    val activo: Boolean = true,
    @SerialName("categorias_servicios") val categoria: NombreRefDto? = null
)

@Serializable
data class ProductoDto(
    val id: Long,
    val sku: String,
    val nombre: String,
    val descripcion: String? = null,
    val precio: Double = 0.0,
    val stock: Int = 0,
    @SerialName("stock_minimo") val stockMinimo: Int = 0,
    val imagen: String? = null,
    val activo: Boolean = true
)

@Serializable
data class PromocionDto(
    val id: Long,
    val nombre: String,
    val descripcion: String? = null,
    @SerialName("tipo_descuento") val tipoDescuento: String,
    @SerialName("valor_descuento") val valorDescuento: Double,
    @SerialName("fecha_inicio") val fechaInicio: String? = null,
    @SerialName("fecha_fin") val fechaFin: String? = null,
    val activo: Boolean = true
)

@Serializable
data class ConfiguracionNegocioDto(
    val id: Long,
    @SerialName("nombre_negocio") val nombreNegocio: String,
    val correo: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    @SerialName("sitio_web") val sitioWeb: String? = null,
    val logo: String? = null
)
