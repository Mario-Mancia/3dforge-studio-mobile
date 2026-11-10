package com.example.a3df_studio_app.ui.screens.cotizar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.domain.utils.formatearTamano
import com.example.a3df_studio_app.ui.classes.leerArchivoLocal
import com.example.a3df_studio_app.ui.classes.primerError
import com.example.a3df_studio_app.ui.components.common.BannerMensaje
import com.example.a3df_studio_app.ui.components.common.BotonPrimario
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ForgeTextField
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.topbar.BackTopBar

@Composable
fun CotizarScreen(
    servicioId: Long?,
    viewModel: CotizarViewModel,
    onBack: () -> Unit,
    onCreada: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current
    var menuServicios by remember { mutableStateOf(false) }

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        viewModel.agregarArchivos(uris.map { leerArchivoLocal(contexto, it) })
    }

    LaunchedEffect(servicioId) { viewModel.iniciar(servicioId) }
    LaunchedEffect(estado.creada) {
        estado.creada?.let { id ->
            viewModel.reiniciar()
            onCreada(id)
        }
    }

    Scaffold(topBar = { BackTopBar("Solicitar cotización", onBack) }) { padding ->
        if (estado.cargandoServicios && estado.servicios.isEmpty()) {
            LoadingState(Modifier.padding(padding))
            return@Scaffold
        }
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ForgeCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Cuéntanos qué necesitas (impresión, modelado, diseño, reparación…). Un profesional revisa tu solicitud y te envía una propuesta con precio y plazo. " +
                        "Si tienes un modelo 3D (STL, OBJ o 3MF), usa la cotización automática desde Actividad > Cotizaciones.",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Text("Servicio", style = MaterialTheme.typography.titleSmall)
            Box {
                OutlinedButton(onClick = { menuServicios = true }, modifier = Modifier.fillMaxWidth()) {
                    Text(estado.servicio?.nombre ?: "Elige un servicio")
                }
                DropdownMenu(expanded = menuServicios, onDismissRequest = { menuServicios = false }) {
                    estado.servicios.forEach { servicio ->
                        DropdownMenuItem(
                            text = { Text("${servicio.nombre} · ${servicio.categoria}") },
                            onClick = {
                                viewModel.elegirServicio(servicio.id)
                                menuServicios = false
                            }
                        )
                    }
                }
            }
            BannerMensaje(estado.campos.primerError("servicio"))
            estado.servicio?.let { servicio ->
                if (servicio.requiereArchivo) {
                    Text("Este servicio requiere al menos un archivo adjunto.", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                }
            }

            ForgeTextField(estado.cantidad, viewModel::cambiarCantidad, "Cantidad", error = estado.campos.primerError("cantidad"), teclado = KeyboardType.Decimal)
            ForgeTextField(estado.titulo, viewModel::cambiarTitulo, "Título de la solicitud", error = estado.campos.primerError("descripcion"))
            ForgeTextField(
                estado.solicitud, viewModel::cambiarSolicitud, "Descripción detallada",
                error = estado.campos.primerError("solicitud"), ayuda = "Mínimo 10 caracteres.", lineas = 4
            )
            ForgeTextField(estado.material, viewModel::cambiarMaterial, "Material (opcional)", error = estado.campos.primerError("material"))
            ForgeTextField(estado.color, viewModel::cambiarColor, "Color (opcional)", error = estado.campos.primerError("color"))
            ForgeTextField(estado.dimensiones, viewModel::cambiarDimensiones, "Dimensiones (opcional)", error = estado.campos.primerError("dimensiones"))

            Text("Archivos", style = MaterialTheme.typography.titleSmall)
            Text(
                "STL, OBJ, 3MF, STEP, PNG, JPG o PDF. Máximo ${AppConfig.MAX_ARCHIVOS} archivos de 50 MB.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            estado.archivos.forEachIndexed { indice, archivo ->
                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                        Text(archivo.nombre, style = MaterialTheme.typography.bodyMedium)
                        Text(formatearTamano(archivo.tamano), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.quitarArchivo(indice) }) { Icon(Icons.Rounded.Close, contentDescription = "Quitar") }
                }
            }
            OutlinedButton(
                onClick = { selector.launch(arrayOf("*/*")) },
                enabled = estado.archivos.size < AppConfig.MAX_ARCHIVOS,
                modifier = Modifier.fillMaxWidth()
            ) { Text("Adjuntar archivos") }
            BannerMensaje(estado.campos.primerError("archivos"))

            BannerMensaje(estado.error)
            Spacer(Modifier.height(4.dp))
            BotonPrimario("Enviar solicitud", { viewModel.enviar() }, cargando = estado.enviando)
            Spacer(Modifier.height(16.dp))
        }
    }
}
