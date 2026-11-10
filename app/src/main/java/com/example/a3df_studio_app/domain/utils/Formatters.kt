package com.example.a3df_studio_app.domain.utils

import java.math.BigDecimal
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale

private val FORMATO_FECHA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
private val FORMATO_FECHA_HORA: DateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")

private fun analizarFecha(iso: String?): ZonedDateTime? {
    if (iso.isNullOrBlank()) return null
    return runCatching { OffsetDateTime.parse(iso).atZoneSameInstant(ZoneId.systemDefault()) }.getOrNull()
}

fun formatearFecha(iso: String?): String = analizarFecha(iso)?.format(FORMATO_FECHA) ?: "—"

fun formatearFechaHora(iso: String?): String = analizarFecha(iso)?.format(FORMATO_FECHA_HORA) ?: "—"

fun String?.aDinero(): BigDecimal = this?.trim()?.toBigDecimalOrNull() ?: BigDecimal.ZERO

fun BigDecimal.comoDinero(): String = String.format(Locale.US, "\$%,.2f", this)

fun formatearTamano(bytes: Long): String = when {
    bytes >= 1024L * 1024L -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
    bytes >= 1024L -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
    else -> "$bytes B"
}

fun soloDigitos(texto: String?): String = texto.orEmpty().filter { it.isDigit() }
