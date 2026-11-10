package com.example.a3df_studio_app.ui.screens.seguimiento

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.Notificacion
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.usecases.CloseSupportUseCase
import com.example.a3df_studio_app.domain.usecases.CreateSupportUseCase
import com.example.a3df_studio_app.domain.usecases.GetSupportUseCase
import com.example.a3df_studio_app.domain.usecases.ListNotificationsUseCase
import com.example.a3df_studio_app.domain.usecases.ListSupportUseCase
import com.example.a3df_studio_app.domain.usecases.MarkAllNotificationsReadUseCase
import com.example.a3df_studio_app.domain.usecases.MarkNotificationReadUseCase
import com.example.a3df_studio_app.ui.classes.NotificacionesUiState
import com.example.a3df_studio_app.ui.classes.SoporteUiState
import com.example.a3df_studio_app.ui.classes.camposUi
import com.example.a3df_studio_app.ui.classes.cargarPagina
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class NotificacionesViewModel(
    private val listNotifications: ListNotificationsUseCase,
    private val markRead: MarkNotificationReadUseCase,
    private val markAllRead: MarkAllNotificationsReadUseCase,
    private val alCambiarNoLeidas: (Int) -> Unit
) : ViewModel() {

    private val _estado = MutableStateFlow(NotificacionesUiState())
    val estado: StateFlow<NotificacionesUiState> = _estado.asStateFlow()

    fun limpiar() {
        _estado.value = NotificacionesUiState()
    }

    fun cargar(reiniciar: Boolean) {
        viewModelScope.launch {
            cargarPagina(
                actual = _estado.value.lista,
                reiniciar = reiniciar,
                pedir = { pagina -> listNotifications(pagina) },
                publicar = { lista -> _estado.update { s -> s.copy(lista = lista) } }
            )
        }
    }

    fun marcarLeida(notificacion: Notificacion) {
        if (notificacion.leida) return
        viewModelScope.launch {
            markRead(notificacion.id).onSuccess { restantes ->
                alCambiarNoLeidas(restantes)
                _estado.update { s ->
                    s.copy(lista = s.lista.copy(items = s.lista.items.map { if (it.id == notificacion.id) it.copy(leida = true) else it }))
                }
            }
        }
    }

    fun marcarTodas() {
        viewModelScope.launch {
            markAllRead().onSuccess { restantes ->
                alCambiarNoLeidas(restantes)
                _estado.update { s -> s.copy(lista = s.lista.copy(items = s.lista.items.map { it.copy(leida = true) })) }
            }
        }
    }
}

class SoporteViewModel(
    private val listSupport: ListSupportUseCase,
    private val getSupport: GetSupportUseCase,
    private val createSupport: CreateSupportUseCase,
    private val closeSupport: CloseSupportUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(SoporteUiState())
    val estado: StateFlow<SoporteUiState> = _estado.asStateFlow()

    fun limpiar() {
        _estado.value = SoporteUiState()
    }

    fun cargar(reiniciar: Boolean) {
        viewModelScope.launch {
            cargarPagina(
                actual = _estado.value.lista,
                reiniciar = reiniciar,
                pedir = { pagina -> listSupport(pagina) },
                publicar = { lista -> _estado.update { s -> s.copy(lista = lista) } }
            )
        }
    }

    fun cargarDetalle(id: Long) {
        viewModelScope.launch {
            _estado.update { it.copy(cargandoDetalle = true, errorDetalle = null, detalle = null) }
            getSupport(id)
                .onSuccess { solicitud -> _estado.update { s -> s.copy(cargandoDetalle = false, detalle = solicitud) } }
                .onFailure { e -> _estado.update { s -> s.copy(cargandoDetalle = false, errorDetalle = e.mensajeUi()) } }
        }
    }

    fun cerrar() {
        val solicitud = _estado.value.detalle ?: return
        viewModelScope.launch {
            _estado.update { it.copy(cerrando = true, errorDetalle = null) }
            closeSupport(solicitud.id)
                .onSuccess { actualizada ->
                    _estado.update { s ->
                        s.copy(
                            cerrando = false,
                            detalle = actualizada,
                            lista = s.lista.copy(items = s.lista.items.map { if (it.id == actualizada.id) actualizada else it })
                        )
                    }
                }
                .onFailure { e -> _estado.update { s -> s.copy(cerrando = false, errorDetalle = e.mensajeUi()) } }
        }
    }

    fun cambiarAsunto(valor: String) = _estado.update { it.copy(asunto = valor, campos = it.campos - "asunto") }
    fun cambiarDescripcion(valor: String) = _estado.update { it.copy(descripcion = valor, campos = it.campos - "descripcion") }
    fun cambiarPrioridad(valor: PrioridadSoporte) = _estado.update { it.copy(prioridad = valor) }

    fun enviar() {
        val s = _estado.value
        if (s.enviando) return
        viewModelScope.launch {
            _estado.update { it.copy(enviando = true, error = null, campos = emptyMap()) }
            createSupport(s.asunto, s.descripcion, s.prioridad)
                .onSuccess { creada ->
                    _estado.update { st ->
                        st.copy(
                            enviando = false, creada = creada.id, asunto = "", descripcion = "", prioridad = PrioridadSoporte.MEDIA,
                            lista = st.lista.copy(items = listOf(creada) + st.lista.items)
                        )
                    }
                }
                .onFailure { e -> _estado.update { st -> st.copy(enviando = false, error = e.mensajeUi(), campos = e.camposUi()) } }
        }
    }

    fun consumirCreada() {
        _estado.update { it.copy(creada = null) }
    }
}
