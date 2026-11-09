package com.example.a3df_studio_app.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PrecisionManufacturing
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ImageOrIcon
import com.example.a3df_studio_app.ui.components.common.StatusChip

@Composable
fun ServicioCard(
    servicio: Servicio,
    accion: String,
    onAccion: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.Top) {
            ImageOrIcon(url = null, icono = Icons.Outlined.PrecisionManufacturing, size = 56.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                servicio.categoriaNombre?.let {
                    StatusChip(it)
                    Spacer(Modifier.height(6.dp))
                }
                Text(servicio.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    servicio.descripcion,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text("Desde", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(servicio.precioBase.comoMoneda(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            if (servicio.requiereArchivo) {
                Text("Requiere archivo 3D", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.width(8.dp))
            }
            TextButton(onClick = onAccion) {
                Icon(Icons.Outlined.PrecisionManufacturing, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(accion)
            }
        }
    }
}
