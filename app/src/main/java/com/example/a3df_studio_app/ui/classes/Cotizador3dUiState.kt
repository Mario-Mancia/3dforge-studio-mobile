package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.ArchivoLocal
import com.example.a3df_studio_app.domain.models.Estimacion3d
import com.example.a3df_studio_app.domain.models.Opciones3d
import com.example.a3df_studio_app.domain.models.Seleccion3d

data class Cotizar3dUiState(
    val cargandoOpciones: Boolean = true,
    val errorOpciones: String? = null,
    val opciones: Opciones3d? = null,
    val seleccion: Seleccion3d? = null,
    val archivo: ArchivoLocal? = null,
    /** Análisis del modelo devuelto por el servidor: permite recalcular al cambiar opciones sin volver a subir el archivo. */
    val analisisJson: String? = null,
    val estimacion: Estimacion3d? = null,
    val estimando: Boolean = false,
    val errorEstimacion: String? = null,
    val nombrePieza: String = "",
    val comentarios: String = "",
    val enviando: Boolean = false,
    val error: String? = null,
    val campos: Map<String, List<String>> = emptyMap(),
    val creada: Long? = null
)
