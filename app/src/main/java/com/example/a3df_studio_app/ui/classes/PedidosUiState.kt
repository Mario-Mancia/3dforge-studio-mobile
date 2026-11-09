package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.Pago

data class PedidosUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val encargos: List<Encargo> = emptyList(),
    val facturas: List<Factura> = emptyList(),
    val pagos: List<Pago> = emptyList(),
    val cargado: Boolean = false
)
