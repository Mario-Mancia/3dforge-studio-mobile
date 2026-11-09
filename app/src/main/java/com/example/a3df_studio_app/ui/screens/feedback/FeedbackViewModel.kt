package com.example.a3df_studio_app.ui.screens.feedback

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.usecases.catalogo.GetServiciosUseCase
import com.example.a3df_studio_app.domain.usecases.encargos.GetMisEncargosUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.GetMisResenasUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.GetMisSolicitudesSoporteUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.SubmitResenaUseCase
import com.example.a3df_studio_app.domain.usecases.feedback.SubmitSupportRequestUseCase
import com.example.a3df_studio_app.ui.classes.FeedbackUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class FeedbackViewModel(
    private val getServiciosUseCase: GetServiciosUseCase,
    private val getMisEncargosUseCase: GetMisEncargosUseCase,
    private val getMisResenasUseCase: GetMisResenasUseCase,
    private val getMisSolicitudesSoporteUseCase: GetMisSolicitudesSoporteUseCase,
    private val submitResenaUseCase: SubmitResenaUseCase,
    private val submitSupportRequestUseCase: SubmitSupportRequestUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(FeedbackUiState())
    val uiState: StateFlow<FeedbackUiState> = _uiState.asStateFlow()

    fun cargar(isLoggedIn: Boolean) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val servicios = async { getServiciosUseCase() }
            if (isLoggedIn) {
                val encargos = async { getMisEncargosUseCase() }
                val resenas = async { getMisResenasUseCase() }
                val solicitudes = async { getMisSolicitudesSoporteUseCase() }
                _uiState.update {
                    it.copy(
                        encargos = encargos.await().getOrDefault(emptyList()),
                        misResenas = resenas.await().getOrDefault(emptyList()),
                        misSolicitudes = solicitudes.await().getOrDefault(emptyList())
                    )
                }
            }
            _uiState.update { it.copy(isLoading = false, servicios = servicios.await().getOrDefault(emptyList())) }
        }
    }

    fun onPuntuacion(valor: Int) = _uiState.update { it.copy(puntuacion = valor) }
    fun onComentario(texto: String) = _uiState.update { it.copy(comentario = texto) }
    fun onAsunto(texto: String) = _uiState.update { it.copy(asunto = texto) }
    fun onDescripcion(texto: String) = _uiState.update { it.copy(descripcion = texto) }
    fun onPrioridad(valor: PrioridadSoporte) = _uiState.update { it.copy(prioridad = valor) }

    fun seleccionarServicio(id: Long?) = _uiState.update { it.copy(servicioSeleccionado = id, encargoSeleccionado = null) }
    fun seleccionarEncargo(id: Long?) = _uiState.update { it.copy(encargoSeleccionado = id, servicioSeleccionado = null) }

    fun enviarResena() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            submitResenaUseCase(estado.puntuacion, estado.comentario, estado.servicioSeleccionado, estado.encargoSeleccionado).fold(
                onSuccess = { resena ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            puntuacion = 0,
                            comentario = "",
                            misResenas = listOf(resena) + it.misResenas,
                            mensaje = "¡Gracias por tu opinión!"
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun enviarSoporte() {
        val estado = _uiState.value
        if (estado.isSending) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSending = true) }
            submitSupportRequestUseCase(estado.asunto, estado.descripcion, estado.prioridad).fold(
                onSuccess = { solicitud ->
                    _uiState.update {
                        it.copy(
                            isSending = false,
                            asunto = "",
                            descripcion = "",
                            misSolicitudes = listOf(solicitud) + it.misSolicitudes,
                            mensaje = "Solicitud enviada. Te contactaremos pronto."
                        )
                    }
                },
                onFailure = { e -> _uiState.update { it.copy(isSending = false, mensaje = e.message) } }
            )
        }
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
