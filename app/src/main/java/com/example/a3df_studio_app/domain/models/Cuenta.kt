package com.example.a3df_studio_app.domain.models

import java.math.BigDecimal

data class AuthUser(
    val id: Long,
    val correo: String,
    val nombres: String,
    val apellidos: String
) {
    val nombreCompleto: String
        get() = "$nombres $apellidos".trim()

    val nombreVisible: String
        get() = nombreCompleto.ifBlank { correo.substringBefore("@") }

    val primerNombre: String
        get() = nombres.ifBlank { nombreVisible }.substringBefore(" ")

    val iniciales: String
        get() = nombreVisible.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
}

sealed interface AuthState {
    data object Cargando : AuthState
    data object SinSesion : AuthState
    data class ConSesion(val usuario: AuthUser) : AuthState
}

data class SolicitudRegistro(
    val nombres: String,
    val apellidos: String,
    val correo: String,
    val telefono: String,
    val password: String
)

data class Cliente(
    val id: Long,
    val tipo: TipoCliente,
    val nombreVisible: String,
    val nombres: String,
    val apellidos: String,
    val nombreEmpresa: String,
    val correo: String,
    val telefono: String,
    val identificacionFiscal: String,
    val direccion: String
)

data class CambiosPerfil(
    val tipo: TipoCliente,
    val nombres: String,
    val apellidos: String,
    val nombreEmpresa: String,
    val telefono: String,
    val identificacionFiscal: String,
    val direccion: String
)

data class InfoNegocio(
    val nombre: String,
    val correo: String?,
    val telefono: String?,
    val direccion: String?,
    val sitioWeb: String?,
    val logoUrl: String?
)

data class ResumenPanel(
    val porResponder: Int,
    val enRevision: Int,
    val pedidosEnCurso: Int,
    val saldoPendiente: BigDecimal,
    val soporteAbierto: Int
)

data class Pagina<T>(
    val items: List<T>,
    val total: Int,
    val pagina: Int,
    val hayMas: Boolean
)
