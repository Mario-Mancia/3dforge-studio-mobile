package com.example.a3df_studio_app.ui.components.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import java.math.BigDecimal
import com.example.a3df_studio_app.domain.utils.comoDinero

@Composable
fun ForgeTextField(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    accion: ImeAction = ImeAction.Next,
    onAccion: (() -> Unit)? = null,
    lineas: Int = 1,
    habilitado: Boolean = true
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = {
            val texto = error ?: ayuda
            if (texto != null) Text(texto)
        },
        singleLine = lineas == 1,
        minLines = lineas,
        enabled = habilitado,
        keyboardOptions = KeyboardOptions(keyboardType = teclado, imeAction = accion),
        keyboardActions = KeyboardActions(
            onNext = { onAccion?.invoke() },
            onDone = { onAccion?.invoke() },
            onSearch = { onAccion?.invoke() },
            onSend = { onAccion?.invoke() }
        )
    )
}

@Composable
fun ForgePasswordField(
    valor: String,
    onCambio: (String) -> Unit,
    etiqueta: String,
    modifier: Modifier = Modifier,
    error: String? = null,
    ayuda: String? = null,
    accion: ImeAction = ImeAction.Next,
    onAccion: (() -> Unit)? = null
) {
    var visible by remember { mutableStateOf(false) }
    OutlinedTextField(
        value = valor,
        onValueChange = onCambio,
        label = { Text(etiqueta) },
        modifier = modifier.fillMaxWidth(),
        isError = error != null,
        supportingText = {
            val texto = error ?: ayuda
            if (texto != null) Text(texto)
        },
        singleLine = true,
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = accion),
        keyboardActions = KeyboardActions(onNext = { onAccion?.invoke() }, onDone = { onAccion?.invoke() }),
        trailingIcon = {
            IconButton(onClick = { visible = !visible }) {
                Icon(
                    imageVector = if (visible) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility,
                    contentDescription = if (visible) "Ocultar contraseña" else "Mostrar contraseña"
                )
            }
        }
    )
}

@Composable
fun BotonPrimario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    cargando: Boolean = false,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        if (cargando) {
            CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp, color = MaterialTheme.colorScheme.onPrimary)
        } else {
            Text(texto)
        }
    }
}

@Composable
fun BotonSecundario(texto: String, onClick: () -> Unit, modifier: Modifier = Modifier, habilitado: Boolean = true) {
    OutlinedButton(
        onClick = onClick,
        enabled = habilitado,
        modifier = modifier
            .fillMaxWidth()
            .height(50.dp)
    ) {
        Text(texto)
    }
}

@Composable
fun BannerMensaje(mensaje: String?, modifier: Modifier = Modifier, esError: Boolean = true) {
    if (mensaje.isNullOrBlank()) return
    val color = if (esError) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(Icons.Rounded.ErrorOutline, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
        Text(mensaje, color = color, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String, modifier: Modifier = Modifier, destacado: Boolean = false) {
    Row(modifier = modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            valor,
            style = if (destacado) MaterialTheme.typography.titleMedium else MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun BloqueTotales(
    subtotal: BigDecimal,
    impuesto: BigDecimal,
    total: BigDecimal,
    modifier: Modifier = Modifier,
    descuento: BigDecimal? = null,
    estimado: Boolean = false
) {
    Column(modifier = modifier.fillMaxWidth()) {
        FilaDato("Subtotal", subtotal.comoDinero())
        if (descuento != null && descuento.signum() > 0) FilaDato("Descuento", "-" + descuento.comoDinero())
        FilaDato("Impuesto", impuesto.comoDinero())
        FilaDato(if (estimado) "Total estimado" else "Total", total.comoDinero(), destacado = true)
    }
}

@Composable
fun BotonCargarMas(visible: Boolean, cargando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (!visible) return
    Column(modifier = modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        if (cargando) CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 2.dp) else TextButton(onClick = onClick) { Text("Cargar más") }
    }
}
