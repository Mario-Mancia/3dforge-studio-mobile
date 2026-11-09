package com.example.a3df_studio_app.ui.screens.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.utils.comoCantidad
import com.example.a3df_studio_app.domain.utils.comoFecha
import com.example.a3df_studio_app.domain.utils.comoFechaHora
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.helpers.color
import androidx.compose.material.icons.outlined.SearchOff

@Composable
fun EncargoDetalleScreen(
    pedidosViewModel: PedidosViewModel,
    idEncargo: Long
) {
    val uiState by pedidosViewModel.uiState.collectAsState()
    val encargo = uiState.encargos.firstOrNull { it.id == idEncargo }

    if (encargo == null) {
        MessageState(Icons.Outlined.SearchOff, "Encargo no encontrado", null)
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ForgeCard(Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(encargo.codigo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                    StatusChip(encargo.estado.etiqueta, encargo.estado.color())
                }
                Spacer(Modifier.height(8.dp))
                Text("Solicitado: ${encargo.fechaSolicitud.comoFecha()}", style = MaterialTheme.typography.bodyMedium)
                Text("Entrega estimada: ${encargo.fechaEntregaEstimada.comoFecha()}", style = MaterialTheme.typography.bodyMedium)
                encargo.observaciones?.let { Text("Notas: $it", style = MaterialTheme.typography.bodySmall) }
            }
        }
        item { Text("Detalle", style = MaterialTheme.typography.titleMedium) }
        item {
            ForgeCard(Modifier.fillMaxWidth()) {
                encargo.detalles.forEach { detalle ->
                    Row(Modifier.padding(vertical = 4.dp)) {
                        Text("${detalle.descripcion} x ${detalle.cantidad.comoCantidad()}", modifier = Modifier.weight(1f))
                        Text(detalle.subtotal.comoMoneda())
                    }
                }
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                FilaTotal("Subtotal", encargo.subtotal.comoMoneda())
                FilaTotal("IVA", encargo.impuesto.comoMoneda())
                if (encargo.descuento > 0) FilaTotal("Descuento", "-${encargo.descuento.comoMoneda()}")
                FilaTotal("Total", encargo.total.comoMoneda(), destacado = true)
            }
        }
        if (encargo.historial.isNotEmpty()) {
            item { Text("Seguimiento", style = MaterialTheme.typography.titleMedium) }
            items(encargo.historial, key = { it.id }) { evento ->
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Rounded.CheckCircle, contentDescription = null, tint = evento.estadoNuevo.color(), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(evento.estadoNuevo.etiqueta, style = MaterialTheme.typography.titleSmall)
                        Text(evento.fecha.comoFechaHora(), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        evento.comentario?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaTotal(etiqueta: String, valor: String, destacado: Boolean = false) {
    val estilo = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium
    Row(Modifier.fillMaxWidth()) {
        Text(etiqueta, style = estilo, modifier = Modifier.weight(1f))
        Text(valor, style = estilo, color = if (destacado) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface)
    }
}
