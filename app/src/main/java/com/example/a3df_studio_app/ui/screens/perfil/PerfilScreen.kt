package com.example.a3df_studio_app.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.common.UserAvatar

@Composable
fun PerfilScreen(
    perfilViewModel: PerfilViewModel,
    usuario: AuthUser?,
    avatarUrl: String?,
    snackbarHostState: SnackbarHostState,
    onLogout: () -> Unit
) {
    val uiState by perfilViewModel.uiState.collectAsState()

    LaunchedEffect(Unit) { perfilViewModel.cargar() }
    LaunchedEffect(uiState.mensaje) {
        uiState.mensaje?.let {
            snackbarHostState.showSnackbar(it)
            perfilViewModel.consumirMensaje()
        }
    }

    val error = uiState.error
    if (uiState.isLoading) { LoadingState(); return }
    if (error != null && uiState.correo.isBlank()) { ErrorState(error, perfilViewModel::cargar); return }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            UserAvatar(avatarUrl = avatarUrl, iniciales = usuario?.iniciales, size = 72.dp)
            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text(usuario?.nombreVisible.orEmpty(), style = MaterialTheme.typography.titleLarge)
                Text(uiState.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Spacer(Modifier.size(4.dp))
                StatusChip(if (uiState.perfilCompleto) "Perfil completo" else "Completa tus datos para tus pedidos")
            }
        }

        Text("Datos para pedidos y facturación", style = MaterialTheme.typography.titleMedium)
        SingleChoiceSegmentedButtonRow(Modifier.fillMaxWidth()) {
            TipoCliente.entries.forEachIndexed { indice, tipo ->
                SegmentedButton(
                    selected = uiState.tipo == tipo,
                    onClick = { perfilViewModel.onTipo(tipo) },
                    shape = SegmentedButtonDefaults.itemShape(indice, TipoCliente.entries.size)
                ) { Text(tipo.etiqueta) }
            }
        }
        if (uiState.tipo == TipoCliente.EMPRESA) {
            Campo(uiState.nombreEmpresa, perfilViewModel::onEmpresa, "Nombre de la empresa")
        }
        Campo(uiState.nombres, perfilViewModel::onNombres, "Nombres")
        Campo(uiState.apellidos, perfilViewModel::onApellidos, "Apellidos")
        Campo(uiState.telefono, perfilViewModel::onTelefono, "Teléfono", KeyboardType.Phone)
        Campo(uiState.identificacionFiscal, perfilViewModel::onIdentificacion, "DUI / NIT (opcional)")
        OutlinedTextField(
            value = uiState.direccion,
            onValueChange = perfilViewModel::onDireccion,
            label = { Text("Dirección de entrega") },
            minLines = 2,
            modifier = Modifier.fillMaxWidth()
        )
        Button(onClick = perfilViewModel::guardar, enabled = !uiState.isSaving, modifier = Modifier.fillMaxWidth()) {
            if (uiState.isSaving) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Guardar cambios")
        }
        OutlinedButton(onClick = { perfilViewModel.cerrarSesion(onLogout) }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
    }
}

@Composable
private fun Campo(valor: String, onCambio: (String) -> Unit, etiqueta: String, teclado: KeyboardType = KeyboardType.Text) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        modifier = Modifier.fillMaxWidth()
    )
}
