package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.Resena
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.models.SolicitudSoporte

data class FeedbackUiState(
    val isLoading: Boolean = false,
    val servicios: List<Servicio> = emptyList(),
    val encargos: List<Encargo> = emptyList(),
    val misResenas: List<Resena> = emptyList(),
    val misSolicitudes: List<SolicitudSoporte> = emptyList(),
    val puntuacion: Int = 0,
    val comentario: String = "",
    val servicioSeleccionado: Long? = null,
    val encargoSeleccionado: Long? = null,
    val asunto: String = "",
    val descripcion: String = "",
    val prioridad: PrioridadSoporte = PrioridadSoporte.MEDIA,
    val isSending: Boolean = false,
    val mensaje: String? = null,
    val error: String? = null
)
