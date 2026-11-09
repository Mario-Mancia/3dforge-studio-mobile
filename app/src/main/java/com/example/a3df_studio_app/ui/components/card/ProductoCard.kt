package com.example.a3df_studio_app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AddShoppingCart
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ImageOrIcon

@Composable
fun ProductoCard(
    producto: Producto,
    cantidad: Int,
    onAgregar: () -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ImageOrIcon(url = producto.imagenUrl, icono = Icons.Outlined.Category)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
                producto.descripcion?.let {
                    Text(
                        it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(Modifier.height(4.dp))
                Text(producto.precio.comoMoneda(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
                Text(
                    if (producto.disponible) "${producto.stock} disponibles" else "Agotado",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (producto.disponible) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(12.dp))
        if (cantidad == 0) {
            FilledTonalButton(
                onClick = onAgregar,
                enabled = producto.disponible,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Rounded.AddShoppingCart, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Agregar al pedido")
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                FilledTonalIconButton(onClick = onQuitar) { Icon(Icons.Rounded.Remove, contentDescription = "Quitar") }
                Text("$cantidad en el pedido", style = MaterialTheme.typography.titleSmall)
                FilledTonalIconButton(onClick = onAgregar, enabled = cantidad < producto.stock) {
                    Icon(Icons.Rounded.Add, contentDescription = "Agregar")
                }
            }
        }
    }
}
