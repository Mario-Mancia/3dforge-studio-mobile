package com.example.a3df_studio_app.ui.screens.cotizar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.domain.models.ArchivoLocal
import com.example.a3df_studio_app.domain.models.SolicitudCotizacion
import com.example.a3df_studio_app.domain.usecases.GetServiciosUseCase
import com.example.a3df_studio_app.domain.usecases.RequestQuoteUseCase
import com.example.a3df_studio_app.ui.classes.CotizarUiState
import com.example.a3df_studio_app.ui.classes.camposUi
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Cotización por formulario (otros servicios). La cotización automática 3D es exclusiva de la web. */
class CotizarViewModel(
    private val getServicios: GetServiciosUseCase,
    private val requestQuote: RequestQuoteUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(CotizarUiState())
    val estado: StateFlow<CotizarUiState> = _estado.asStateFlow()

    fun iniciar(servicioInicial: Long?) {
        if (servicioInicial != null && servicioInicial > 0) {
            _estado.update { it.copy(servicioId = servicioInicial) }
        }
        if (_estado.value.servicios.isNotEmpty()) return
        viewModelScope.launch {
            _estado.update { it.copy(cargandoServicios = true, error = null) }
            getServicios(null)
                .onSuccess { lista -> _estado.update { s -> s.copy(servicios = lista, cargandoServicios = false) } }
                .onFailure { e -> _estado.update { s -> s.copy(cargandoServicios = false, error = e.mensajeUi()) } }
        }
    }

    fun elegirServicio(id: Long) = _estado.update { it.copy(servicioId = id, campos = it.campos - "servicio") }
    fun cambiarCantidad(valor: String) = _estado.update { it.copy(cantidad = valor, campos = it.campos - "cantidad") }
    fun cambiarTitulo(valor: String) = _estado.update { it.copy(titulo = valor, campos = it.campos - "descripcion") }
    fun cambiarSolicitud(valor: String) = _estado.update { it.copy(solicitud = valor, campos = it.campos - "solicitud") }
    fun cambiarMaterial(valor: String) = _estado.update { it.copy(material = valor, campos = it.campos - "material") }
    fun cambiarColor(valor: String) = _estado.update { it.copy(color = valor, campos = it.campos - "color") }
    fun cambiarDimensiones(valor: String) = _estado.update { it.copy(dimensiones = valor, campos = it.campos - "dimensiones") }

    fun agregarArchivos(nuevos: List<ArchivoLocal>) {
        _estado.update { s ->
            val combinados = (s.archivos + nuevos).distinctBy { it.uri }.take(AppConfig.MAX_ARCHIVOS)
            s.copy(archivos = combinados, campos = s.campos - "archivos")
        }
    }

    fun quitarArchivo(indice: Int) {
        _estado.update { s -> s.copy(archivos = s.archivos.filterIndexed { i, _ -> i != indice }) }
    }

    fun enviar() {
        val s = _estado.value
        if (s.enviando) return
        viewModelScope.launch {
            _estado.update { it.copy(enviando = true, error = null, campos = emptyMap()) }
            requestQuote(
                SolicitudCotizacion(
                    servicioId = s.servicioId ?: 0L,
                    cantidad = s.cantidad,
                    titulo = s.titulo,
                    solicitud = s.solicitud,
                    material = s.material,
                    color = s.color,
                    dimensiones = s.dimensiones,
                    archivos = s.archivos
                )
            )
                .onSuccess { cotizacion -> _estado.update { st -> st.copy(enviando = false, creada = cotizacion.id) } }
                .onFailure { e -> _estado.update { st -> st.copy(enviando = false, error = e.mensajeUi(), campos = e.camposUi()) } }
        }
    }

    /** Limpia el borrador después de crear la solicitud (conserva la lista de servicios). */
    fun reiniciar() {
        _estado.update { CotizarUiState(servicios = it.servicios, cargandoServicios = false) }
    }
}
