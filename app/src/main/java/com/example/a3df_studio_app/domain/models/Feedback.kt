package com.example.a3df_studio_app.domain.models

import java.time.OffsetDateTime

data class Resena(
    val id: Long,
    val idServicio: Long?,
    val idEncargo: Long?,
    val puntuacion: Int,
    val comentario: String?,
    val fecha: OffsetDateTime?
)

data class SolicitudSoporte(
    val id: Long,
    val asunto: String,
    val descripcion: String,
    val prioridad: PrioridadSoporte,
    val estado: EstadoSoporte,
    val fecha: OffsetDateTime?
)
