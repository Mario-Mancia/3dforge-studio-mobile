package com.example.a3df_studio_app.ui.screens.cuenta

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.CambiosPerfil
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.domain.usecases.ChangePasswordUseCase
import com.example.a3df_studio_app.domain.usecases.GetNegocioUseCase
import com.example.a3df_studio_app.domain.usecases.GetProfileUseCase
import com.example.a3df_studio_app.domain.usecases.LogoutUseCase
import com.example.a3df_studio_app.domain.usecases.UpdateProfileUseCase
import com.example.a3df_studio_app.ui.classes.CuentaUiState
import com.example.a3df_studio_app.ui.classes.camposUi
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CuentaViewModel(
    private val getProfile: GetProfileUseCase,
    private val updateProfile: UpdateProfileUseCase,
    private val changePassword: ChangePasswordUseCase,
    private val logout: LogoutUseCase,
    private val getNegocio: GetNegocioUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(CuentaUiState())
    val estado: StateFlow<CuentaUiState> = _estado.asStateFlow()

    fun limpiar() {
        _estado.value = CuentaUiState()
    }

    private fun CuentaUiState.conCliente(cliente: Cliente): CuentaUiState = copy(
        cliente = cliente,
        tipo = cliente.tipo,
        nombres = cliente.nombres,
        apellidos = cliente.apellidos,
        nombreEmpresa = cliente.nombreEmpresa,
        telefono = cliente.telefono,
        identificacionFiscal = cliente.identificacionFiscal,
        direccion = cliente.direccion
    )

    fun cargar() {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            val negocio = getNegocio().getOrNull()
            getProfile()
                .onSuccess { cliente -> _estado.update { s -> s.conCliente(cliente).copy(cargando = false, negocio = negocio ?: s.negocio) } }
                .onFailure { e -> _estado.update { s -> s.copy(cargando = false, error = e.mensajeUi(), negocio = negocio ?: s.negocio) } }
        }
    }

    fun cambiarTipo(valor: TipoCliente) = _estado.update { it.copy(tipo = valor) }
    fun cambiarNombres(valor: String) = _estado.update { it.copy(nombres = valor, campos = it.campos - "nombres") }
    fun cambiarApellidos(valor: String) = _estado.update { it.copy(apellidos = valor, campos = it.campos - "apellidos") }
    fun cambiarEmpresa(valor: String) = _estado.update { it.copy(nombreEmpresa = valor, campos = it.campos - "nombre_empresa") }
    fun cambiarTelefono(valor: String) = _estado.update { it.copy(telefono = valor, campos = it.campos - "telefono") }
    fun cambiarFiscal(valor: String) = _estado.update { it.copy(identificacionFiscal = valor, campos = it.campos - "identificacion_fiscal") }
    fun cambiarDireccion(valor: String) = _estado.update { it.copy(direccion = valor, campos = it.campos - "direccion") }

    fun guardar() {
        val s = _estado.value
        if (s.guardando) return
        viewModelScope.launch {
            _estado.update { it.copy(guardando = true, error = null, campos = emptyMap(), mensaje = null) }
            updateProfile(
                CambiosPerfil(
                    tipo = s.tipo,
                    nombres = s.nombres,
                    apellidos = s.apellidos,
                    nombreEmpresa = s.nombreEmpresa,
                    telefono = s.telefono,
                    identificacionFiscal = s.identificacionFiscal,
                    direccion = s.direccion
                )
            )
                .onSuccess { cliente -> _estado.update { st -> st.conCliente(cliente).copy(guardando = false, mensaje = "Datos actualizados correctamente.") } }
                .onFailure { e -> _estado.update { st -> st.copy(guardando = false, error = e.mensajeUi(), campos = e.camposUi()) } }
        }
    }

    fun cambiarClave(actual: String, nueva: String, confirmacion: String) {
        if (_estado.value.cambiandoClave) return
        viewModelScope.launch {
            _estado.update { it.copy(cambiandoClave = true, errorClave = null, camposClave = emptyMap(), claveCambiada = false) }
            changePassword(actual, nueva, confirmacion)
                .onSuccess { _estado.update { s -> s.copy(cambiandoClave = false, claveCambiada = true, mensaje = "Contraseña actualizada.") } }
                .onFailure { e -> _estado.update { s -> s.copy(cambiandoClave = false, errorClave = e.mensajeUi(), camposClave = e.camposUi()) } }
        }
    }

    fun consumirClaveCambiada() {
        _estado.update { it.copy(claveCambiada = false, errorClave = null, camposClave = emptyMap()) }
    }

    fun consumirMensaje() {
        _estado.update { it.copy(mensaje = null) }
    }

    fun cerrarSesion() {
        viewModelScope.launch {
            logout()
            limpiar()
        }
    }
}
