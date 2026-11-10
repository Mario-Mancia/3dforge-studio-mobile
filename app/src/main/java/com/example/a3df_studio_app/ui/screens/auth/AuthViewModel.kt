package com.example.a3df_studio_app.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.SolicitudRegistro
import com.example.a3df_studio_app.domain.usecases.GetLastEmailUseCase
import com.example.a3df_studio_app.domain.usecases.LoginUseCase
import com.example.a3df_studio_app.domain.usecases.LoginWithGoogleUseCase
import com.example.a3df_studio_app.domain.usecases.RecoverPasswordUseCase
import com.example.a3df_studio_app.domain.usecases.RegisterUseCase
import com.example.a3df_studio_app.domain.usecases.ResendConfirmationUseCase
import com.example.a3df_studio_app.ui.classes.AuthUiState
import com.example.a3df_studio_app.ui.classes.camposUi
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val login: LoginUseCase,
    private val loginGoogle: LoginWithGoogleUseCase,
    private val registrar: RegisterUseCase,
    private val reenviar: ResendConfirmationUseCase,
    private val recuperar: RecoverPasswordUseCase,
    private val ultimoCorreo: GetLastEmailUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(AuthUiState())
    val estado: StateFlow<AuthUiState> = _estado.asStateFlow()

    init {
        viewModelScope.launch {
            val correo = ultimoCorreo()
            _estado.update { it.copy(ultimoCorreo = correo) }
        }
    }

    fun limpiar() {
        _estado.update { it.copy(cargando = false, cargandoGoogle = false, error = null, campos = emptyMap(), mensaje = null, correoPendiente = null, exito = false) }
    }

    fun consumirExito() {
        _estado.update { it.copy(exito = false) }
    }

    fun iniciarSesion(correo: String, clave: String) {
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null, campos = emptyMap(), mensaje = null, correoPendiente = null) }
            login(correo, clave)
                .onSuccess { _estado.update { s -> s.copy(cargando = false, exito = true) } }
                .onFailure { e ->
                    _estado.update { s ->
                        s.copy(
                            cargando = false,
                            error = e.mensajeUi(),
                            campos = e.camposUi(),
                            correoPendiente = if (e is AppError.CorreoNoConfirmado) correo.trim() else null
                        )
                    }
                }
        }
    }

    fun continuarConGoogle(contexto: Context) {
        if (_estado.value.cargandoGoogle) return
        viewModelScope.launch {
            _estado.update { it.copy(cargandoGoogle = true, error = null, campos = emptyMap(), mensaje = null) }
            loginGoogle(contexto)
                .onSuccess { _estado.update { s -> s.copy(cargandoGoogle = false, exito = true) } }
                .onFailure { e -> _estado.update { s -> s.copy(cargandoGoogle = false, error = e.mensajeUi()) } }
        }
    }

    fun registrarse(datos: SolicitudRegistro, confirmacion: String, acepta: Boolean) {
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null, campos = emptyMap(), mensaje = null, correoPendiente = null) }
            registrar(datos, confirmacion, acepta)
                .onSuccess { mensaje ->
                    _estado.update { s -> s.copy(cargando = false, mensaje = mensaje, correoPendiente = datos.correo.trim().lowercase()) }
                }
                .onFailure { e -> _estado.update { s -> s.copy(cargando = false, error = e.mensajeUi(), campos = e.camposUi()) } }
        }
    }

    fun reenviarConfirmacion(correo: String) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null, mensaje = null) }
            reenviar(correo)
                .onSuccess { mensaje -> _estado.update { s -> s.copy(cargando = false, mensaje = mensaje) } }
                .onFailure { e -> _estado.update { s -> s.copy(cargando = false, error = e.mensajeUi()) } }
        }
    }

    fun recuperarContrasena(correo: String) {
        if (_estado.value.cargando) return
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null, mensaje = null) }
            recuperar(correo)
                .onSuccess { mensaje -> _estado.update { s -> s.copy(cargando = false, mensaje = mensaje) } }
                .onFailure { e -> _estado.update { s -> s.copy(cargando = false, error = e.mensajeUi()) } }
        }
    }
}
