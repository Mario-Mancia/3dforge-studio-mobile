package com.example.a3df_studio_app.ui.components.dialog

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Se muestra cuando una acción (comprar, cotizar, ver pedidos…) necesita sesión. */
@Composable
fun AuthGateDialog(
    motivo: String,
    googleDisponible: Boolean,
    cargandoGoogle: Boolean,
    error: String?,
    onLogin: () -> Unit,
    onRegistro: () -> Unit,
    onGoogle: () -> Unit,
    onCerrar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        icon = { Icon(Icons.Outlined.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
        title = { Text("Necesitas iniciar sesión") },
        text = {
            Column {
                Text(motivo, style = MaterialTheme.typography.bodyMedium)
                if (!error.isNullOrBlank()) {
                    Spacer(Modifier.height(8.dp))
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Spacer(Modifier.height(16.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRegistro, modifier = Modifier.fillMaxWidth()) { Text("Crear una cuenta") }
                if (googleDisponible) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onGoogle, enabled = !cargandoGoogle, modifier = Modifier.fillMaxWidth()) {
                        if (cargandoGoogle) CircularProgressIndicator(modifier = Modifier.height(18.dp), strokeWidth = 2.dp) else Text("Continuar con Google")
                    }
                }
            }
        },
        confirmButton = {},
        dismissButton = { TextButton(onClick = onCerrar) { Text("Ahora no") } }
    )
}

@Composable
fun ConfirmarDialog(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCerrar: () -> Unit,
    cargando: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = { Button(onClick = onConfirmar, enabled = !cargando) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Volver") } }
    )
}
