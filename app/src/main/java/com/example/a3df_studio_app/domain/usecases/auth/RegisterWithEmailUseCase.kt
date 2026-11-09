package com.example.a3df_studio_app.domain.usecases.auth

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.data.repositories.PreferencesRepository
import com.example.a3df_studio_app.domain.models.SignUpResult
import com.example.a3df_studio_app.domain.models.ValidationException
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterWithEmailUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val preferencesRepository: PreferencesRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        nombres: String,
        apellidos: String,
        correo: String,
        clave: String,
        confirmacion: String
    ): Result<SignUpResult> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireNombre(nombres, "un nombre")
            InputValidator.requireNombre(apellidos, "un apellido")
            InputValidator.requireEmail(email)
            InputValidator.requirePassword(clave)
            if (clave != confirmacion) throw ValidationException("Las contraseñas no coinciden.")

            when (val resultado = authRepository.registrar(email, clave, nombres.trim(), apellidos.trim())) {
                is SignUpResult.VerificationRequired -> {
                    preferencesRepository.guardarCorreoPendiente(email)
                    resultado
                }
                is SignUpResult.SignedIn -> {
                    preferencesRepository.guardarUltimoCorreo(email)
                    clienteRepository.asegurarCliente(nombres, apellidos, null, null)
                    resultado
                }
            }
        }
    }
}
