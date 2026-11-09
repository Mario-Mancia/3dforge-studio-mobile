package com.example.a3df_studio_app.ui.screens.cotizaciones

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.ItemCotizacion
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.usecases.catalogo.GetServiciosUseCase
import com.example.a3df_studio_app.domain.usecases.cotizaciones.GetMisCotizacionesUseCase
import com.example.a3df_studio_app.domain.usecases.cotizaciones.RequestAdvisoryUseCase
import com.example.a3df_studio_app.domain.usecases.cotizaciones.RequestQuoteUseCase
import com.example.a3df_studio_app.ui.classes.CotizacionesUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CotizacionesViewModel(
    private val getServiciosUseCase: GetServiciosUseCase,
    private val requestQuoteUseCase: RequestQuoteUseCase,
    private val requestAdvisoryUseCase: RequestAdvisoryUseCase,
    private val getMisCotizacionesUseCase: GetMisCotizacionesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(CotizacionesUiState())
    val uiState: StateFlow<CotizacionesUiState> = _uiState.asStateFlow()

    init {
        cargarServicios()
    }

    fun cargarServicios() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getServiciosUseCase().fold(
                onSuccess = { lista -> _uiState.update { it.copy(isLoading = false, servicios = lista) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun cargarMisCotizaciones() {
        viewModelScope.launch {
            getMisCotizacionesUseCase().onSuccess { lista -> _uiState.update { it.copy(misCotizaciones = lista) } }
        }
    }

    fun agregarServicio(servicio: Servicio) {
        _uiState.update { estado ->
            if (estado.seleccion.any { it.servicio.id == servicio.id }) estado
            else estado.copy(seleccion = estado.seleccion + ItemCotizacion(servicio))
        }
    }

    fun cambiarCantidad(idServicio: Long, delta: Int) {
        _uiState.update { estado ->
            estado.copy(seleccion = estado.seleccion.mapNotNull { item ->
                if (item.servicio.id != idServicio) item
                else (item.cantidad + delta).takeIf { it >= 1.0 }?.let { item.copy(cantidad = it) }
            })
        }
    }

    fun onObservaciones(texto: String) = _uiState.update { it.copy(observaciones = texto) }
    fun onTema(texto: String) = _uiState.update { it.copy(temaAsesoria = texto) }
    fun onDetalle(texto: String) = _uiState.update { it.copy(detalleAsesoria = texto) }
    fun onHorario(texto: String) = _uiState.update { it.copy(horarioAsesoria = texto) }

    fun solicitarCotizacion() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            requestQuoteUseCase(estado.seleccion, estado.observaciones).fold(
                onSuccess = { solicitud ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            seleccion = emptyList(),
                            observaciones = "",
                            whatsAppIntent = solicitud.intent,
                            mensaje = solicitud.codigoReferencia?.let { codigo -> "Cotización $codigo registrada" }
                        )
                    }
                    cargarMisCotizaciones()
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun solicitarAsesoria() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            requestAdvisoryUseCase(estado.temaAsesoria, estado.detalleAsesoria, estado.horarioAsesoria).fold(
                onSuccess = { solicitud ->
                    _uiState.update {
                        it.copy(isSending = false, temaAsesoria = "", detalleAsesoria = "", horarioAsesoria = "", whatsAppIntent = solicitud.intent)
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun consumirIntent() = _uiState.update { it.copy(whatsAppIntent = null) }
    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
