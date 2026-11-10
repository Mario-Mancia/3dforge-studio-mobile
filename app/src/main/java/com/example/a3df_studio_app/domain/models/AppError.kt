package com.example.a3df_studio_app.domain.models

sealed class AppError(mensaje: String) : Exception(mensaje) {
    data object SinConexion : AppError("Sin conexión. Verifica que Tailscale esté activo y que el servidor esté encendido.")
    data object SesionExpirada : AppError("Tu sesión expiró. Inicia sesión nuevamente.")
    data object Cancelado : AppError("Operación cancelada.")
    data class CorreoNoConfirmado(val detalle: String) : AppError(detalle)
    data class CuentaNoHabilitada(val detalle: String) : AppError(detalle)
    data class Bloqueado(val detalle: String) : AppError(detalle)
    data class DatosInvalidos(val detalle: String, val campos: Map<String, List<String>>) : AppError(detalle)
    data class ErrorApi(val http: Int, val codigo: String, val detalle: String) : AppError(detalle)
    data class Local(val detalle: String) : AppError(detalle)
}
