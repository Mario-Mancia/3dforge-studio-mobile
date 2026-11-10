package com.example.a3df_studio_app.ui.screens.catalogo

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.utils.comoDinero
import com.example.a3df_studio_app.ui.classes.PestanaCatalogo
import com.example.a3df_studio_app.ui.components.card.ProductoCard
import com.example.a3df_studio_app.ui.components.card.ServicioCard
import com.example.a3df_studio_app.ui.components.card.colorDisponibilidad
import com.example.a3df_studio_app.ui.components.common.BotonCargarMas
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ForgeTextField
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.topbar.BackTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CatalogoScreen(
    viewModel: CatalogoViewModel,
    onProducto: (Long) -> Unit,
    onServicio: (Long) -> Unit,
    onCotizar: (Long?) -> Unit,
    modifier: Modifier = Modifier
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    LaunchedEffect(Unit) { viewModel.iniciar() }

    Column(modifier = modifier.fillMaxSize()) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = estado.pestana == PestanaCatalogo.PRODUCTOS,
                onClick = { viewModel.cambiarPestana(PestanaCatalogo.PRODUCTOS) },
                label = { Text("Productos") }
            )
            FilterChip(
                selected = estado.pestana == PestanaCatalogo.SERVICIOS,
                onClick = { viewModel.cambiarPestana(PestanaCatalogo.SERVICIOS) },
                label = { Text("Servicios") }
            )
        }
        when (estado.pestana) {
            PestanaCatalogo.PRODUCTOS -> {
                Row(modifier = Modifier.padding(horizontal = 16.dp), verticalAlignment = Alignment.CenterVertically) {
                    ForgeTextField(
                        valor = estado.busqueda,
                        onCambio = viewModel::actualizarBusqueda,
                        etiqueta = "Buscar productos",
                        modifier = Modifier.weight(1f),
                        accion = ImeAction.Search,
                        onAccion = viewModel::buscar
                    )
                    IconButton(onClick = viewModel::buscar) { Icon(Icons.Rounded.Search, contentDescription = "Buscar") }
                }
                val lista = estado.productos
                when {
                    lista.cargando && lista.items.isEmpty() -> LoadingState()
                    lista.error != null && lista.items.isEmpty() -> ErrorState(lista.error, { viewModel.cargarProductos(true) })
                    lista.cargada && lista.items.isEmpty() -> MessageState(Icons.Rounded.Inventory2, "No hay productos", "Prueba con otra búsqueda.")
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista.items, key = { it.id }) { producto ->
                            ProductoCard(
                                producto = producto,
                                onClick = { onProducto(producto.id) },
                                onAgregar = {
                                    viewModel.agregarAlCarrito(producto) { texto -> Toast.makeText(contexto, texto, Toast.LENGTH_SHORT).show() }
                                }
                            )
                        }
                        item { BotonCargarMas(lista.hayMas, lista.cargandoMas, viewModel::cargarMasProductos) }
                    }
                }
            }
            PestanaCatalogo.SERVICIOS -> {
                if (estado.categorias.isNotEmpty()) {
                    LazyRow(contentPadding = PaddingValues(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        item {
                            FilterChip(
                                selected = estado.categoriaSeleccionada == null,
                                onClick = { viewModel.seleccionarCategoria(null) },
                                label = { Text("Todas") }
                            )
                        }
                        items(estado.categorias, key = { it.id }) { categoria ->
                            FilterChip(
                                selected = estado.categoriaSeleccionada == categoria.id,
                                onClick = { viewModel.seleccionarCategoria(categoria.id) },
                                label = { Text(categoria.nombre) }
                            )
                        }
                    }
                }
                when {
                    estado.cargandoServicios && estado.servicios.isEmpty() -> LoadingState()
                    estado.errorServicios != null && estado.servicios.isEmpty() -> ErrorState(estado.errorServicios.orEmpty(), { viewModel.cargarServicios() })
                    estado.serviciosCargados && estado.servicios.isEmpty() -> MessageState(Icons.Rounded.Handyman, "No hay servicios", "Vuelve pronto.")
                    else -> LazyColumn(
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(estado.servicios, key = { it.id }) { servicio ->
                            ServicioCard(
                                servicio = servicio,
                                onClick = { onServicio(servicio.id) },
                                onCotizar = { onCotizar(servicio.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ProductoDetalleScreen(
    id: Long,
    viewModel: DetalleCatalogoViewModel,
    onBack: () -> Unit,
    onComprarAhora: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var cantidad by remember { mutableIntStateOf(1) }

    LaunchedEffect(id) { viewModel.cargarProducto(id) }
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show()
            viewModel.consumirMensaje()
        }
    }

    Scaffold(topBar = { BackTopBar("Producto", onBack) }) { padding ->
        val producto = estado.producto
        when {
            estado.cargando -> LoadingState(Modifier.padding(padding))
            estado.error != null -> ErrorState(estado.error.orEmpty(), { viewModel.cargarProducto(id) }, Modifier.padding(padding))
            producto != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (!producto.imagenUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = producto.imagenUrl,
                        contentDescription = producto.nombre,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxWidth().height(240.dp).clip(RoundedCornerShape(16.dp))
                    )
                }
                Text(producto.nombre, style = MaterialTheme.typography.headlineSmall)
                Text(producto.precio.comoDinero(), style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                StatusChip(producto.disponibilidad.etiqueta, colorDisponibilidad(producto.disponibilidad))
                if (producto.descripcion.isNotBlank()) Text(producto.descripcion, style = MaterialTheme.typography.bodyLarge)
                Text("Código: ${producto.sku} · Existencias: ${producto.stock}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (!producto.agotado) {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        IconButton(onClick = { if (cantidad > 1) cantidad-- }) { Icon(Icons.Rounded.Remove, contentDescription = "Menos") }
                        Text("$cantidad", style = MaterialTheme.typography.titleLarge)
                        IconButton(onClick = { if (cantidad < producto.stock) cantidad++ }) { Icon(Icons.Rounded.Add, contentDescription = "Más") }
                    }
                }
                OutlinedButton(onClick = { viewModel.agregar(cantidad) }, enabled = !producto.agotado, modifier = Modifier.fillMaxWidth()) {
                    Text("Agregar al carrito")
                }
                Button(onClick = onComprarAhora, enabled = !producto.agotado, modifier = Modifier.fillMaxWidth()) {
                    Text("Comprar ahora")
                }
            }
        }
    }
}

@Composable
fun ServicioDetalleScreen(
    id: Long,
    viewModel: DetalleCatalogoViewModel,
    onBack: () -> Unit,
    onCotizar: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    LaunchedEffect(id) { viewModel.cargarServicio(id) }

    Scaffold(topBar = { BackTopBar("Servicio", onBack) }) { padding ->
        val servicio = estado.servicio
        when {
            estado.cargando -> LoadingState(Modifier.padding(padding))
            estado.error != null -> ErrorState(estado.error.orEmpty(), { viewModel.cargarServicio(id) }, Modifier.padding(padding))
            servicio != null -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(servicio.nombre, style = MaterialTheme.typography.headlineSmall)
                StatusChip(servicio.categoria)
                Text("Desde ${servicio.precioBase.comoDinero()}", style = MaterialTheme.typography.headlineMedium, color = MaterialTheme.colorScheme.primary)
                if (servicio.descripcion.isNotBlank()) Text(servicio.descripcion, style = MaterialTheme.typography.bodyLarge)
                if (servicio.requiereArchivo) {
                    ForgeCard(modifier = Modifier.fillMaxWidth()) {
                        Text("Este servicio requiere adjuntar al menos un archivo (modelo 3D, imagen o PDF).", style = MaterialTheme.typography.bodyMedium)
                    }
                }
                ForgeCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        "El precio es una referencia: nuestro equipo revisa tu solicitud y te envía una propuesta con precio y plazo.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Button(onClick = { onCotizar(servicio.id) }, modifier = Modifier.fillMaxWidth()) { Text("Solicitar cotización") }
            }
        }
    }
}
