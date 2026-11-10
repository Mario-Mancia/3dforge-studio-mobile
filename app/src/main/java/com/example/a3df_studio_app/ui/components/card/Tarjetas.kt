package com.example.a3df_studio_app.ui.components.card

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.ArchivoAdjunto
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.Disponibilidad
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.ItemCarrito
import com.example.a3df_studio_app.domain.models.Notificacion
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.models.SolicitudSoporte
import com.example.a3df_studio_app.domain.utils.comoDinero
import com.example.a3df_studio_app.domain.utils.formatearTamano
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ImageOrIcon
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.helpers.color
import com.example.a3df_studio_app.ui.theme.ForgeSuccess
import com.example.a3df_studio_app.ui.theme.ForgeWarning

@Composable
fun ProductoCard(
    producto: Producto,
    onClick: () -> Unit,
    onAgregar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ImageOrIcon(url = producto.imagenUrl, icono = Icons.Rounded.Inventory2, size = 84.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(2.dp))
                Text(producto.precio.comoDinero(), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                Spacer(Modifier.height(4.dp))
                StatusChip(producto.disponibilidad.etiqueta, colorDisponibilidad(producto.disponibilidad))
            }
        }
        Spacer(Modifier.height(10.dp))
        Button(onClick = onAgregar, enabled = !producto.agotado, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Rounded.Add, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(if (producto.agotado) "Agotado" else "Agregar al carrito")
        }
    }
}

@Composable
fun colorDisponibilidad(disponibilidad: Disponibilidad): Color = when (disponibilidad) {
    Disponibilidad.DISPONIBLE -> ForgeSuccess
    Disponibilidad.ULTIMAS -> ForgeWarning
    Disponibilidad.AGOTADO -> MaterialTheme.colorScheme.error
}

@Composable
fun ServicioCard(
    servicio: Servicio,
    onClick: () -> Unit,
    onCotizar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ImageOrIcon(url = null, icono = Icons.Rounded.Handyman, size = 56.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(servicio.nombre, style = MaterialTheme.typography.titleMedium, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(servicio.categoria, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.height(2.dp))
                Text("Desde ${servicio.precioBase.comoDinero()}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
            }
        }
        if (servicio.descripcion.isNotBlank()) {
            Spacer(Modifier.height(8.dp))
            Text(servicio.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(Modifier.height(10.dp))
        OutlinedButton(onClick = onCotizar, modifier = Modifier.fillMaxWidth()) { Text("Solicitar cotización") }
    }
}

@Composable
fun ItemCarritoCard(
    item: ItemCarrito,
    onMas: () -> Unit,
    onMenos: () -> Unit,
    onQuitar: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            ImageOrIcon(url = item.imagenUrl, icono = Icons.Rounded.Inventory2, size = 64.dp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.nombre, style = MaterialTheme.typography.titleSmall, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text("${item.precio.comoDinero()} c/u", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(item.subtotal.comoDinero(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
            }
            IconButton(onClick = onQuitar) { Icon(Icons.Rounded.Delete, contentDescription = "Quitar", tint = MaterialTheme.colorScheme.error) }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            IconButton(onClick = onMenos) { Icon(Icons.Rounded.Remove, contentDescription = "Menos") }
            Text("${item.cantidad}", style = MaterialTheme.typography.titleMedium)
            IconButton(onClick = onMas) { Icon(Icons.Rounded.Add, contentDescription = "Más") }
            Text("Stock: ${item.stock}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
fun EncargoCard(encargo: Encargo, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(encargo.codigo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(encargo.estadoTexto, encargo.estado.color())
        }
        if (encargo.descripcion.isNotBlank()) {
            Text(encargo.descripcion, style = MaterialTheme.typography.bodyMedium, maxLines = 2, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        val progreso = encargo.progreso
        if (progreso != null) {
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(progress = { progreso / 100f }, modifier = Modifier.fillMaxWidth())
        }
        Spacer(Modifier.height(8.dp))
        Row {
            Text(encargo.fechaSolicitud, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(encargo.total.comoDinero(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
    }
}

@Composable
fun CotizacionCard(cotizacion: Cotizacion, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(cotizacion.codigo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(if (cotizacion.vencida) "Vencida" else cotizacion.estadoTexto, cotizacion.estado.color())
        }
        Spacer(Modifier.height(4.dp))
        Row {
            Text("Emitida ${cotizacion.fechaEmision}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(cotizacion.total.comoDinero(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (cotizacion.puedeResponder) {
            Spacer(Modifier.height(6.dp))
            Text("Esperando tu respuesta", style = MaterialTheme.typography.labelLarge, color = ForgeWarning)
        }
    }
}

@Composable
fun FacturaCard(factura: Factura, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(factura.numero, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
            StatusChip(factura.estadoTexto, factura.estado.color())
        }
        Spacer(Modifier.height(4.dp))
        Row {
            Text("Vence ${factura.fechaVencimiento ?: "—"}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.weight(1f))
            Text(factura.total.comoDinero(), style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.primary)
        }
        if (factura.saldoPendiente.signum() > 0) {
            Text("Saldo pendiente: ${factura.saldoPendiente.comoDinero()}", style = MaterialTheme.typography.labelLarge, color = ForgeWarning)
        }
    }
}

@Composable
fun NotificacionCard(notificacion: Notificacion, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                notificacion.titulo,
                style = if (notificacion.leida) MaterialTheme.typography.titleSmall else MaterialTheme.typography.titleMedium,
                modifier = Modifier.weight(1f)
            )
            if (!notificacion.leida) StatusChip("Nueva")
        }
        Spacer(Modifier.height(4.dp))
        Text(notificacion.mensaje, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(4.dp))
        Text(notificacion.fecha, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SoporteCard(solicitud: SolicitudSoporte, onClick: () -> Unit, modifier: Modifier = Modifier) {
    ForgeCard(modifier = modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("#${solicitud.id} · ${solicitud.asunto}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f), maxLines = 2, overflow = TextOverflow.Ellipsis)
            StatusChip(solicitud.estadoTexto, solicitud.estado.color())
        }
        Spacer(Modifier.height(4.dp))
        Text("Prioridad ${solicitud.prioridad.etiqueta} · ${solicitud.creada}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun ArchivoFila(
    archivo: ArchivoAdjunto,
    descargando: Boolean,
    onDescargar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
        Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(archivo.nombre, style = MaterialTheme.typography.bodyMedium, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text("${archivo.formato} · ${formatearTamano(archivo.tamanoBytes)}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        IconButton(onClick = onDescargar, enabled = !descargando) {
            Icon(Icons.Rounded.Download, contentDescription = "Descargar y abrir")
        }
    }
}
