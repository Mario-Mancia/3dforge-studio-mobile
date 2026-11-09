package com.example.a3df_studio_app.ui.screens.productos

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.card.ProductoCard
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.MessageState

@Composable
fun ProductosScreen(
    productosViewModel: ProductosViewModel,
    isLoggedIn: Boolean,
    snackbarHostState: SnackbarHostState,
    onRequireLogin: () -> Unit,
    onPagar: (Long) -> Unit,
    onVerPedidos: () -> Unit
) {
    val uiState by productosViewModel.uiState.collectAsState()
    var confirmar by remember { mutableStateOf(false) }

    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            productosViewModel.consumirMensaje()
        }
    }

    Column(Modifier.fillMaxSize()) {
        OutlinedTextField(
            value = uiState.busqueda,
            onValueChange = productosViewModel::onBusqueda,
            leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) },
            placeholder = { Text("Buscar productos") },
            singleLine = true,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        )

        val error = uiState.error
        when {
            uiState.isLoading && uiState.productos.isEmpty() -> LoadingState(Modifier.weight(1f))
            error != null && uiState.productos.isEmpty() ->
                ErrorState(error, productosViewModel::cargar, Modifier.weight(1f))
            uiState.productos.isEmpty() -> MessageState(
                icono = Icons.Outlined.Inventory2,
                titulo = "Sin resultados",
                mensaje = "No encontramos productos con ese nombre.",
                modifier = Modifier.weight(1f)
            )
            else -> LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(uiState.productos, key = { it.id }) { producto ->
                    ProductoCard(
                        producto = producto,
                        cantidad = uiState.carrito[producto.id] ?: 0,
                        onAgregar = { productosViewModel.agregar(producto.id) },
                        onQuitar = { productosViewModel.quitar(producto.id) }
                    )
                }
            }
        }

        if (uiState.unidadesCarrito > 0) {
            Surface(tonalElevation = 3.dp, shadowElevation = 6.dp) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(Modifier.weight(1f)) {
                        Text("${uiState.unidadesCarrito} artículo(s)", style = MaterialTheme.typography.bodySmall)
                        Text(
                            "${uiState.totalCarrito.comoMoneda()} + IVA",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    Button(
                        onClick = { if (isLoggedIn) confirmar = true else onRequireLogin() },
                        enabled = !uiState.isOrdering
                    ) {
                        if (uiState.isOrdering) {
                            CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
                        } else {
                            Text("Realizar pedido")
                        }
                    }
                }
            }
        }
    }

    if (confirmar) {
        AlertDialog(
            onDismissRequest = { confirmar = false },
            title = { Text("Confirmar pedido") },
            text = {
                Column {
                    uiState.itemsCarrito.forEach {
                        Text("• ${it.producto.nombre} x ${it.cantidad} — ${it.subtotal.comoMoneda()}")
                    }
                    Spacer(Modifier.height(12.dp))
                    OutlinedTextField(
                        value = uiState.observaciones,
                        onValueChange = productosViewModel::onObservaciones,
                        label = { Text("Observaciones (opcional)") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("Se aplicará IVA (13%) al generar la factura.", style = MaterialTheme.typography.bodySmall)
                }
            },
            confirmButton = {
                Button(onClick = {
                    confirmar = false
                    productosViewModel.realizarPedido()
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { confirmar = false }) { Text("Cancelar") } }
        )
    }

    uiState.pedidoCreado?.let { pedido ->
        AlertDialog(
            onDismissRequest = productosViewModel::cerrarPedidoCreado,
            title = { Text("¡Pedido registrado!") },
            text = {
                Text("Tu pedido ${pedido.codigo} fue creado con la factura ${pedido.numeroFactura} por ${pedido.total.comoMoneda()}.")
            },
            confirmButton = {
                Button(onClick = {
                    productosViewModel.cerrarPedidoCreado()
                    onPagar(pedido.idFactura)
                }) { Text("Pagar ahora") }
            },
            dismissButton = {
                TextButton(onClick = {
                    productosViewModel.cerrarPedidoCreado()
                    onVerPedidos()
                }) { Text("Ver mis pedidos") }
            }
        )
    }
}
