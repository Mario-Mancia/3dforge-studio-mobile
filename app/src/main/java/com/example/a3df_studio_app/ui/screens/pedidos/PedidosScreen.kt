package com.example.a3df_studio_app.ui.screens.pedidos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingBag
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.utils.comoFechaHora
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.card.EncargoCard
import com.example.a3df_studio_app.ui.components.card.FacturaCard
import com.example.a3df_studio_app.ui.components.common.AuthRequiredState
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.helpers.color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PedidosScreen(
    pedidosViewModel: PedidosViewModel,
    isLoggedIn: Boolean,
    onRequireLogin: () -> Unit,
    onEncargo: (Long) -> Unit,
    onPagar: (Long) -> Unit,
    onIrProductos: () -> Unit
) {
    if (!isLoggedIn) {
        AuthRequiredState(
            titulo = "Tus pedidos y pagos",
            mensaje = "Inicia sesión para ver el estado de tus encargos, tus facturas y realizar pagos en línea.",
            onLogin = onRequireLogin
        )
        return
    }

    val uiState by pedidosViewModel.uiState.collectAsState()
    var pestana by rememberSaveable { mutableIntStateOf(0) }

    LaunchedEffect(Unit) { pedidosViewModel.cargar() }

    Column(Modifier.fillMaxSize()) {
        PrimaryTabRow(selectedTabIndex = pestana) {
            listOf("Encargos", "Facturas", "Pagos").forEachIndexed { indice, titulo ->
                Tab(selected = pestana == indice, onClick = { pestana = indice }, text = { Text(titulo) })
            }
        }

        val error = uiState.error
        when {
            uiState.isLoading && !uiState.cargado -> LoadingState()
            error != null && uiState.encargos.isEmpty() -> ErrorState(error, pedidosViewModel::cargar)
            else -> PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = pedidosViewModel::cargar,
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    when (pestana) {
                        0 -> {
                            if (uiState.encargos.isEmpty()) item {
                                MessageState(Icons.Outlined.ShoppingBag, "Aún no tienes encargos", "Explora el catálogo y realiza tu primer pedido.", accion = "Ver productos", onAccion = onIrProductos)
                            }
                            items(uiState.encargos, key = { it.id }) { EncargoCard(it, onClick = { onEncargo(it.id) }) }
                        }
                        1 -> {
                            if (uiState.facturas.isEmpty()) item {
                                MessageState(Icons.Outlined.ReceiptLong, "Sin facturas", "Aquí aparecerán las facturas de tus pedidos.")
                            }
                            items(uiState.facturas, key = { it.id }) { FacturaCard(it, onPagar = { onPagar(it.id) }) }
                        }
                        else -> {
                            if (uiState.pagos.isEmpty()) item {
                                MessageState(Icons.Outlined.Payments, "Sin pagos registrados", null)
                            }
                            items(uiState.pagos, key = { it.id }) { pago ->
                                ForgeCard(Modifier.fillMaxWidth()) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(pago.monto.comoMoneda(), style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                                        StatusChip(pago.estado.etiqueta, pago.estado.color())
                                    }
                                    Text(pago.fecha.comoFechaHora(), style = MaterialTheme.typography.bodySmall)
                                    pago.referencia?.let { Text("Ref. $it", style = MaterialTheme.typography.bodySmall) }
                                    pago.observaciones?.let {
                                        Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
