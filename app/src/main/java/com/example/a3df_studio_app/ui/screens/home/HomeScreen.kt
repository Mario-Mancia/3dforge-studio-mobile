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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.ui.components.card.PromocionCard
import com.example.a3df_studio_app.ui.components.card.ServicioCard
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.SectionTitle

@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    usuario: AuthUser?,
    onCotizarServicio: (Servicio) -> Unit,
    onAsesoria: () -> Unit,
    onVerProductos: () -> Unit
) {
    val uiState by homeViewModel.uiState.collectAsState()

    if (uiState.isLoading && uiState.servicios.isEmpty()) {
        LoadingState()
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            ) {
                Column(Modifier.padding(20.dp)) {
                    Text(
                        usuario?.let { "¡Hola, ${it.primerNombre}!" } ?: "¡Bienvenido!",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Convertimos tus ideas en piezas reales: prototipos, repuestos, figuras y más.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(16.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = onAsesoria) {
                            Icon(Icons.Rounded.SupportAgent, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Asesoría")
                        }
                        OutlinedButton(
                            onClick = onVerProductos,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                        ) {
                            Icon(Icons.Rounded.Inventory2, contentDescription = null)
                            Spacer(Modifier.width(6.dp))
                            Text("Productos")
                        }
                    }
                }
            }
        }

        if (uiState.promociones.isNotEmpty()) {
            item { SectionTitle("Promociones") }
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.promociones, key = { it.id }) { PromocionCard(it) }
                }
            }
        }

        item { SectionTitle("Nuestros servicios") }

        if (uiState.categorias.isNotEmpty()) {
            item {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(uiState.categorias, key = { it.id }) { categoria ->
                        FilterChip(
                            selected = uiState.categoriaSeleccionada == categoria.id,
                            onClick = { homeViewModel.seleccionarCategoria(categoria.id) },
                            label = { Text(categoria.nombre) }
                        )
                    }
                }
            }
        }

        val error = uiState.error
        if (error != null && uiState.servicios.isEmpty()) {
            item { ErrorState(mensaje = error, onRetry = homeViewModel::cargar) }
        }

        items(uiState.serviciosFiltrados, key = { it.id }) { servicio ->
            ServicioCard(
                servicio = servicio,
                accion = "Cotizar",
                onAccion = { onCotizarServicio(servicio) },
                modifier = Modifier.padding(horizontal = 16.dp)
            )
        }

        uiState.negocio?.let { negocio ->
            item { SectionTitle("Contacto") }
            item {
                ForgeCard(modifier = Modifier.padding(horizontal = 16.dp).fillMaxWidth()) {
                    Text(negocio.nombre, style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))
                    negocio.telefono?.let { ContactoFila(Icons.Outlined.Phone, it) }
                    negocio.correo?.let { ContactoFila(Icons.Outlined.Email, it) }
                    negocio.direccion?.let { ContactoFila(Icons.Outlined.LocationOn, it) }
                }
            }
        }
    }
}

@Composable
private fun ContactoFila(icono: ImageVector, texto: String) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 4.dp)) {
        Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(12.dp))
        Text(texto, style = MaterialTheme.typography.bodyMedium)
    }
}
