package com.example.a3df_studio_app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp

@Composable
fun RegisterScreen(
    authViewModel: AuthViewModel,
    onAutenticado: () -> Unit,
    onVerificar: (String) -> Unit,
    onLogin: () -> Unit
) {
    val uiState by authViewModel.uiState.collectAsState()
    val context = LocalContext.current
    var nombres by rememberSaveable { mutableStateOf("") }
    var apellidos by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var clave by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }

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
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Crea tu cuenta", style = MaterialTheme.typography.headlineSmall)
        Text(
            "Te enviaremos un código a tu correo para verificar tu cuenta.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        OutlinedTextField(nombres, { nombres = it }, label = { Text("Nombres") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(apellidos, { apellidos = it }, label = { Text("Apellidos") }, singleLine = true, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(
            value = correo,
            onValueChange = { correo = it },
            label = { Text("Correo electrónico") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        CampoClave(clave, { clave = it }, "Contraseña (mín. 8, letras y números)")
        CampoClave(confirmacion, { confirmacion = it }, "Confirmar contraseña")
        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        Button(
            onClick = { authViewModel.registrar(nombres, apellidos, correo, clave, confirmacion) },
            enabled = !uiState.isLoading,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            else Text("Registrarme")
        }
        HorizontalDivider(Modifier.padding(vertical = 8.dp))
        BotonGoogle(uiState.isLoading) { authViewModel.loginGoogle(context) }
        TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("¿Ya tienes cuenta? Inicia sesión") }
    }
}
