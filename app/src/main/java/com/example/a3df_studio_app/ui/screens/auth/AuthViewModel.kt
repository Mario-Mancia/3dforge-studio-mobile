package com.example.a3df_studio_app.ui.screens.auth

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.EmailNotVerifiedException
import com.example.a3df_studio_app.domain.models.GoogleSignInCancelledException
import com.example.a3df_studio_app.domain.models.SignUpResult
import com.example.a3df_studio_app.domain.usecases.auth.GetLastEmailUseCase
import com.example.a3df_studio_app.domain.usecases.auth.LoginWithEmailUseCase
import com.example.a3df_studio_app.domain.usecases.auth.RegisterWithEmailUseCase
import com.example.a3df_studio_app.domain.usecases.auth.ResendVerificationCodeUseCase
import com.example.a3df_studio_app.domain.usecases.auth.SendPasswordResetUseCase
import com.example.a3df_studio_app.domain.usecases.auth.SignInWithGoogleUseCase
import com.example.a3df_studio_app.domain.usecases.auth.VerifyEmailCodeUseCase
import com.example.a3df_studio_app.ui.classes.AuthUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel(
    private val loginWithEmailUseCase: LoginWithEmailUseCase,
    private val registerWithEmailUseCase: RegisterWithEmailUseCase,
    private val verifyEmailCodeUseCase: VerifyEmailCodeUseCase,
    private val resendVerificationCodeUseCase: ResendVerificationCodeUseCase,
    private val signInWithGoogleUseCase: SignInWithGoogleUseCase,
    private val sendPasswordResetUseCase: SendPasswordResetUseCase,
    getLastEmailUseCase: GetLastEmailUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()
    val ultimoCorreo: Flow<String?> = getLastEmailUseCase()

    private fun ejecutar(bloque: suspend () -> Unit) {
        if (_uiState.value.isLoading) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, mensaje = null) }
            bloque()
        }
    }

    private fun fallo(e: Throwable) {
        when (e) {
            is EmailNotVerifiedException ->
                _uiState.update { it.copy(isLoading = false, correoVerificacion = e.correo, mensaje = e.message) }
            is GoogleSignInCancelledException -> _uiState.update { it.copy(isLoading = false) }
            else -> _uiState.update { it.copy(isLoading = false, error = e.message) }
        }
    }

    fun login(correo: String, clave: String) = ejecutar {
        loginWithEmailUseCase(correo, clave).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, autenticado = true) } },
            onFailure = ::fallo
        )
    }

    fun loginGoogle(activityContext: Context) = ejecutar {
        signInWithGoogleUseCase(activityContext).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, autenticado = true) } },
            onFailure = ::fallo
        )
    }

    fun registrar(nombres: String, apellidos: String, correo: String, clave: String, confirmacion: String) = ejecutar {
        registerWithEmailUseCase(nombres, apellidos, correo, clave, confirmacion).fold(
            onSuccess = { resultado ->
                when (resultado) {
                    is SignUpResult.VerificationRequired -> _uiState.update {
                        it.copy(isLoading = false, correoVerificacion = resultado.correo)
                    }
                    is SignUpResult.SignedIn -> _uiState.update { it.copy(isLoading = false, autenticado = true) }
                }
            },
            onFailure = ::fallo
        )
    }

    fun verificar(correo: String, codigo: String) = ejecutar {
        verifyEmailCodeUseCase(correo, codigo).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, autenticado = true) } },
            onFailure = ::fallo
        )
    }

    fun reenviarCodigo(correo: String) = ejecutar {
        resendVerificationCodeUseCase(correo).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, mensaje = "Te enviamos un nuevo código.") } },
            onFailure = ::fallo
        )
    }

    fun recuperarClave(correo: String) = ejecutar {
        sendPasswordResetUseCase(correo).fold(
            onSuccess = { _uiState.update { it.copy(isLoading = false, mensaje = "Revisa tu correo para restablecer la contraseña.") } },
            onFailure = ::fallo
        )
    }

    fun consumirNavegacion() {
        _uiState.update { it.copy(correoVerificacion = null, autenticado = false) }
    }

    fun limpiarMensajes() {
        _uiState.update { it.copy(error = null, mensaje = null) }
    }
}
