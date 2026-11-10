package com.example.a3df_studio_app.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.ui.Alignment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.utils.comoDinero
import com.example.a3df_studio_app.ui.components.card.ProductoCard
import com.example.a3df_studio_app.ui.components.card.ServicioCard
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.SectionTitle

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    usuario: AuthUser?,
    onCatalogo: () -> Unit,
    onProducto: (Long) -> Unit,
    onServicio: (Long) -> Unit,
    onCotizar: (Long?) -> Unit,
    onAgregar: (Producto) -> Unit,
    onCotizaciones: () -> Unit,
    onFacturas: () -> Unit,
    onSoporte: () -> Unit,
    onIniciarSesion: () -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(usuario?.id) { viewModel.cargar(usuario != null) }

    if (estado.cargando && estado.negocio == null && estado.destacados.isEmpty()) {
        LoadingState(modifier)
        return
    }
    if (estado.error != null && estado.destacados.isEmpty() && estado.servicios.isEmpty()) {
        ErrorState(estado.error.orEmpty(), { viewModel.cargar(usuario != null) }, modifier)
        return
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ForgeCard(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp).fillMaxWidth()) {
                Text(
                    if (usuario != null) "Hola, ${usuario.primerNombre}" else "Bienvenido a ${estado.negocio?.nombre ?: "3DForge Studio"}",
                    style = MaterialTheme.typography.headlineSmall
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "Impresión 3D, modelado y productos. Cotiza o compra en minutos.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onCatalogo, modifier = Modifier.weight(1f)) { Text("Ver catálogo") }
                    OutlinedButton(onClick = { onCotizar(null) }, modifier = Modifier.weight(1f)) { Text("Cotizar") }
                }
                if (usuario == null) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onIniciarSesion, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión o registrarme") }
                }
            }
        }

        val panel = estado.panel
        if (usuario != null && panel != null) {
            item { SectionTitle("Tu resumen") }
            item {
                ForgeCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Dato("Por responder", panel.porResponder.toString())
                        Dato("En revisión", panel.enRevision.toString())
                        Dato("Pedidos en curso", panel.pedidosEnCurso.toString())
                    }
                    Spacer(Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Dato("Saldo pendiente", panel.saldoPendiente.comoDinero())
                        Dato("Soporte abierto", panel.soporteAbierto.toString())
                    }
                }
            }
            item {
                Row(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Acceso("Cotizaciones", Icons.Rounded.RequestQuote, onCotizaciones, Modifier.weight(1f))
                    Acceso("Facturas", Icons.Rounded.ReceiptLong, onFacturas, Modifier.weight(1f))
                    Acceso("Soporte", Icons.Rounded.SupportAgent, onSoporte, Modifier.weight(1f))
                }
            }
        }

        if (estado.destacados.isNotEmpty()) {
            item { SectionTitle("Productos destacados", accion = "Ver todos", onAccion = onCatalogo) }
            items(estado.destacados, key = { "p-${it.id}" }) { producto ->
                ProductoCard(
                    producto = producto,
                    onClick = { onProducto(producto.id) },
                    onAgregar = { onAgregar(producto) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        if (estado.servicios.isNotEmpty()) {
            item { SectionTitle("Servicios") }
            items(estado.servicios, key = { "s-${it.id}" }) { servicio ->
                ServicioCard(
                    servicio = servicio,
                    onClick = { onServicio(servicio.id) },
                    onCotizar = { onCotizar(servicio.id) },
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }

        estado.negocio?.let { negocio ->
            item {
                ForgeCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(negocio.nombre, style = MaterialTheme.typography.titleMedium)
                            negocio.telefono?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            negocio.correo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                            negocio.direccion?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Column {
        Text(valor, style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
        Text(etiqueta, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun Acceso(
    texto: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ForgeCard(modifier = modifier, onClick = onClick) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.height(4.dp))
            Text(texto, style = MaterialTheme.typography.labelMedium)
        }
    }
}
