package com.example.a3df_studio_app.domain.mappers

import java.time.OffsetDateTime

internal fun String?.toFecha(): OffsetDateTime? =
    this?.let { runCatching { OffsetDateTime.parse(it) }.getOrNull() }
