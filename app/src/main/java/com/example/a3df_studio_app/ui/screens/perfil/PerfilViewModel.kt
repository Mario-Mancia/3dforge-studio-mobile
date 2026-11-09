package com.example.a3df_studio_app.ui.screens.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.ClienteUpdate
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.domain.usecases.auth.LogoutUseCase
import com.example.a3df_studio_app.domain.usecases.perfil.GetClienteProfileUseCase
import com.example.a3df_studio_app.domain.usecases.perfil.UpdateClienteProfileUseCase
import com.example.a3df_studio_app.ui.classes.PerfilUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val getClienteProfileUseCase: GetClienteProfileUseCase,
    private val updateClienteProfileUseCase: UpdateClienteProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getClienteProfileUseCase().fold(
                onSuccess = ::aplicar,
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    private fun aplicar(cliente: Cliente) {
        _uiState.update {
            it.copy(
                isLoading = false,
                isSaving = false,
                correo = cliente.correo,
                tipo = cliente.tipo,
                nombres = cliente.nombres.orEmpty(),
                apellidos = cliente.apellidos.orEmpty(),
                nombreEmpresa = cliente.nombreEmpresa.orEmpty(),
                telefono = cliente.telefono.orEmpty(),
                identificacionFiscal = cliente.identificacionFiscal.orEmpty(),
                direccion = cliente.direccion.orEmpty(),
                perfilCompleto = cliente.perfilCompleto
            )
        }
    }

    fun onTipo(valor: TipoCliente) = _uiState.update { it.copy(tipo = valor) }
    fun onNombres(valor: String) = _uiState.update { it.copy(nombres = valor) }
    fun onApellidos(valor: String) = _uiState.update { it.copy(apellidos = valor) }
    fun onEmpresa(valor: String) = _uiState.update { it.copy(nombreEmpresa = valor) }
    fun onTelefono(valor: String) = _uiState.update { it.copy(telefono = valor) }
    fun onIdentificacion(valor: String) = _uiState.update { it.copy(identificacionFiscal = valor) }
    fun onDireccion(valor: String) = _uiState.update { it.copy(direccion = valor) }

    fun guardar() {
        val e = _uiState.value
        if (e.isSaving) return
        val cambios = ClienteUpdate(e.tipo, e.nombres, e.apellidos, e.nombreEmpresa, e.telefono, e.identificacionFiscal, e.direccion)
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, mensaje = null) }
            updateClienteProfileUseCase(cambios).fold(
                onSuccess = { cliente ->
                    aplicar(cliente)
                    _uiState.update { it.copy(mensaje = "Perfil actualizado") }
                },
                onFailure = { ex -> _uiState.update { it.copy(isSaving = false, mensaje = ex.message) } }
            )
        }
    }

    fun cerrarSesion(onListo: () -> Unit) {
        viewModelScope.launch {
            logoutUseCase()
            _uiState.value = PerfilUiState()
            onListo()
        }
    }

    fun consumirMensaje() = _uiState.update { it.copy(mensaje = null) }
}
