package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ClienteDto(
    val id: Long,
    @SerialName("auth_uid") val authUid: String? = null,
    @SerialName("tipo_cliente") val tipoCliente: String = "PERSONA",
    val nombres: String? = null,
    val apellidos: String? = null,
    @SerialName("nombre_empresa") val nombreEmpresa: String? = null,
    val correo: String = "",
    val telefono: String? = null,
    @SerialName("identificacion_fiscal") val identificacionFiscal: String? = null,
    val direccion: String? = null,
    @SerialName("foto_url") val fotoUrl: String? = null,
    @SerialName("creado_en") val creadoEn: String? = null,
    @SerialName("actualizado_en") val actualizadoEn: String? = null
)
