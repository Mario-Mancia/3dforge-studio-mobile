package com.example.a3df_studio_app.ui.screens.cotizar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.Handyman
import androidx.compose.material.icons.rounded.Remove
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.a3df_studio_app.domain.models.Estimacion3d
import com.example.a3df_studio_app.domain.utils.comoDinero
import com.example.a3df_studio_app.domain.utils.formatearTamano
import com.example.a3df_studio_app.ui.classes.leerArchivoLocal
import com.example.a3df_studio_app.ui.classes.primerError
import com.example.a3df_studio_app.ui.components.common.BannerMensaje
import com.example.a3df_studio_app.ui.components.common.BloqueTotales
import com.example.a3df_studio_app.ui.components.common.BotonPrimario
import com.example.a3df_studio_app.ui.components.common.ErrorState
import com.example.a3df_studio_app.ui.components.common.FilaDato
import com.example.a3df_studio_app.ui.components.common.ForgeCard
import com.example.a3df_studio_app.ui.components.common.ForgeTextField
import com.example.a3df_studio_app.ui.components.common.LoadingState
import com.example.a3df_studio_app.ui.components.common.MessageState
import com.example.a3df_studio_app.ui.components.common.StatusChip
import com.example.a3df_studio_app.ui.components.topbar.BackTopBar
import com.example.a3df_studio_app.ui.theme.ForgeSuccess
import com.example.a3df_studio_app.ui.theme.ForgeWarning
import java.util.Locale

private const val AVISO_REVISION =
    "Este precio es una estimación automática y puede cambiar después de que un profesional revise tu solicitud. " +
        "El precio final lo confirma nuestro equipo."

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Cotizar3DScreen(
    viewModel: Cotizar3DViewModel,
    onBack: () -> Unit,
    onFormulario: () -> Unit,
    onCreada: (Long) -> Unit
) {
    val estado by viewModel.estado.collectAsStateWithLifecycle()
    val contexto = LocalContext.current

    val selector = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) viewModel.elegirArchivo(leerArchivoLocal(contexto, uri))
    }

    LaunchedEffect(Unit) { viewModel.iniciar() }
    LaunchedEffect(estado.creada) {
        estado.creada?.let { id ->
            viewModel.reiniciar()
            onCreada(id)
        }
    }

    Scaffold(topBar = { BackTopBar("Cotización automática 3D", onBack) }) { padding ->
        val opciones = estado.opciones
        val seleccion = estado.seleccion
        val estimacion = estado.estimacion
        when {
            estado.cargandoOpciones && opciones == null -> LoadingState(Modifier.padding(padding))
            opciones == null -> ErrorState(estado.errorOpciones.orEmpty(), { viewModel.cargarOpciones() }, Modifier.padding(padding))
            !opciones.disponible || seleccion == null -> MessageState(
                icono = Icons.Rounded.Handyman,
                titulo = "Cotización automática no disponible",
                mensaje = "Aún no hay materiales configurados. Puedes enviar tu solicitud con el formulario y un profesional la revisará.",
                modifier = Modifier.padding(padding),
                accion = "Usar el formulario",
                onAccion = onFormulario
            )
            else -> Column(
                modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ForgeCard(modifier = Modifier.fillMaxWidth()) {
                    Text("Cotiza tu pieza en segundos", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(
                        "Adjunta tu modelo 3D, elige el material y las opciones, y verás el tiempo, el peso y el precio estimados.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(AVISO_REVISION, style = MaterialTheme.typography.bodySmall, color = ForgeWarning)
                }

                // ---- 1. Archivo ----
                Text("1. Modelo 3D", style = MaterialTheme.typography.titleMedium)
                val archivo = estado.archivo
                if (archivo != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.AttachFile, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                            Text(archivo.nombre, style = MaterialTheme.typography.bodyMedium)
                            Text(formatearTamano(archivo.tamano), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
                OutlinedButton(onClick = { selector.launch(arrayOf("*/*")) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (archivo == null) "Elegir archivo" else "Cambiar archivo")
                }
                Text(
                    "Formatos: ${opciones.formatos.joinToString(", ") { it.uppercase() }}. Máximo ${opciones.tamanoMaximoMb} MB." +
                        if (opciones.camaMm.size == 3) " Tamaño máximo de pieza: ${opciones.camaMm.joinToString(" × ") { String.format(Locale.US, "%.0f", it) }} mm." else "",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                BannerMensaje(estado.campos.primerError("archivo"))

                // ---- 2. Opciones ----
                Text("2. Opciones de impresión", style = MaterialTheme.typography.titleMedium)
                GrupoChips("Material", opciones.filamentos.map { it.id.toString() to it.nombre }, seleccion.filamentoId.toString()) {
                    viewModel.elegirFilamento(it.toLong())
                }
                GrupoChips("Relleno", opciones.rellenos.map { it.toString() to "$it %" }, seleccion.relleno.toString()) {
                    viewModel.elegirRelleno(it.toInt())
                }
                GrupoChips("Calidad", opciones.calidades.map { it.codigo to it.texto }, seleccion.calidad) { viewModel.elegirCalidad(it) }
                GrupoChips("Escala", opciones.escalas.map { it.toString() to "$it %" }, seleccion.escala.toString()) {
                    viewModel.elegirEscala(it.toInt())
                }
                GrupoChips("Unidades del archivo", opciones.unidades.map { it.codigo to it.texto }, seleccion.unidades) { viewModel.elegirUnidades(it) }
                GrupoChips("Soportes", opciones.soportes.map { it.codigo to it.texto }, seleccion.soportes) { viewModel.elegirSoportes(it) }
                Text("Cantidad", style = MaterialTheme.typography.titleSmall)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    IconButton(onClick = { viewModel.cambiarCantidad(seleccion.cantidad - 1) }) { Icon(Icons.Rounded.Remove, contentDescription = "Menos") }
                    Text("${seleccion.cantidad}", style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = { viewModel.cambiarCantidad(seleccion.cantidad + 1) }) { Icon(Icons.Rounded.Add, contentDescription = "Más") }
                }

                // ---- 3. Estimación ----
                Text("3. Estimación", style = MaterialTheme.typography.titleMedium)
                ForgeCard(modifier = Modifier.fillMaxWidth()) {
                    when {
                        archivo == null -> Text(
                            "Elige un archivo para ver la estimación.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        estimacion == null && estado.estimando -> Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(22.dp), strokeWidth = 2.dp)
                            Spacer(Modifier.width(12.dp))
                            Text("Analizando el modelo… puede tardar unos segundos.", style = MaterialTheme.typography.bodyMedium)
                        }
                        estimacion == null -> {
                            BannerMensaje(estado.errorEstimacion ?: "No se pudo calcular la estimación.")
                            OutlinedButton(onClick = { viewModel.calcular() }, modifier = Modifier.fillMaxWidth()) { Text("Reintentar") }
                        }
                        else -> ResultadoEstimacion(estimacion, estado.estimando)
                    }
                }

                // ---- 4. Datos opcionales y envío ----
                Text("4. Solicitar", style = MaterialTheme.typography.titleMedium)
                ForgeTextField(estado.nombrePieza, viewModel::cambiarNombre, "Nombre de la pieza (opcional)", error = estado.campos.primerError("descripcion"))
                ForgeTextField(
                    estado.comentarios, viewModel::cambiarComentarios, "Comentarios para el equipo (opcional)",
                    error = estado.campos.primerError("comentarios"), lineas = 3
                )
                BannerMensaje(estado.error)
                Text(AVISO_REVISION, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                BotonPrimario(
                    texto = "Solicitar cotización",
                    onClick = { viewModel.solicitar() },
                    cargando = estado.enviando,
                    habilitado = archivo != null && estimacion != null && estimacion.cabe && !estado.estimando
                )
                OutlinedButton(onClick = onFormulario, modifier = Modifier.fillMaxWidth()) { Text("Prefiero usar el formulario") }
                Spacer(Modifier.height(16.dp))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GrupoChips(titulo: String, opciones: List<Pair<String, String>>, elegido: String, onElegir: (String) -> Unit) {
    Text(titulo, style = MaterialTheme.typography.titleSmall)
    Row(modifier = Modifier.horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        opciones.forEach { (codigo, texto) ->
            FilterChip(selected = codigo == elegido, onClick = { onElegir(codigo) }, label = { Text(texto) })
        }
    }
}

@Composable
private fun ResultadoEstimacion(e: Estimacion3d, recalculando: Boolean) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        if (recalculando) LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        if (!e.cabe) BannerMensaje(e.mensajeCabida.ifBlank { "La pieza no cabe en la impresora. Reduce la escala o usa el formulario." })
        FilaDato("Dimensiones", e.dimensionesTexto)
        FilaDato("Peso estimado", String.format(Locale.US, "%.1f g", e.gramos))
        FilaDato("Tiempo de impresión", e.tiempoTexto)
        FilaDato("Soportes", if (e.soportes) "Sí" else "No")
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("Confianza de la estimación", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            val color: Color = when (e.confianza) {
                "alta" -> ForgeSuccess
                "baja" -> MaterialTheme.colorScheme.error
                else -> ForgeWarning
            }
            StatusChip(e.confianzaTexto, color)
        }
        Spacer(Modifier.height(6.dp))
        FilaDato("Precio por pieza", e.precioUnitario.comoDinero())
        BloqueTotales(e.subtotal, e.impuesto, e.total, estimado = true)
        e.advertencias.forEach { advertencia ->
            Text("• $advertencia", style = MaterialTheme.typography.bodySmall, color = ForgeWarning)
        }
        Spacer(Modifier.height(4.dp))
        Text(e.aviso.ifBlank { AVISO_REVISION }, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(AVISO_REVISION, style = MaterialTheme.typography.labelMedium, color = ForgeWarning)
    }
}
