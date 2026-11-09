package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.MarcaTarjeta
import com.example.a3df_studio_app.domain.models.PagoRegistrado

data class PagoUiState(
    val numero: String = "",
    val titular: String = "",
    val expiracion: String = "",
    val cvv: String = "",
    val marca: MarcaTarjeta = MarcaTarjeta.DESCONOCIDA,
    val isProcessing: Boolean = false,
    val error: String? = null,
    val resultado: PagoRegistrado? = null
)
