package com.example.a3df_studio_app.ui.screens.feedback

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarBorder
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.utils.comoFecha
import com.example.a3df_studio_app.ui.components.common.AuthRequiredState
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.StatusChip

@Composable
fun FeedbackScreen(
    feedbackViewModel: FeedbackViewModel,
    isLoggedIn: Boolean,
    snackbarHostState: SnackbarHostState,
    onRequireLogin: () -> Unit
) {
    val uiState by feedbackViewModel.uiState.collectAsState()

    LaunchedEffect(isLoggedIn) { feedbackViewModel.cargar(isLoggedIn) }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            feedbackViewModel.consumirMensaje()
        }
    }

    if (!isLoggedIn) {
        AuthRequiredState(
            titulo = "Tu opinión nos ayuda a mejorar",
            mensaje = "Inicia sesión para calificar nuestros servicios, tus encargos o enviarnos una solicitud de soporte.",
            onLogin = onRequireLogin
        )
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item { Text("Califica un servicio o encargo", style = MaterialTheme.typography.titleMedium) }
        item {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(uiState.encargos, key = { "e${it.id}" }) { encargo ->
                    FilterChip(
                        selected = uiState.encargoSeleccionado == encargo.id,
                        onClick = { feedbackViewModel.seleccionarEncargo(encargo.id) },
                        label = { Text(encargo.codigo) }
                    )
                }
                items(uiState.servicios, key = { "s${it.id}" }) { servicio ->
                    FilterChip(
                        selected = uiState.servicioSeleccionado == servicio.id,
                        onClick = { feedbackViewModel.seleccionarServicio(servicio.id) },
                        label = { Text(servicio.nombre) }
                    )
                }
            }
        }
        item {
            Row {
                (1..5).forEach { valor ->
                    IconButton(onClick = { feedbackViewModel.onPuntuacion(valor) }) {
                        Icon(
                            if (valor <= uiState.puntuacion) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "$valor estrellas",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
        item {
            OutlinedTextField(
                value = uiState.comentario,
                onValueChange = feedbackViewModel::onComentario,
                label = { Text("Comentario (opcional)") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Button(onClick = feedbackViewModel::enviarResena, enabled = !uiState.isSending, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar calificación")
            }
        }

        item { Text("¿Necesitas ayuda?", style = MaterialTheme.typography.titleMedium) }
        item {
            OutlinedTextField(
                value = uiState.asunto,
                onValueChange = feedbackViewModel::onAsunto,
                label = { Text("Asunto") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            OutlinedTextField(
                value = uiState.descripcion,
                onValueChange = feedbackViewModel::onDescripcion,
                label = { Text("Describe tu solicitud, sugerencia o reclamo") },
                minLines = 3,
                modifier = Modifier.fillMaxWidth()
            )
        }
        item {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                PrioridadSoporte.entries.forEach { prioridad ->
                    FilterChip(
                        selected = uiState.prioridad == prioridad,
                        onClick = { feedbackViewModel.onPrioridad(prioridad) },
                        label = { Text(prioridad.etiqueta) }
                    )
                }
            }
        }
        item {
            Button(onClick = feedbackViewModel::enviarSoporte, enabled = !uiState.isSending, modifier = Modifier.fillMaxWidth()) {
                Text("Enviar solicitud")
            }
        }

        if (uiState.misSolicitudes.isNotEmpty()) {
            item { Text("Mis solicitudes", style = MaterialTheme.typography.titleMedium) }
            items(uiState.misSolicitudes, key = { "sol${it.id}" }) { solicitud ->
                ForgeCard(Modifier.fillMaxWidth()) {
                    Row {
                        Text(solicitud.asunto, style = MaterialTheme.typography.titleSmall, modifier = Modifier.weight(1f))
                        StatusChip(solicitud.estado.etiqueta)
                    }
                    Text(solicitud.fecha.comoFecha(), style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
