package com.example.a3df_studio_app.domain.models

data class Notificacion(
    val id: Long,
    val titulo: String,
    val mensaje: String,
    val tipo: TipoNotificacion,
    val referenciaId: Long?,
    val leida: Boolean,
    val fecha: String
)

data class SolicitudSoporte(
    val id: Long,
    val asunto: String,
    val descripcion: String,
    val prioridad: PrioridadSoporte,
    val estado: EstadoSoporte,
    val estadoTexto: String,
    val creada: String,
    val actualizada: String
)

data class ArchivoLocal(
    val uri: String,
    val nombre: String,
    val tamano: Long
)

data class SolicitudCotizacion(
    val servicioId: Long,
    val cantidad: String,
    val titulo: String,
    val solicitud: String,
    val material: String,
    val color: String,
    val dimensiones: String,
    val archivos: List<ArchivoLocal>
)
