package com.example.a3df_studio_app.domain.models

import java.math.BigDecimal

data class OpcionTexto(val codigo: String, val texto: String)

data class Filamento3d(val id: Long, val nombre: String)

data class Opciones3d(
    val disponible: Boolean,
    val filamentos: List<Filamento3d>,
    val rellenos: List<Int>,
    val calidades: List<OpcionTexto>,
    val escalas: List<Int>,
    val unidades: List<OpcionTexto>,
    val soportes: List<OpcionTexto>,
    val cantidadMaxima: Int,
    val formatos: List<String>,
    val tamanoMaximoMb: Int,
    val camaMm: List<Double>,
    val aviso: String
)

/** Opciones elegidas por el cliente para estimar y solicitar. */
data class Seleccion3d(
    val filamentoId: Long,
    val relleno: Int,
    val calidad: String,
    val escala: Int,
    val unidades: String,
    val soportes: String,
    val cantidad: Int
)

data class Estimacion3d(
    val cabe: Boolean,
    val mensajeCabida: String,
    val dimensionesTexto: String,
    val gramos: Double,
    val tiempoTexto: String,
    val soportes: Boolean,
    val confianza: String,
    val confianzaTexto: String,
    val advertencias: List<String>,
    val cantidad: Int,
    val precioUnitario: BigDecimal,
    val subtotal: BigDecimal,
    val impuesto: BigDecimal,
    val total: BigDecimal,
    val aviso: String
)

/** [analisisJson] es el análisis del modelo (texto JSON): se reenvía para recalcular sin volver a subir el archivo. */
data class Resultado3d(
    val analisisJson: String,
    val estimacion: Estimacion3d
)
