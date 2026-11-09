package com.example.a3df_studio_app.domain.utils

import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val localeEs: Locale = Locale.forLanguageTag("es-SV")
private val formatoFecha: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", localeEs)
private val formatoFechaHora: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a", localeEs)

fun Double.comoMoneda(): String = "$" + String.format(Locale.US, "%,.2f", this)

fun Double.comoCantidad(): String =
    if (this % 1.0 == 0.0) this.toLong().toString() else String.format(Locale.US, "%.2f", this)

fun OffsetDateTime?.comoFecha(): String =
    this?.atZoneSameInstant(ZoneId.systemDefault())?.format(formatoFecha) ?: "—"

fun OffsetDateTime?.comoFechaHora(): String =
    this?.atZoneSameInstant(ZoneId.systemDefault())?.format(formatoFechaHora) ?: "—"
