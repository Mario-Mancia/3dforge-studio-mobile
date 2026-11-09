package com.example.a3df_studio_app.ui.screens.pago

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.SearchOff
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.utils.comoMoneda
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.screens.pedidos.PedidosViewModel

@Composable
fun PagoScreen(
    idFactura: Long,
    pedidosViewModel: PedidosViewModel,
    pagoViewModel: PagoViewModel,
    onFinalizado: () -> Unit
) {
    val pedidos by pedidosViewModel.uiState.collectAsState()
    val uiState by pagoViewModel.uiState.collectAsState()
    val factura = pedidos.facturas.firstOrNull { it.id == idFactura }

    DisposableEffect(Unit) { onDispose { pagoViewModel.reiniciar() } }
    LaunchedEffect(idFactura) {
        if (pedidosViewModel.factura(idFactura) == null) pedidosViewModel.cargar()
    }

    if (factura == null) {
        MessageState(Icons.Outlined.SearchOff, "Cargando factura…", "Si no aparece, revisa la pestaña Facturas en Pedidos.")
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(Modifier.padding(20.dp)) {
                Row {
                    Text(uiState.marca.etiqueta, style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                    Icon(Icons.Rounded.CreditCard, contentDescription = null)
                }
                Spacer(Modifier.height(24.dp))
                Text(
                    uiState.numero.padEnd(16, '•').chunked(4).joinToString(" "),
                    style = MaterialTheme.typography.titleLarge
                )
                Spacer(Modifier.height(12.dp))
                Row {
                    Text(uiState.titular.ifBlank { "NOMBRE DEL TITULAR" }.uppercase(), modifier = Modifier.weight(1f))
                    Text(uiState.expiracion.padEnd(4, '•').chunked(2).joinToString("/"))
                }
            }
        }

        Text("Factura ${factura.numero}", style = MaterialTheme.typography.titleMedium)
        Text(
            "Saldo a pagar: ${factura.saldoPendiente.comoMoneda()}",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.primary
        )

        OutlinedTextField(
            value = uiState.numero,
            onValueChange = pagoViewModel::onNumero,
            label = { Text("Número de tarjeta") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = uiState.titular,
            onValueChange = pagoViewModel::onTitular,
            label = { Text("Nombre del titular") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = uiState.expiracion,
                onValueChange = pagoViewModel::onExpiracion,
                label = { Text("MMAA") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = uiState.cvv,
                onValueChange = pagoViewModel::onCvv,
                label = { Text("CVV") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                singleLine = true,
                modifier = Modifier.weight(1f)
            )
        }

        uiState.error?.let { Text(it, color = MaterialTheme.colorScheme.error) }

        Button(
            onClick = { pagoViewModel.pagar(factura) },
            enabled = !uiState.isProcessing,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isProcessing) {
                CircularProgressIndicator(Modifier.size(18.dp), strokeWidth = 2.dp)
            } else {
                Icon(Icons.Rounded.Lock, contentDescription = null)
                Text("  Pagar ${factura.saldoPendiente.comoMoneda()}")
            }
        }

        Text(
            "Pago simulado con fines académicos. No se almacena el número de tarjeta, solo sus últimos 4 dígitos. " +
                "Prueba con 4242 4242 4242 4242 (aprobada) o 4000 0000 0000 0002 (rechazada).",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    uiState.resultado?.let { pago ->
        AlertDialog(
            onDismissRequest = {},
            title = { Text("¡Pago aprobado!") },
            text = {
                Text("Referencia ${pago.referencia}\nEstado de la factura: ${pago.estadoFactura.etiqueta}\nSaldo pendiente: ${pago.saldoPendiente.comoMoneda()}")
            },
            confirmButton = {
                Button(onClick = {
                    pedidosViewModel.cargar()
                    onFinalizado()
                }) { Text("Listo") }
            }
        )
    }
}
