package com.example.a3df_studio_app.ui.components.card

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.EstadoEncargo
import com.example.a3df_studio_app.domain.utils.comoFecha
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.helpers.color

@Composable
fun EncargoCard(encargo: Encargo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(encargo.codigo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(encargo.estado.etiqueta, encargo.estado.color())
        }
        Text(
            "Solicitado el ${encargo.fechaSolicitud.comoFecha()} · ${encargo.detalles.size} artículo(s)",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(10.dp))
        if (encargo.estado.paso >= 0) {
            LinearProgressIndicator(
                progress = { encargo.estado.paso / EstadoEncargo.TOTAL_PASOS.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(10.dp))
        }
        Text("Total ${encargo.total.comoMoneda()}", style = MaterialTheme.typography.titleSmall)
    }
}
