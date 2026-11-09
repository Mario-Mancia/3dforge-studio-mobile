package com.example.a3df_studio_app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    authViewModel: AuthViewModel,
    onAutenticado: () -> Unit,
    onVerificar: (String) -> Unit,
    onRegistro: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val ultimoCorreo by authViewModel.ultimoCorreo.collectAsState(initial = null)
    val context = LocalContext.current
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(ultimoCorreo) { if (correo.isBlank()) ultimoCorreo?.let { correo = it } }
    LaunchedEffect(uiState.autenticado, uiState.correoVerificacion) {
        when {
            uiState.autenticado -> { authViewModel.consumirNavegacion(); onAutenticado() }
            uiState.correoVerificacion != null -> {
                val destino = uiState.correoVerificacion.orEmpty()
                authViewModel.consumirNavegacion()
                onVerificar(destino)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(Icons.Rounded.ViewInAr, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(72.dp))
        Text("Bienvenido de nuevo", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Inicia sesión para cotizar, hacer pedidos y dar seguimiento a tus encargos.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        CampoClave(clave, { clave = it }, "Contraseña")
        TextButton(onClick = { authViewModel.recuperarClave(correo) }, modifier = Modifier.align(Alignment.End)) {
            Text("¿Olvidaste tu contraseña?")
        }
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center) }
        uiState.mensaje?.let { Text(it, color = MaterialTheme.colorScheme.primary, textAlign = TextAlign.Center) }
        Button(
            onClick = { authViewModel.login(correo, clave) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Iniciar sesión")
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        BotonGoogle(uiState.isLoading) { authViewModel.loginGoogle(context) }
        TextButton(onClick = onRegistro) { Text("¿No tienes cuenta? Regístrate") }
    }
}
