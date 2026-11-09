package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.remote.dto.ResenaDto
import com.example.a3df_studio_app.data.remote.dto.SolicitudSoporteDto
import com.example.a3df_studio_app.domain.models.EstadoSoporte
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.Resena
import com.example.a3df_studio_app.domain.models.SolicitudSoporte

fun ResenaDto.toDomain(): Resena = Resena(
    id = id,
    idServicio = idServicio,
    idEncargo = idEncargo,
    puntuacion = puntuacion,
    comentario = comentario,
    fecha = fechaCreacion.toFecha()
)

fun SolicitudSoporteDto.toDomain(): SolicitudSoporte = SolicitudSoporte(
    id = id,
    asunto = asunto,
    descripcion = descripcion,
    prioridad = PrioridadSoporte.desde(prioridad),
    estado = EstadoSoporte.desde(estado),
    fecha = creadoEn.toFecha()
)
