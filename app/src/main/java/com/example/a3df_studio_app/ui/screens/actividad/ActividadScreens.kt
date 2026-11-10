package com.example.a3df_studio_app.ui.screens.actividad

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.data.external.ArchivosExternos
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.DetalleDocumento
import com.example.a3df_studio_app.domain.utils.comoDinero
import com.example.a3df_studio_app.ui.classes.DescargaLista
import com.example.a3df_studio_app.ui.classes.ListaEstado
import com.example.a3df_studio_app.ui.classes.PestanaActividad
import com.example.a3df_studio_app.ui.components.card.ArchivoFila
import com.example.a3df_studio_app.ui.components.card.CotizacionCard
import com.example.a3df_studio_app.ui.components.card.EncargoCard
import com.example.a3df_studio_app.ui.components.card.FacturaCard
import com.example.a3df_studio_app.ui.components.common.AuthRequiredState
import com.example.a3df_studio_app.ui.components.common.BloqueTotales
import com.example.a3df_studio_app.ui.components.common.BotonCargarMas
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.FilaDato
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.components.common.SectionTitle
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.dialog.ConfirmarDialog
import com.example.a3df_studio_app.ui.components.helpers.color
import com.example.a3df_studio_app.ui.components.topbar.BackTopBar
import com.example.a3df_studio_app.ui.theme.ForgeWarning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActividadScreen(
    viewModel: ActividadViewModel,
    usuario: AuthUser?,
    onIniciarSesion: () -> Unit,
    onPedido: (Long) -> Unit,
    onCotizacion: (Long) -> Unit,
    onFactura: (Long) -> Unit,
    onNuevaCotizacion: () -> Unit,
    onCotizacionAutomatica: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (usuario == null) {
        AuthRequiredState(
            titulo = "Inicia sesión para ver tu actividad",
            mensaje = "Aquí encontrarás tus pedidos, cotizaciones y facturas.",
            onLogin = onIniciarSesion,
            modifier = modifier
        )
        return
    }
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(usuario.id) { viewModel.iniciar() }

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = estado.pestana == PestanaActividad.PEDIDOS, onClick = { viewModel.seleccionar(PestanaActividad.PEDIDOS) }, label = { Text("Pedidos") })
            FilterChip(selected = estado.pestana == PestanaActividad.COTIZACIONES, onClick = { viewModel.seleccionar(PestanaActividad.COTIZACIONES) }, label = { Text("Cotizaciones") })
            FilterChip(selected = estado.pestana == PestanaActividad.FACTURAS, onClick = { viewModel.seleccionar(PestanaActividad.FACTURAS) }, label = { Text("Facturas") })
        }
        when (estado.pestana) {
            PestanaActividad.PEDIDOS -> ListaPaginada(
                lista = estado.pedidos,
                vacioTitulo = "Aún no tienes pedidos",
                vacioMensaje = "Cuando compres o aceptes una cotización, tu pedido aparecerá aquí.",
                icono = Icons.Rounded.Inventory2,
                onReintentar = { viewModel.refrescar() },
                onMas = { viewModel.cargarMas() },
                clave = { it.id }
            ) { encargo -> EncargoCard(encargo, onClick = { onPedido(encargo.id) }) }

            PestanaActividad.COTIZACIONES -> Column(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = onCotizacionAutomatica, modifier = Modifier.fillMaxWidth()) { Text("Cotización automática (impresión 3D)") }
                    OutlinedButton(onClick = onNuevaCotizacion, modifier = Modifier.fillMaxWidth()) { Text("Solicitar otro servicio") }
                }
                ListaPaginada(
                    lista = estado.cotizaciones,
                    vacioTitulo = "Aún no tienes cotizaciones",
                    vacioMensaje = "Solicita una cotización y la verás aquí.",
                    icono = Icons.Rounded.RequestQuote,
                    onReintentar = { viewModel.refrescar() },
                    onMas = { viewModel.cargarMas() },
                    clave = { it.id }
                ) { cotizacion -> CotizacionCard(cotizacion, onClick = { onCotizacion(cotizacion.id) }) }
            }

            PestanaActividad.FACTURAS -> ListaPaginada(
                lista = estado.facturas,
                vacioTitulo = "Aún no tienes facturas",
                vacioMensaje = "Las facturas de tus pedidos aparecerán aquí.",
                icono = Icons.Rounded.ReceiptLong,
                onReintentar = { viewModel.refrescar() },
                onMas = { viewModel.cargarMas() },
                clave = { it.id }
            ) { factura -> FacturaCard(factura, onClick = { onFactura(factura.id) }) }
        }
    }
}

@Composable
fun <T> ListaPaginada(
    lista: ListaEstado<T>,
    vacioTitulo: String,
    vacioMensaje: String,
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    onReintentar: () -> Unit,
    onMas: () -> Unit,
    clave: (T) -> Any,
    tarjeta: @Composable (T) -> Unit
) {
    when {
        lista.cargando && lista.items.isEmpty() -> LoadingState()
        lista.error != null && lista.items.isEmpty() -> ErrorState(lista.error, onReintentar)
        lista.cargada && lista.items.isEmpty() -> MessageState(icono, vacioTitulo, vacioMensaje)
        else -> LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(lista.items, key = { clave(it) }) { elemento -> tarjeta(elemento) }
            item { BotonCargarMas(lista.hayMas, lista.cargandoMas, onMas) }
        }
    }
}

@Composable
private fun LineasDocumento(lineas: List<DetalleDocumento>) {
    lineas.forEach { linea ->
        Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
            Text(linea.descripcion, style = MaterialTheme.typography.titleSmall)
            Text(
                "${linea.cantidad} × ${linea.precioUnitario.comoDinero()} = ${linea.subtotal.comoDinero()}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (linea.especificaciones.isNotEmpty()) {
                Text(
                    linea.especificaciones.entries.joinToString(" · ") { "${it.key}: ${it.value}" },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun AbrirDescarga(descarga: DescargaLista?, alConsumir: () -> Unit) {
    val contexto = LocalContext.current
    LaunchedEffect(descarga) {
        if (descarga != null) {
            val abierto = ArchivosExternos.abrir(contexto, descarga.archivo, descarga.mime)
            if (!abierto) Toast.makeText(contexto, "No hay una aplicación para abrir este archivo.", Toast.LENGTH_LONG).show()
            alConsumir()
        }
    }
}

@Composable
private fun AvisarMensaje(mensaje: String?, alConsumir: () -> Unit) {
    val contexto = LocalContext.current
    LaunchedEffect(mensaje) {
        if (mensaje != null) {
            Toast.makeText(contexto, mensaje, Toast.LENGTH_SHORT).show()
            alConsumir()
        }
    }
}

@Composable
fun PedidoDetalleScreen(
    id: Long,
    viewModel: PedidoDetalleViewModel,
    onBack: () -> Unit,
    onCotizacion: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var confirmarCancelar by remember { mutableStateOf(false) }
    LaunchedEffect(id) { viewModel.cargar(id) }
    AbrirDescarga(estado.descarga) { viewModel.consumirDescarga() }
    AvisarMensaje(estado.mensaje) { viewModel.consumirMensaje() }

    if (confirmarCancelar) {
        ConfirmarDialog(
            titulo = "Cancelar pedido",
            mensaje = "Si es una compra de la tienda, las existencias se reponen. ¿Deseas cancelarlo?",
            textoConfirmar = "Cancelar pedido",
            onConfirmar = { confirmarCancelar = false; viewModel.cancelar() },
            onCerrar = { confirmarCancelar = false }
        )
    }

    Scaffold(topBar = { BackTopBar("Pedido", onBack) }) { padding ->
        val pedido = estado.pedido
        when {
            estado.cargando -> LoadingState(Modifier.padding(padding))
            estado.error != null -> ErrorState(estado.error.orEmpty(), { viewModel.cargar(id) }, Modifier.padding(padding))
            pedido != null -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ForgeCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(pedido.codigo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            StatusChip(pedido.estadoTexto, pedido.estado.color())
                        }
                        val progreso = pedido.progreso
                        if (progreso != null) {
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(progress = { progreso / 100f }, modifier = Modifier.fillMaxWidth())
                            Text("Avance $progreso %", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Spacer(Modifier.height(8.dp))
                        FilaDato("Solicitado", pedido.fechaSolicitud)
                        pedido.fechaEntregaEstimada?.let { FilaDato("Entrega estimada", it) }
                        pedido.fechaEntregaReal?.let { FilaDato("Entregado", it) }
                        pedido.responsable?.let { FilaDato("Responsable", it) }
                    }
                }
                if (pedido.lineas.isNotEmpty()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Detalle", style = MaterialTheme.typography.titleMedium)
                            LineasDocumento(pedido.lineas)
                            Spacer(Modifier.height(8.dp))
                            BloqueTotales(pedido.subtotal, pedido.impuesto, pedido.total, descuento = pedido.descuento)
                        }
                    }
                }
                val archivos = pedido.lineas.flatMap { it.archivos }
                if (archivos.isNotEmpty()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Archivos", style = MaterialTheme.typography.titleMedium)
                            archivos.forEach { archivo -> ArchivoFila(archivo, estado.descargando, { viewModel.descargar(archivo) }) }
                        }
                    }
                }
                if (pedido.historial.isNotEmpty()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Historial", style = MaterialTheme.typography.titleMedium)
                            pedido.historial.forEach { h ->
                                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                                    Text("${h.estadoNuevoTexto} · ${h.fecha}", style = MaterialTheme.typography.titleSmall)
                                    if (h.comentario.isNotBlank()) Text(h.comentario, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
                pedido.cotizacionId?.let { cotizacionId ->
                    item { OutlinedButton(onClick = { onCotizacion(cotizacionId) }, modifier = Modifier.fillMaxWidth()) { Text("Ver la cotización de origen") } }
                }
                if (pedido.puedeCancelar) {
                    item {
                        OutlinedButton(onClick = { confirmarCancelar = true }, enabled = !estado.accionando, modifier = Modifier.fillMaxWidth()) { Text("Cancelar pedido") }
                    }
                }
            }
        }
    }
}

@Composable
fun CotizacionDetalleScreen(
    id: Long,
    viewModel: CotizacionDetalleViewModel,
    onBack: () -> Unit,
    onPedido: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var accionPendiente by remember { mutableStateOf<String?>(null) }
    LaunchedEffect(id) { viewModel.cargar(id) }
    AbrirDescarga(estado.descarga) { viewModel.consumirDescarga() }
    AvisarMensaje(estado.mensaje) { viewModel.consumirMensaje() }

    accionPendiente?.let { accion ->
        val texto = when (accion) {
            "aceptar" -> "Al aceptar, nuestro equipo creará tu pedido con estos importes."
            "rechazar" -> "La cotización quedará rechazada."
            else -> "Se cancelará tu solicitud."
        }
        ConfirmarDialog(
            titulo = when (accion) { "aceptar" -> "Aceptar cotización"; "rechazar" -> "Rechazar cotización"; else -> "Cancelar solicitud" },
            mensaje = texto,
            textoConfirmar = "Confirmar",
            onConfirmar = { accionPendiente = null; viewModel.responder(accion) },
            onCerrar = { accionPendiente = null }
        )
    }

    Scaffold(topBar = { BackTopBar("Cotización", onBack) }) { padding ->
        val cotizacion = estado.cotizacion
        when {
            estado.cargando -> LoadingState(Modifier.padding(padding))
            estado.error != null -> ErrorState(estado.error.orEmpty(), { viewModel.cargar(id) }, Modifier.padding(padding))
            cotizacion != null -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ForgeCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(cotizacion.codigo, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            StatusChip(if (cotizacion.vencida) "Vencida" else cotizacion.estadoTexto, cotizacion.estado.color())
                        }
                        Spacer(Modifier.height(8.dp))
                        FilaDato("Emitida", cotizacion.fechaEmision)
                        cotizacion.fechaVencimiento?.let { FilaDato("Válida hasta", it) }
                        cotizacion.plazoEntregaDias?.let { FilaDato("Plazo de entrega", "$it días") }
                    }
                }
                item {
                    val mensaje = when {
                        cotizacion.vencida -> "Esta cotización venció. Solicita una nueva si aún la necesitas."
                        cotizacion.puedeResponder -> "Revisa la propuesta y acéptala o recházala antes de que venza."
                        cotizacion.puedeCancelar -> "Tu solicitud está en espera de revisión. Los importes son preliminares."
                        cotizacion.estado.name == "BORRADOR" -> "Nuestro equipo está revisando tu solicitud."
                        cotizacion.estado.name == "ACEPTADA" -> "Aceptaste la cotización. Nuestro equipo creará tu pedido."
                        else -> ""
                    }
                    if (mensaje.isNotBlank()) {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text(mensaje, style = MaterialTheme.typography.bodyMedium, color = ForgeWarning)
                        }
                    }
                }
                if (cotizacion.solicitud.isNotBlank()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Tu solicitud", style = MaterialTheme.typography.titleMedium)
                            Text(cotizacion.solicitud, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (cotizacion.observaciones.isNotBlank()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Notas del equipo", style = MaterialTheme.typography.titleMedium)
                            Text(cotizacion.observaciones, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
                if (cotizacion.lineas.isNotEmpty()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Detalle", style = MaterialTheme.typography.titleMedium)
                            LineasDocumento(cotizacion.lineas)
                            Spacer(Modifier.height(8.dp))
                            BloqueTotales(cotizacion.subtotal, cotizacion.impuesto, cotizacion.total, descuento = cotizacion.descuento)
                        }
                    }
                }
                if (cotizacion.archivos.isNotEmpty()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Archivos", style = MaterialTheme.typography.titleMedium)
                            cotizacion.archivos.forEach { archivo -> ArchivoFila(archivo, estado.descargando, { viewModel.descargar(archivo) }) }
                        }
                    }
                }
                cotizacion.pedidoId?.let { pedidoId ->
                    item {
                        Button(onClick = { onPedido(pedidoId) }, modifier = Modifier.fillMaxWidth()) { Text("Ver pedido ${cotizacion.pedidoCodigo.orEmpty()}") }
                    }
                }
                if (cotizacion.puedeResponder) {
                    item {
                        Button(onClick = { accionPendiente = "aceptar" }, enabled = !estado.accionando, modifier = Modifier.fillMaxWidth()) { Text("Aceptar cotización") }
                    }
                    item {
                        OutlinedButton(onClick = { accionPendiente = "rechazar" }, enabled = !estado.accionando, modifier = Modifier.fillMaxWidth()) { Text("Rechazar") }
                    }
                }
                if (cotizacion.puedeCancelar) {
                    item {
                        OutlinedButton(onClick = { accionPendiente = "cancelar" }, enabled = !estado.accionando, modifier = Modifier.fillMaxWidth()) { Text("Cancelar solicitud") }
                    }
                }
            }
        }
    }
}

@Composable
fun FacturaDetalleScreen(
    id: Long,
    viewModel: FacturaDetalleViewModel,
    onBack: () -> Unit,
    onPedido: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(id) { viewModel.cargar(id) }

    Scaffold(topBar = { BackTopBar("Factura", onBack) }) { padding ->
        val factura = estado.factura
        when {
            estado.cargando -> LoadingState(Modifier.padding(padding))
            estado.error != null -> ErrorState(estado.error.orEmpty(), { viewModel.cargar(id) }, Modifier.padding(padding))
            factura != null -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    ForgeCard(modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                            Text(factura.numero, style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
                            StatusChip(factura.estadoTexto, factura.estado.color())
                        }
                        Spacer(Modifier.height(8.dp))
                        FilaDato("Emitida", factura.fechaEmision)
                        factura.fechaVencimiento?.let { FilaDato("Vence", it) }
                        if (factura.nombreFacturacion.isNotBlank()) FilaDato("Facturado a", factura.nombreFacturacion)
                        if (factura.identificacionFiscal.isNotBlank()) FilaDato("Identificación", factura.identificacionFiscal)
                    }
                }
                if (factura.lineas.isNotEmpty()) {
                    item {
                        ForgeCard(modifier = Modifier.fillMaxWidth()) {
                            Text("Detalle", style = MaterialTheme.typography.titleMedium)
                            LineasDocumento(factura.lineas)
                            Spacer(Modifier.height(8.dp))
                            BloqueTotales(factura.subtotal, factura.impuesto, factura.total)
                            FilaDato("Saldo pendiente", factura.saldoPendiente.comoDinero(), destacado = true)
                        }
                    }
                }
                item {
                    ForgeCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Pagos registrados", style = MaterialTheme.typography.titleMedium)
                        if (factura.pagos.isEmpty()) {
                            Text("Aún no hay pagos confirmados.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            factura.pagos.forEach { pago ->
                                FilaDato("${pago.fecha} · ${pago.metodo}", pago.monto.comoDinero())
                            }
                        }
                        if (factura.aviso.isNotBlank()) {
                            Spacer(Modifier.height(8.dp))
                            Text(factura.aviso, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                factura.pedidoId?.let { pedidoId ->
                    item { OutlinedButton(onClick = { onPedido(pedidoId) }, modifier = Modifier.fillMaxWidth()) { Text("Ver el pedido") } }
                }
            }
        }
    }
}
