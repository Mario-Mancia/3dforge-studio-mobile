package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject

@Serializable
data class OpcionTextoDto(val codigo: String = "", val texto: String = "")

@Serializable
data class FilamentoDto(
    val id: Long = 0,
    val material: String = "",
    val color: String = "",
    val nombre: String = ""
)

@Serializable
data class Opciones3dDto(
    val disponible: Boolean = false,
    val filamentos: List<FilamentoDto> = emptyList(),
    val rellenos: List<Int> = emptyList(),
    val calidades: List<OpcionTextoDto> = emptyList(),
    val escalas: List<Int> = emptyList(),
    val unidades: List<OpcionTextoDto> = emptyList(),
    val soportes: List<OpcionTextoDto> = emptyList(),
    @SerialName("cantidad_maxima") val cantidadMaxima: Int = 50,
    val formatos: List<String> = emptyList(),
    @SerialName("tamano_maximo_mb") val tamanoMaximoMb: Int = 50,
    @SerialName("cama_mm") val camaMm: List<Double> = emptyList(),
    val aviso: String = ""
)

@Serializable
data class Estimacion3dDto(
    val cabe: Boolean = true,
    @SerialName("mensaje_cabida") val mensajeCabida: String = "",
    @SerialName("dimensiones_texto") val dimensionesTexto: String = "",
    val gramos: Double = 0.0,
    val horas: Double = 0.0,
    @SerialName("tiempo_texto") val tiempoTexto: String = "",
    val capas: Double = 0.0,
    val soportes: Boolean = false,
    val confianza: String = "media",
    @SerialName("confianza_texto") val confianzaTexto: String = "",
    val advertencias: List<String> = emptyList(),
    val cantidad: Int = 1,
    @SerialName("costo_material") val costoMaterial: String? = null,
    @SerialName("costo_maquina") val costoMaquina: String? = null,
    @SerialName("precio_unitario") val precioUnitario: String? = null,
    val subtotal: String? = null,
    val impuesto: String? = null,
    val total: String? = null,
    val aviso: String = ""
)

@Serializable
data class Estimar3dRespuestaDto(
    val analisis: JsonObject = JsonObject(emptyMap()),
    val estimacion: Estimacion3dDto = Estimacion3dDto()
)
