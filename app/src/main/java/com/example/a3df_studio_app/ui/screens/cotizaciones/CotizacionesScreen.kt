package com.example.a3df_studio_app.ui.screens.cotizaciones

import android.content.ActivityNotFoundException
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Chat
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.utils.comoCantidad
import com.example.a3df_studio_app.domain.utils.comoFecha
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.helpers.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CotizacionesScreen(
    cotizacionesViewModel: CotizacionesViewModel,
    isLoggedIn: Boolean,
    snackbarHostState: SnackbarHostState,
    pestanaInicial: Int,
    onRequireLogin: () -> Unit
) {
    val uiState by cotizacionesViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var pestana by rememberSaveable(pestanaInicial) { mutableIntStateOf(pestanaInicial) }

    LaunchedEffect(isLoggedIn) { if (isLoggedIn) cotizacionesViewModel.cargarMisCotizaciones() }

    LaunchedEffect(uiState.whatsAppIntent) {
        uiState.whatsAppIntent?.let { intent ->
            try {
                context.startActivity(intent)
            } catch (e: ActivityNotFoundException) {
                snackbarHostState.showSnackbar("No se encontró una aplicación para abrir WhatsApp.")
            }
            cotizacionesViewModel.consumirIntent()
        }
    }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            cotizacionesViewModel.consumirMensaje()
        }
    }

    val enviar: (() -> Unit) -> Unit = { accion -> if (isLoggedIn) accion() else onRequireLogin() }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pestana) {
            listOf("Cotizar", "Asesoría", "Historial").forEachIndexed { indice, titulo ->
                Tab(selected = pestana == indice, onClick = { pestana = indice }, text = { Text(titulo) })
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            when (pestana) {
                0 -> {
                    item {
                        Text(
                            "Arma tu cotización con nuestros servicios o describe tu proyecto. La enviaremos por WhatsApp a un asesor.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    items(uiState.seleccion, key = { it.servicio.id }) { item ->
                        ForgeCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text(item.servicio.nombre, style = MaterialTheme.typography.titleSmall)
                                    Text(item.subtotalEstimado.comoMoneda(), color = MaterialTheme.colorScheme.primary)
                                }
                                FilledTonalIconButton(onClick = { cotizacionesViewModel.cambiarCantidad(item.servicio.id, -1) }) {
                                    Icon(Icons.Rounded.Remove, contentDescription = "Menos")
                                }
                                Text(item.cantidad.comoCantidad(), style = MaterialTheme.typography.titleMedium)
                                FilledTonalIconButton(onClick = { cotizacionesViewModel.cambiarCantidad(item.servicio.id, 1) }) {
                                    Icon(Icons.Rounded.Add, contentDescription = "Más")
                                }
                            }
                        }
                    }
                    item {
                        Text("Agregar servicios", style = MaterialTheme.typography.titleSmall)
                        Spacer(Modifier.height(4.dp))
                        if (uiState.isLoading) LoadingState(Modifier.height(80.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            uiState.servicios
                                .filter { s -> uiState.seleccion.none { it.servicio.id == s.id } }
                                .forEach { servicio ->
                                    AssistChip(
                                        onClick = { cotizacionesViewModel.agregarServicio(servicio) },
                                        label = { Text("${servicio.nombre} · ${servicio.precioBase.comoMoneda()}") },
                                        leadingIcon = { Icon(Icons.Rounded.Add, contentDescription = null) }
                                    )
                                }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.observaciones,
                            onValueChange = cotizacionesViewModel::onObservaciones,
                            label = { Text("Describe tu proyecto, medidas, material o color") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        if (uiState.seleccion.isNotEmpty()) {
                            Text(
                                "Estimado: ${uiState.totalEstimado.comoMoneda()} + IVA",
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(Modifier.height(8.dp))
                        }
                        BotonWhatsApp("Solicitar cotización", uiState.isSending) {
                            enviar(cotizacionesViewModel::solicitarCotizacion)
                        }
                    }
                }
                1 -> {
                    item {
                        Text(
                            "Agenda una asesoría con nuestro equipo para elegir material, técnica de impresión o resolver dudas de diseño.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.temaAsesoria,
                            onValueChange = cotizacionesViewModel::onTema,
                            label = { Text("Tema de la asesoría") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.detalleAsesoria,
                            onValueChange = cotizacionesViewModel::onDetalle,
                            label = { Text("Detalle (opcional)") },
                            minLines = 3,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = uiState.horarioAsesoria,
                            onValueChange = cotizacionesViewModel::onHorario,
                            label = { Text("Horario preferido (opcional)") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        BotonWhatsApp("Agendar por WhatsApp", uiState.isSending) {
                            enviar(cotizacionesViewModel::solicitarAsesoria)
                        }
                    }
                }
                else -> {
                    if (!isLoggedIn) {
                        item {
                            MessageState(Icons.Outlined.RequestQuote, "Inicia sesión para ver tus cotizaciones", null, accion = "Iniciar sesión", onAccion = onRequireLogin)
                        }
                    } else if (uiState.misCotizaciones.isEmpty()) {
                        item { MessageState(Icons.Outlined.RequestQuote, "Aún no tienes cotizaciones", null) }
                    }
                    items(uiState.misCotizaciones, key = { it.id }) { cotizacion ->
                        ForgeCard(Modifier.fillMaxWidth()) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(cotizacion.codigo, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                StatusChip(cotizacion.estado.etiqueta, cotizacion.estado.color())
                            }
                            Text(
                                "Emitida ${cotizacion.fechaEmision.comoFecha()} · vence ${cotizacion.fechaVencimiento.comoFecha()}",
                                style = MaterialTheme.typography.bodySmall
                            )
                            cotizacion.detalles.forEach {
                                Text("• ${it.descripcion} x ${it.cantidad.comoCantidad()}", style = MaterialTheme.typography.bodySmall)
                            }
                            Text("Total ${cotizacion.total.comoMoneda()}", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BotonWhatsApp(texto: String, enviando: Boolean, onClick: () -> Unit) {
    Button(onClick = onClick, enabled = !enviando, modifier = Modifier.fillMaxWidth()) {
        if (enviando) {
            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
        } else {
            Icon(Icons.Rounded.Chat, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text(texto)
        }
    }
}
