package com.example.a3df_studio_app.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.utils.comoFecha
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.helpers.color

@Composable
fun FacturaCard(factura: Factura, onPagar: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(factura.numero, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(factura.estado.etiqueta, factura.estado.color())
        }
        Text(
            "Emitida ${factura.fechaEmision.comoFecha()} · vence ${factura.fechaVencimiento.comoFecha()}",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Total ${factura.total.comoMoneda()}", style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Pendiente ${factura.saldoPendiente.comoMoneda()}",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            if (factura.pagable) {
                Button(onClick = onPagar) {
                    Icon(Icons.Rounded.CreditCard, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Pagar")
                }
            }
        }
    }
}
