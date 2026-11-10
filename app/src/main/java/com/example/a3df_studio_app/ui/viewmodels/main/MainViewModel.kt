package com.example.a3df_studio_app.ui.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.AuthState
import com.example.a3df_studio_app.domain.models.InfoNegocio
import com.example.a3df_studio_app.domain.usecases.GetNegocioUseCase
import com.example.a3df_studio_app.domain.usecases.GetUnreadCountUseCase
import com.example.a3df_studio_app.domain.usecases.ObserveAuthStateUseCase
import com.example.a3df_studio_app.domain.usecases.ObserveCartUseCase
import com.example.a3df_studio_app.domain.usecases.RestoreSessionUseCase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado global: sesión, negocio, contador de avisos y cantidad de productos en el carrito. */
class MainViewModel(
    observeAuth: ObserveAuthStateUseCase,
    private val restoreSession: RestoreSessionUseCase,
    private val getUnreadCount: GetUnreadCountUseCase,
    private val getNegocio: GetNegocioUseCase,
    observeCart: ObserveCartUseCase
) : ViewModel() {

    val authState: StateFlow<AuthState> = observeAuth()

    private val _negocio = MutableStateFlow<InfoNegocio?>(null)
    val negocio: StateFlow<InfoNegocio?> = _negocio.asStateFlow()

    private val _noLeidas = MutableStateFlow(0)
    val noLeidas: StateFlow<Int> = _noLeidas.asStateFlow()

    val cantidadCarrito: StateFlow<Int> = observeCart()
        .map { items -> items.sumOf { it.cantidad } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        viewModelScope.launch { restoreSession() }
        cargarNegocio()
        viewModelScope.launch {
            authState.collectLatest { estado ->
                if (estado is AuthState.ConSesion) {
                    while (true) {
                        getUnreadCount().onSuccess { _noLeidas.value = it }
                        delay(60_000)
                    }
                } else {
                    _noLeidas.value = 0
                }
            }
        }
    }

    fun cargarNegocio() {
        viewModelScope.launch { getNegocio().onSuccess { _negocio.value = it } }
    }

    fun fijarNoLeidas(valor: Int) {
        _noLeidas.value = valor
    }
}
