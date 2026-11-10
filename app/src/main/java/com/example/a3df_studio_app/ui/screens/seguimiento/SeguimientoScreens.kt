package com.example.a3df_studio_app.ui.screens.seguimiento

import android.widget.Toast
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.data.external.ArchivosExternos
import com.example.a3df_studio_app.domain.models.EstadoSoporte
import com.example.a3df_studio_app.domain.models.InfoNegocio
import com.example.a3df_studio_app.domain.models.Notificacion
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.TipoNotificacion
import com.example.a3df_studio_app.domain.utils.soloDigitos
import com.example.a3df_studio_app.ui.classes.primerError
import com.example.a3df_studio_app.ui.components.card.NotificacionCard
import com.example.a3df_studio_app.ui.components.card.SoporteCard
import com.example.a3df_studio_app.ui.components.common.BannerMensaje
import com.example.a3df_studio_app.ui.components.common.BotonCargarMas
import com.example.a3df_studio_app.ui.components.common.BotonPrimario
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.FilaDato
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ForgeTextField
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.helpers.color
import com.example.a3df_studio_app.ui.components.topbar.BackTopBar

@Composable
fun NotificacionesScreen(
    viewModel: NotificacionesViewModel,
    onBack: () -> Unit,
    onAbrir: (Notificacion) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(Unit) { viewModel.cargar(true) }
    val lista = estado.lista

    Scaffold(topBar = { BackTopBar("Notificaciones", onBack) }) { padding ->
        when {
            lista.cargando && lista.items.isEmpty() -> LoadingState(Modifier.padding(padding))
            lista.error != null && lista.items.isEmpty() -> ErrorState(lista.error, { viewModel.cargar(true) }, Modifier.padding(padding))
            lista.cargada && lista.items.isEmpty() -> MessageState(Icons.Rounded.Notifications, "Sin notificaciones", "Aquí verás los avisos de tus cotizaciones, pedidos y facturas.", Modifier.padding(padding))
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    OutlinedButton(onClick = { viewModel.marcarTodas() }, modifier = Modifier.fillMaxWidth()) { Text("Marcar todas como leídas") }
                }
                items(lista.items, key = { it.id }) { notificacion ->
                    NotificacionCard(notificacion, onClick = {
                        viewModel.marcarLeida(notificacion)
                        onAbrir(notificacion)
                    })
                }
                item { BotonCargarMas(lista.hayMas, lista.cargandoMas, { viewModel.cargar(false) }) }
            }
        }
    }
}

@Composable
fun SoporteScreen(
    viewModel: SoporteViewModel,
    negocio: InfoNegocio?,
    onBack: () -> Unit,
    onNueva: () -> Unit,
    onDetalle: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    LaunchedEffect(Unit) { viewModel.cargar(true) }
    val lista = estado.lista

    Scaffold(topBar = { BackTopBar("Soporte", onBack) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item { Button(onClick = onNueva, modifier = Modifier.fillMaxWidth()) { Text("Nueva solicitud de soporte") } }
            val telefono = negocio?.telefono
            if (!telefono.isNullOrBlank() && soloDigitos(telefono).length >= 8) {
                item {
                    OutlinedButton(
                        onClick = {
                            if (!ArchivosExternos.abrirEnlace(contexto, "https://wa.me/${soloDigitos(telefono)}")) {
                                Toast.makeText(contexto, "No se pudo abrir WhatsApp.", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) { Text("Escribir por WhatsApp") }
                }
            }
            when {
                lista.cargando && lista.items.isEmpty() -> item { LoadingState(Modifier.height(160.dp)) }
                lista.error != null && lista.items.isEmpty() -> item { ErrorState(lista.error, { viewModel.cargar(true) }) }
                lista.cargada && lista.items.isEmpty() -> item { MessageState(Icons.Rounded.SupportAgent, "Sin solicitudes", "Cuando abras una solicitud de soporte, la verás aquí.") }
                else -> {
                    items(lista.items, key = { it.id }) { solicitud -> SoporteCard(solicitud, onClick = { onDetalle(solicitud.id) }) }
                    item { BotonCargarMas(lista.hayMas, lista.cargandoMas, { viewModel.cargar(false) }) }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SoporteNuevoScreen(
    viewModel: SoporteViewModel,
    onBack: () -> Unit,
    onCreada: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(estado.creada) {
        estado.creada?.let { id ->
            viewModel.consumirCreada()
            onCreada(id)
        }
    }

    Scaffold(topBar = { BackTopBar("Nueva solicitud", onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ForgeTextField(estado.asunto, viewModel::cambiarAsunto, "Asunto", error = estado.campos.primerError("asunto"))
            ForgeTextField(
                estado.descripcion, viewModel::cambiarDescripcion, "Describe tu consulta o problema",
                error = estado.campos.primerError("descripcion"), ayuda = "Mínimo 10 caracteres.", lineas = 5
            )
            Text("Prioridad", style = MaterialTheme.typography.titleSmall)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(PrioridadSoporte.BAJA, PrioridadSoporte.MEDIA, PrioridadSoporte.ALTA).forEach { prioridad ->
                    FilterChip(
                        selected = estado.prioridad == prioridad,
                        onClick = { viewModel.cambiarPrioridad(prioridad) },
                        label = { Text(prioridad.etiqueta) }
                    )
                }
            }
            BannerMensaje(estado.error)
            BotonPrimario("Enviar solicitud", { viewModel.enviar() }, cargando = estado.enviando)
        }
    }
}

@Composable
fun SoporteDetalleScreen(
    id: Long,
    viewModel: SoporteViewModel,
    onBack: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(id) { viewModel.cargarDetalle(id) }

    Scaffold(topBar = { BackTopBar("Solicitud #$id", onBack) }) { padding ->
        val solicitud = estado.detalle
        when {
            estado.cargandoDetalle -> LoadingState(Modifier.padding(padding))
            estado.errorDetalle != null && solicitud == null -> ErrorState(estado.errorDetalle.orEmpty(), { viewModel.cargarDetalle(id) }, Modifier.padding(padding))
            solicitud != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ForgeCard(modifier = Modifier.fillMaxWidth()) {
                    Row {
                        Text(solicitud.asunto, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                        StatusChip(solicitud.estadoTexto, solicitud.estado.color())
                    }
                    Spacer(Modifier.height(8.dp))
                    FilaDato("Prioridad", solicitud.prioridad.etiqueta)
                    FilaDato("Creada", solicitud.creada)
                    FilaDato("Actualizada", solicitud.actualizada)
                }
                ForgeCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Descripción", style = MaterialTheme.typography.titleMedium)
                    Text(solicitud.descripcion, style = MaterialTheme.typography.bodyMedium)
                }
                BannerMensaje(estado.errorDetalle)
                if (solicitud.estado != EstadoSoporte.CERRADA) {
                    OutlinedButton(onClick = { viewModel.cerrar() }, enabled = !estado.cerrando, modifier = Modifier.fillMaxWidth()) { Text("Cerrar solicitud") }
                }
            }
        }
    }
}

/** Ruta a la que lleva una notificación según su tipo. */
fun rutaDeNotificacion(notificacion: Notificacion): String? {
    val id = notificacion.referenciaId ?: return null
    return when (notificacion.tipo) {
        TipoNotificacion.COTIZACION -> "cotizacion/$id"
        TipoNotificacion.ENCARGO -> "pedido/$id"
        TipoNotificacion.FACTURA -> "factura/$id"
        TipoNotificacion.SOPORTE -> "soporte/$id"
        TipoNotificacion.OTRO -> null
    }
}
