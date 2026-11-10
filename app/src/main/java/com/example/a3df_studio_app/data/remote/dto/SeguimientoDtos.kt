package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class NotificacionDto(
    val id: Long = 0,
    val titulo: String = "",
    val mensaje: String = "",
    val tipo: String = "",
    @SerialName("referencia_id") val referenciaId: Long? = null,
    val leida: Boolean = false,
    val fecha: String? = null
)

@Serializable
data class SoporteDto(
    val id: Long = 0,
    val asunto: String = "",
    val descripcion: String = "",
    val prioridad: String = "MEDIA",
    val estado: String = "",
    @SerialName("estado_texto") val estadoTexto: String = "",
    @SerialName("creada_en") val creadaEn: String? = null,
    @SerialName("actualizada_en") val actualizadaEn: String? = null
)

@Serializable
data class SoporteRequest(
    val asunto: String,
    val descripcion: String,
    val prioridad: String
)
