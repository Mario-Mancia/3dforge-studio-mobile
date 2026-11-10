package com.example.a3df_studio_app.ui.screens.carrito

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.utils.comoDinero
import com.example.a3df_studio_app.ui.components.card.ItemCarritoCard
import com.example.a3df_studio_app.ui.components.common.BannerMensaje
import com.example.a3df_studio_app.ui.components.common.BloqueTotales
import com.example.a3df_studio_app.ui.components.common.BotonPrimario
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.MessageState

@Composable
fun CarritoScreen(
    viewModel: CarritoViewModel,
    usuario: AuthUser?,
    onCatalogo: () -> Unit,
    onRequiereSesion: () -> Unit,
    onPedidoCreado: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()

    LaunchedEffect(estado.pedidoCreado) {
        estado.pedidoCreado?.let {
            viewModel.consumirPedido()
            onPedidoCreado(it)
        }
    }

    val validado = estado.validado
    if (validado != null) {
        AlertDialog(
            onDismissRequest = { viewModel.cerrarRevision() },
            title = { Text(if (validado.hayProblemas) "Revisa tu carrito" else "Confirmar compra") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    validado.lineas.forEach { linea ->
                        val texto = if (linea.disponible) "${linea.cantidad} × ${linea.nombre}" else "${linea.nombre}: sin existencias suficientes"
                        Text(texto, color = if (linea.disponible) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.error)
                    }
                    Spacer(Modifier.height(6.dp))
                    if (!validado.hayProblemas) {
                        BloqueTotales(validado.subtotal, validado.impuesto, validado.total, descuento = validado.descuento)
                        Text(
                            "El pago es en efectivo y se coordina con nuestro equipo. Recibirás un aviso cuando tu pedido cambie de estado.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Text("Ajusta las cantidades o quita los productos sin existencias y vuelve a intentarlo.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            },
            confirmButton = {
                if (!validado.hayProblemas) {
                    Button(onClick = { viewModel.confirmar() }, enabled = !estado.comprando) {
                        if (estado.comprando) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp) else Text("Confirmar compra")
                    }
                }
            },
            dismissButton = { TextButton(onClick = { viewModel.cerrarRevision() }) { Text("Volver") } }
        )
    }

    if (estado.items.isEmpty()) {
        MessageState(
            icono = Icons.Rounded.ShoppingCart,
            titulo = "Tu carrito está vacío",
            mensaje = "Agrega productos desde el catálogo.",
            modifier = modifier.fillMaxSize(),
            accion = "Ir al catálogo",
            onAccion = onCatalogo
        )
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(estado.items, key = { it.productoId }) { item ->
            ItemCarritoCard(
                item = item,
                onMas = { viewModel.cambiarCantidad(item.productoId, item.cantidad + 1) },
                onMenos = { viewModel.cambiarCantidad(item.productoId, item.cantidad - 1) },
                onQuitar = { viewModel.quitar(item.productoId) }
            )
        }
        item {
            val totales = estado.totales
            ForgeCard(modifier = Modifier.fillMaxWidth()) {
                if (totales != null) BloqueTotales(totales.subtotal, totales.impuesto, totales.total, estimado = true)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Estimación con impuesto del 13 %. Al confirmar, el servidor valida precios y existencias.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        item { BannerMensaje(estado.error) }
        item {
            BotonPrimario(
                texto = if (usuario == null) "Iniciar sesión para comprar" else "Revisar y confirmar compra",
                onClick = { if (usuario == null) onRequiereSesion() else viewModel.revisar() },
                cargando = estado.validando
            )
        }
        item {
            TextButton(onClick = { viewModel.vaciar() }, modifier = Modifier.fillMaxWidth()) { Text("Vaciar carrito") }
        }
    }
}
