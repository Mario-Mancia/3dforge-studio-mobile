package com.example.a3df_studio_app.ui.classes

import android.content.Intent
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.ItemCotizacion
import com.example.a3df_studio_app.domain.models.Servicio

data class CotizacionesUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val servicios: List<Servicio> = emptyList(),
    val seleccion: List<ItemCotizacion> = emptyList(),
    val observaciones: String = "",
    val misCotizaciones: List<Cotizacion> = emptyList(),
    val temaAsesoria: String = "",
    val detalleAsesoria: String = "",
    val horarioAsesoria: String = "",
    val isSending: Boolean = false,
    val mensaje: String? = null,
    val whatsAppIntent: Intent? = null
) {
    val totalEstimado: Double
        get() = seleccion.sumOf { it.subtotalEstimado }
}
