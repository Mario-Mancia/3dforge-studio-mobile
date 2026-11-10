package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(val correo: String, val password: String)

@Serializable
data class GoogleRequest(@SerialName("id_token") val idToken: String, val nonce: String)

@Serializable
data class RegistroRequest(
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val password: String
)

@Serializable
data class CorreoRequest(val correo: String)

@Serializable
data class CambiarPasswordRequest(
    @SerialName("password_actual") val passwordActual: String,
    @SerialName("password_nueva") val passwordNueva: String
)

@Serializable
data class UsuarioDto(
    val id: Long = 0,
    val correo: String = "",
    val nombres: String = "",
    val apellidos: String = ""
)

@Serializable
data class ClienteDto(
    val id: Long = 0,
    @SerialName("tipo_cliente") val tipoCliente: String = "PERSONA",
    @SerialName("nombre_visible") val nombreVisible: String = "",
    val nombres: String? = null,
    val apellidos: String? = null,
    @SerialName("nombre_empresa") val nombreEmpresa: String? = null,
    val correo: String? = null,
    val telefono: String? = null,
    @SerialName("identificacion_fiscal") val identificacionFiscal: String? = null,
    val direccion: String? = null
)

@Serializable
data class SesionDto(
    val token: String,
    @SerialName("expira_en") val expiraEn: String? = null,
    val usuario: UsuarioDto = UsuarioDto(),
    val cliente: ClienteDto = ClienteDto()
)

@Serializable
data class PerfilRequest(
    @SerialName("tipo_cliente") val tipoCliente: String,
    val nombres: String,
    val apellidos: String,
    @SerialName("nombre_empresa") val nombreEmpresa: String,
    val telefono: String,
    @SerialName("identificacion_fiscal") val identificacionFiscal: String,
    val direccion: String
)

@Serializable
data class KpisDto(
    @SerialName("por_responder") val porResponder: Int = 0,
    @SerialName("en_revision") val enRevision: Int = 0,
    @SerialName("pedidos_en_curso") val pedidosEnCurso: Int = 0,
    @SerialName("saldo_pendiente") val saldoPendiente: String? = null,
    @SerialName("soporte_abierto") val soporteAbierto: Int = 0
)

@Serializable
data class PanelDto(val kpis: KpisDto = KpisDto())

@Serializable
data class NegocioDto(
    val nombre: String = "",
    val correo: String? = null,
    val telefono: String? = null,
    val direccion: String? = null,
    @SerialName("sitio_web") val sitioWeb: String? = null,
    @SerialName("logo_url") val logoUrl: String? = null
)
