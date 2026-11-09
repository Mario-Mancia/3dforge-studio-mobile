package com.example.a3df_studio_app.ui.components.card

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.Promocion
import com.example.a3df_studio_app.domain.models.TipoDescuento
import com.example.a3df_studio_app.domain.utils.comoFecha
import com.example.a3df_studio_app.domain.utils.comoMoneda

@Composable
fun PromocionCard(promocion: Promocion, modifier: Modifier = Modifier) {
    val etiqueta = when (promocion.tipoDescuento) {
        TipoDescuento.PORCENTAJE -> "${promocion.valorDescuento.toInt()}% OFF"
        TipoDescuento.MONTO_FIJO -> "${promocion.valorDescuento.comoMoneda()} de descuento"
    }
    Card(
        modifier = modifier.width(280.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Column(Modifier.padding(20.dp)) {
            Text(etiqueta, style = MaterialTheme.typography.headlineSmall)
            Text(promocion.nombre, style = MaterialTheme.typography.titleMedium)
            promocion.descripcion?.let {
                Text(it, style = MaterialTheme.typography.bodySmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
            }
            Text("Válido hasta ${promocion.fechaFin.comoFecha()}", style = MaterialTheme.typography.labelSmall)
        }
    }
}
