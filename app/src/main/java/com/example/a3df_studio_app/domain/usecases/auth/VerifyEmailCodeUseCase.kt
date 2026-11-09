package com.example.a3df_studio_app.domain.usecases.auth

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.data.repositories.PreferencesRepository
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VerifyEmailCodeUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(correo: String, codigo: String): Result<AuthUser> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireEmail(email)
            InputValidator.requireOtp(codigo)
            val usuario = authRepository.verificarCodigoRegistro(email, codigo.trim())
            preferencesRepository.guardarCorreoPendiente(null)
            preferencesRepository.guardarUltimoCorreo(email)
            clienteRepository.asegurarCliente(usuario.nombres, usuario.apellidos, null, usuario.avatarUrl)
            usuario
        }
    }
}
