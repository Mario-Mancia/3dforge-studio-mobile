package com.example.a3df_studio_app.ui.viewmodels.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.AuthState
import com.example.a3df_studio_app.domain.usecases.auth.ObserveAuthStateUseCase
import com.example.a3df_studio_app.domain.usecases.perfil.GetClienteProfileUseCase
import com.example.a3df_studio_app.domain.usecases.perfil.ObserveClienteProfileUseCase
import com.example.a3df_studio_app.ui.classes.MainUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MainViewModel(
    private val observeAuthStateUseCase: ObserveAuthStateUseCase,
    private val observeClienteProfileUseCase: ObserveClienteProfileUseCase,
    private val getClienteProfileUseCase: GetClienteProfileUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(observeAuthStateUseCase(), observeClienteProfileUseCase()) { auth, cliente ->
                MainUiState(
                    authState = auth,
                    cliente = if (auth is AuthState.Authenticated) cliente else null
                )
            }.collect { _uiState.value = it }
        }
        viewModelScope.launch {
            observeAuthStateUseCase()
                .map { (it as? AuthState.Authenticated)?.user?.id }
                .distinctUntilChanged()
                .collect { id -> if (id != null) getClienteProfileUseCase() }
        }
    }
}
