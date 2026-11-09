package com.example.a3df_studio_app.domain.models

data class Cliente(
    val id: Long,
    val tipo: TipoCliente,
    val nombres: String?,
    val apellidos: String?,
    val nombreEmpresa: String?,
    val correo: String,
    val telefono: String?,
    val identificacionFiscal: String?,
    val direccion: String?,
    val fotoUrl: String?
) {
    val nombreVisible: String
        get() = when {
            tipo == TipoCliente.EMPRESA && !nombreEmpresa.isNullOrBlank() -> nombreEmpresa
            else -> listOfNotNull(nombres, apellidos).joinToString(" ").ifBlank { correo }
        }

    val perfilCompleto: Boolean
        get() = !telefono.isNullOrBlank() && !direccion.isNullOrBlank() &&
            (tipo == TipoCliente.PERSONA && !nombres.isNullOrBlank() ||
                tipo == TipoCliente.EMPRESA && !nombreEmpresa.isNullOrBlank())
}

data class ClienteUpdate(
    val tipo: TipoCliente,
    val nombres: String?,
    val apellidos: String?,
    val nombreEmpresa: String?,
    val telefono: String?,
    val identificacionFiscal: String?,
    val direccion: String?
)
