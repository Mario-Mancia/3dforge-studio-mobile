package com.example.a3df_studio_app.ui.screens.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.domain.models.SolicitudRegistro
import com.example.a3df_studio_app.ui.classes.primerError
import com.example.a3df_studio_app.ui.components.common.BannerMensaje
import com.example.a3df_studio_app.ui.components.common.BotonPrimario
import com.example.a3df_studio_app.ui.components.common.BotonSecundario
import com.example.a3df_studio_app.ui.components.common.ForgePasswordField
import com.example.a3df_studio_app.ui.components.common.ForgeTextField
import com.example.a3df_studio_app.ui.components.topbar.BackTopBar

@Composable
private fun EncabezadoAuth(titulo: String, subtitulo: String) {
    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.ViewInAr, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(56.dp))
        Spacer(Modifier.height(8.dp))
        Text(titulo, style = MaterialTheme.typography.headlineSmall)
        Text(subtitulo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun BotonGoogle(cargando: Boolean, onClick: () -> Unit) {
    if (!AppConfig.isGoogleConfigured) return
    OutlinedButton(onClick = onClick, enabled = !cargando, modifier = Modifier.fillMaxWidth().height(50.dp)) {
        if (cargando) CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp) else Text("Continuar con Google")
    }
}

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onRegistro: () -> Unit,
    onRecuperar: () -> Unit,
    onExito: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var correo by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }

    LaunchedEffect(Unit) { viewModel.limpiar() }
    LaunchedEffect(estado.ultimoCorreo) { if (correo.isBlank()) correo = estado.ultimoCorreo }
    LaunchedEffect(estado.exito) {
        if (estado.exito) {
            viewModel.consumirExito()
            onExito()
        }
    }

    Scaffold(topBar = { BackTopBar("Iniciar sesión", onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EncabezadoAuth("Bienvenido de nuevo", "Ingresa para comprar, cotizar y seguir tus pedidos.")
            ForgeTextField(correo, { correo = it }, "Correo electrónico", error = estado.campos.primerError("correo"), teclado = KeyboardType.Email)
            ForgePasswordField(clave, { clave = it }, "Contraseña", error = estado.campos.primerError("password"), accion = ImeAction.Done, onAccion = { viewModel.iniciarSesion(correo, clave) })
            BannerMensaje(estado.error)
            BannerMensaje(estado.mensaje, esError = false)
            if (estado.correoPendiente != null) {
                BotonSecundario("Reenviar correo de confirmación", { viewModel.reenviarConfirmacion(estado.correoPendiente ?: correo) })
            }
            BotonPrimario("Ingresar", { viewModel.iniciarSesion(correo, clave) }, cargando = estado.cargando)
            BotonGoogle(estado.cargandoGoogle) { viewModel.continuarConGoogle(contexto) }
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                TextButton(onClick = onRecuperar) { Text("¿Olvidaste tu contraseña?") }
                TextButton(onClick = onRegistro) { Text("Crear cuenta") }
            }
        }
    }
}

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit,
    onLogin: () -> Unit,
    onExito: () -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var nombres by remember { mutableStateOf("") }
    var apellidos by remember { mutableStateOf("") }
    var correo by remember { mutableStateOf("") }
    var telefono by remember { mutableStateOf("") }
    var clave by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    var acepta by remember { mutableStateOf(false) }
    var verTerminos by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { viewModel.limpiar() }
    LaunchedEffect(estado.exito) {
        if (estado.exito) {
            viewModel.consumirExito()
            onExito()
        }
    }

    if (verTerminos) {
        AlertDialog(
            onDismissRequest = { verTerminos = false },
            title = { Text("Términos y condiciones") },
            text = {
                Text(
                    "Al crear tu cuenta aceptas que usemos tus datos para gestionar cotizaciones, pedidos, facturas y soporte. " +
                        "Las cotizaciones automáticas o preliminares son aproximadas y un profesional confirma el precio final. " +
                        "Los pagos son en efectivo y se coordinan con el equipo. El texto completo está en el sitio web."
                )
            },
            confirmButton = { TextButton(onClick = { verTerminos = false }) { Text("Entendido") } }
        )
    }

    Scaffold(topBar = { BackTopBar("Crear cuenta", onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (estado.correoPendiente != null && estado.mensaje != null) {
                EncabezadoAuth("Revisa tu correo", "Un paso más para activar tu cuenta")
                Text(estado.mensaje.orEmpty(), style = MaterialTheme.typography.bodyMedium)
                Text(
                    "Abre el enlace del correo que enviamos a ${estado.correoPendiente} (con Tailscale activo) y luego inicia sesión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BannerMensaje(estado.error)
                BotonSecundario("Reenviar correo de confirmación", { viewModel.reenviarConfirmacion(estado.correoPendiente.orEmpty()) })
                BotonPrimario("Ya confirmé: iniciar sesión", onLogin)
            } else {
                EncabezadoAuth("Crea tu cuenta", "Cotiza, compra y sigue tus pedidos desde el celular.")
                ForgeTextField(nombres, { nombres = it }, "Nombres", error = estado.campos.primerError("nombres"))
                ForgeTextField(apellidos, { apellidos = it }, "Apellidos", error = estado.campos.primerError("apellidos"))
                ForgeTextField(correo, { correo = it }, "Correo electrónico", error = estado.campos.primerError("correo"), teclado = KeyboardType.Email)
                ForgeTextField(telefono, { telefono = it }, "Teléfono (opcional)", error = estado.campos.primerError("telefono"), teclado = KeyboardType.Phone)
                ForgePasswordField(clave, { clave = it }, "Contraseña", error = estado.campos.primerError("password"), ayuda = "Mínimo 10 caracteres.")
                ForgePasswordField(confirmacion, { confirmacion = it }, "Confirmar contraseña", error = estado.campos.primerError("password2"), accion = ImeAction.Done)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = acepta, onCheckedChange = { acepta = it })
                    Text("Acepto los ", style = MaterialTheme.typography.bodyMedium)
                    TextButton(onClick = { verTerminos = true }) { Text("términos y condiciones") }
                }
                BannerMensaje(estado.campos.primerError("terminos"))
                BannerMensaje(estado.error)
                BotonPrimario(
                    "Crear cuenta",
                    { viewModel.registrarse(SolicitudRegistro(nombres, apellidos, correo, telefono, clave), confirmacion, acepta) },
                    cargando = estado.cargando
                )
                BotonGoogle(estado.cargandoGoogle) { viewModel.continuarConGoogle(contexto) }
                TextButton(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("¿Ya tienes cuenta? Inicia sesión") }
            }
        }
    }
}

@Composable
fun RecuperarScreen(viewModel: AuthViewModel, onBack: () -> Unit) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    var correo by remember { mutableStateOf("") }
    LaunchedEffect(Unit) { viewModel.limpiar() }

    Scaffold(topBar = { BackTopBar("Recuperar contraseña", onBack) }) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EncabezadoAuth("¿Olvidaste tu contraseña?", "Te enviaremos un enlace para elegir una nueva.")
            ForgeTextField(correo, { correo = it }, "Correo electrónico", teclado = KeyboardType.Email, accion = ImeAction.Done, onAccion = { viewModel.recuperarContrasena(correo) })
            BannerMensaje(estado.error)
            BannerMensaje(estado.mensaje, esError = false)
            BotonPrimario("Enviar enlace", { viewModel.recuperarContrasena(correo) }, cargando = estado.cargando)
            Text(
                "El enlace abre una página web del sitio: ábrelo con Tailscale activo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
