package com.example.a3df_studio_app.ui.components.helpers

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.a3df_studio_app.domain.models.EstadoCotizacion
import com.example.a3df_studio_app.domain.models.EstadoEncargo
import com.example.a3df_studio_app.domain.models.EstadoFactura
import com.example.a3df_studio_app.domain.models.EstadoSoporte
import com.example.a3df_studio_app.ui.theme.ForgeSuccess
import com.example.a3df_studio_app.ui.theme.ForgeWarning

@Composable
fun EstadoEncargo.color(): Color = when (this) {
    EstadoEncargo.CANCELADO -> MaterialTheme.colorScheme.error
    EstadoEncargo.ENTREGADO, EstadoEncargo.FINALIZADO -> ForgeSuccess
    EstadoEncargo.EN_ESPERA, EstadoEncargo.PENDIENTE -> ForgeWarning
    else -> MaterialTheme.colorScheme.primary
}

@Composable
fun EstadoFactura.color(): Color = when (this) {
    EstadoFactura.PAGADA -> ForgeSuccess
    EstadoFactura.ANULADA, EstadoFactura.VENCIDA -> MaterialTheme.colorScheme.error
    else -> ForgeWarning
}

@Composable
fun EstadoCotizacion.color(): Color = when (this) {
    EstadoCotizacion.ACEPTADA -> ForgeSuccess
    EstadoCotizacion.RECHAZADA, EstadoCotizacion.CANCELADA, EstadoCotizacion.VENCIDA -> MaterialTheme.colorScheme.error
    EstadoCotizacion.SOLICITADA -> ForgeWarning
    else -> MaterialTheme.colorScheme.primary
}

@Composable
fun EstadoSoporte.color(): Color = when (this) {
    EstadoSoporte.RESUELTA -> ForgeSuccess
    EstadoSoporte.CERRADA -> MaterialTheme.colorScheme.onSurfaceVariant
    EstadoSoporte.ABIERTA -> ForgeWarning
    else -> MaterialTheme.colorScheme.primary
}
