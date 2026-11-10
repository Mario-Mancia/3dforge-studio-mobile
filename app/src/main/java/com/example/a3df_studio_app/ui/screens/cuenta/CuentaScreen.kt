package com.example.a3df_studio_app.ui.screens.cuenta

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.material.icons.rounded.Storefront
import androidx.compose.material.icons.rounded.SupportAgent
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.InfoNegocio
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.ui.classes.primerError
import com.example.a3df_studio_app.ui.components.common.BannerMensaje
import com.example.a3df_studio_app.ui.components.common.BotonPrimario
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ForgePasswordField
import com.example.a3df_studio_app.ui.components.common.ForgeTextField
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.UserAvatar
import com.example.a3df_studio_app.ui.components.dialog.ConfirmarDialog

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CuentaScreen(
    viewModel: CuentaViewModel,
    usuario: AuthUser?,
    negocio: InfoNegocio?,
    googleCargando: Boolean,
    onLogin: () -> Unit,
    onRegistro: () -> Unit,
    onGoogle: () -> Unit,
    onNotificaciones: () -> Unit,
    onCotizaciones: () -> Unit,
    onFacturas: () -> Unit,
    onSoporte: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (usuario == null) {
        Column(
            modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ForgeCard(modifier = Modifier.fillMaxWidth()) {
                Text("Crea tu cuenta o inicia sesión", style = MaterialTheme.typography.titleLarge)
                Spacer(Modifier.height(4.dp))
                Text(
                    "Con tu cuenta puedes comprar, solicitar cotizaciones, ver el avance de tus pedidos, consultar tus facturas y contactar a soporte.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) { Text("Iniciar sesión") }
                Spacer(Modifier.height(8.dp))
                OutlinedButton(onClick = onRegistro, modifier = Modifier.fillMaxWidth()) { Text("Crear una cuenta") }
                if (AppConfig.isGoogleConfigured) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedButton(onClick = onGoogle, enabled = !googleCargando, modifier = Modifier.fillMaxWidth()) {
                        if (googleCargando) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Continuar con Google")
                    }
                }
            }
            negocio?.let { InfoNegocioCard(it) }
        }
        return
    }

    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var dialogoClave by remember { mutableStateOf(false) }
    var confirmarSalir by remember { mutableStateOf(false) }

    LaunchedEffect(usuario.id) { viewModel.cargar() }
    LaunchedEffect(estado.mensaje) {
        estado.mensaje?.let {
            Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show()
            viewModel.consumirMensaje()
        }
    }
    LaunchedEffect(estado.claveCambiada) {
        if (estado.claveCambiada) {
            dialogoClave = false
            viewModel.consumirClaveCambiada()
        }
    }

    if (dialogoClave) {
        DialogoCambiarClave(
            cargando = estado.cambiandoClave,
            error = estado.errorClave,
            campos = estado.camposClave,
            onConfirmar = { actual, nueva, confirmacion -> viewModel.cambiarClave(actual, nueva, confirmacion) },
            onCerrar = {
                dialogoClave = false
                viewModel.consumirClaveCambiada()
            }
        )
    }
    if (confirmarSalir) {
        ConfirmarDialog(
            titulo = "Cerrar sesión",
            mensaje = "Tu carrito se conserva en este dispositivo. ¿Deseas cerrar sesión?",
            textoConfirmar = "Cerrar sesión",
            onConfirmar = {
                confirmarSalir = false
                viewModel.cerrarSesion()
            },
            onCerrar = { confirmarSalir = false }
        )
    }

    if (estado.cargando && estado.cliente == null) {
        LoadingState(modifier)
        return
    }
    if (estado.error != null && estado.cliente == null) {
        ErrorState(estado.error.orEmpty(), { viewModel.cargar() }, modifier)
        return
    }

    Column(
        modifier = modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        ForgeCard(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                UserAvatar(avatarUrl = null, iniciales = usuario.iniciales, size = 56.dp)
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(usuario.nombreVisible, style = MaterialTheme.typography.titleLarge)
                    Text(usuario.correo, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

        FilaMenu("Notificaciones", Icons.Rounded.Notifications, onNotificaciones)
        FilaMenu("Mis cotizaciones", Icons.Rounded.RequestQuote, onCotizaciones)
        FilaMenu("Mis facturas", Icons.Rounded.ReceiptLong, onFacturas)
        FilaMenu("Soporte", Icons.Rounded.SupportAgent, onSoporte)
        FilaMenu("Cambiar contraseña", Icons.Rounded.Lock) { dialogoClave = true }

        ForgeCard(modifier = Modifier.fillMaxWidth()) {
            Text("Mis datos", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = estado.tipo == TipoCliente.PERSONA, onClick = { viewModel.cambiarTipo(TipoCliente.PERSONA) }, label = { Text("Persona") })
                FilterChip(selected = estado.tipo == TipoCliente.EMPRESA, onClick = { viewModel.cambiarTipo(TipoCliente.EMPRESA) }, label = { Text("Empresa") })
            }
            Spacer(Modifier.height(8.dp))
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ForgeTextField(estado.nombres, viewModel::cambiarNombres, "Nombres", error = estado.campos.primerError("nombres"))
                ForgeTextField(estado.apellidos, viewModel::cambiarApellidos, "Apellidos", error = estado.campos.primerError("apellidos"))
                if (estado.tipo == TipoCliente.EMPRESA) {
                    ForgeTextField(estado.nombreEmpresa, viewModel::cambiarEmpresa, "Nombre de la empresa", error = estado.campos.primerError("nombre_empresa"))
                }
                ForgeTextField(estado.telefono, viewModel::cambiarTelefono, "Teléfono", error = estado.campos.primerError("telefono"), teclado = KeyboardType.Phone)
                ForgeTextField(estado.identificacionFiscal, viewModel::cambiarFiscal, "Identificación fiscal (opcional)", error = estado.campos.primerError("identificacion_fiscal"))
                ForgeTextField(estado.direccion, viewModel::cambiarDireccion, "Dirección (opcional)", error = estado.campos.primerError("direccion"), lineas = 2)
                BannerMensaje(estado.error)
                BotonPrimario("Guardar cambios", { viewModel.guardar() }, cargando = estado.guardando)
            }
        }

        negocio?.let { InfoNegocioCard(it) }

        OutlinedButton(onClick = { confirmarSalir = true }, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.AutoMirrored.Rounded.Logout, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            Text("Cerrar sesión")
        }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun FilaMenu(texto: String, icono: ImageVector, onClick: () -> Unit) {
    ForgeCard(modifier = Modifier.fillMaxWidth(), onClick = onClick) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(12.dp))
            Text(texto, style = MaterialTheme.typography.titleSmall)
        }
    }
}

@Composable
private fun InfoNegocioCard(negocio: InfoNegocio) {
    ForgeCard(modifier = Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Rounded.Storefront, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column {
                Text(negocio.nombre, style = MaterialTheme.typography.titleMedium)
                negocio.telefono?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                negocio.correo?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
                negocio.direccion?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                negocio.sitioWeb?.let { Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant) }
            }
        }
    }
}

@Composable
private fun DialogoCambiarClave(
    cargando: Boolean,
    error: String?,
    campos: Map<String, List<String>>,
    onConfirmar: (String, String, String) -> Unit,
    onCerrar: () -> Unit
) {
    var actual by remember { mutableStateOf("") }
    var nueva by remember { mutableStateOf("") }
    var confirmacion by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onCerrar,
        title = { Text("Cambiar contraseña") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                ForgePasswordField(actual, { actual = it }, "Contraseña actual", error = campos.primerError("password_actual"))
                ForgePasswordField(nueva, { nueva = it }, "Contraseña nueva", error = campos.primerError("password_nueva"), ayuda = "Mínimo 10 caracteres.")
                ForgePasswordField(confirmacion, { confirmacion = it }, "Confirmar contraseña nueva", error = campos.primerError("confirmacion"))
                BannerMensaje(error)
            }
        },
        confirmButton = {
            Button(onClick = { onConfirmar(actual, nueva, confirmacion) }, enabled = !cargando) {
                if (cargando) CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp) else Text("Cambiar")
            }
        },
        dismissButton = { TextButton(onClick = onCerrar) { Text("Cancelar") } }
    )
}
