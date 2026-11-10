package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ErrorBody(val error: ErrorInfo? = null)

@Serializable
data class ErrorInfo(
    val codigo: String = "",
    val mensaje: String = "",
    val campos: Map<String, List<String>> = emptyMap()
)

@Serializable
data class MensajeDto(val mensaje: String = "")

@Serializable
data class OkDto(val ok: Boolean = true)

@Serializable
data class PaginaDto<T>(
    val resultados: List<T> = emptyList(),
    val total: Int = 0,
    val pagina: Int = 1,
    val paginas: Int = 1,
    @SerialName("hay_mas") val hayMas: Boolean = false
)

@Serializable
data class ListaDto<T>(val resultados: List<T> = emptyList())

@Serializable
data class ContadorDto(@SerialName("no_leidas") val noLeidas: Int = 0)
