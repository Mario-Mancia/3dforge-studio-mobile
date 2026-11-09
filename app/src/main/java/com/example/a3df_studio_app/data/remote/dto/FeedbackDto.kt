package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ResenaDto(
    val id: Long,
    @SerialName("id_servicio") val idServicio: Long? = null,
    @SerialName("id_encargo") val idEncargo: Long? = null,
    val puntuacion: Int,
    val comentario: String? = null,
    @SerialName("fecha_creacion") val fechaCreacion: String? = null
)

@Serializable
data class ResenaInsertDto(
    @SerialName("id_cliente") val idCliente: Long,
    @SerialName("id_servicio") val idServicio: Long? = null,
    @SerialName("id_encargo") val idEncargo: Long? = null,
    val puntuacion: Int,
    val comentario: String? = null
)

@Serializable
data class SolicitudSoporteDto(
    val id: Long,
    val asunto: String,
    val descripcion: String,
    val prioridad: String,
    val estado: String,
    @SerialName("creado_en") val creadoEn: String? = null
)

@Serializable
data class SolicitudSoporteInsertDto(
    @SerialName("id_cliente") val idCliente: Long,
    val asunto: String,
    val descripcion: String,
    val prioridad: String,
    val estado: String = "ABIERTA"
)
