package com.example.a3df_studio_app.domain.usecases.auth

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ResendVerificationCodeUseCase(
    private val authRepository: AuthRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(correo: String): Result<Unit> = withContext(dispatcher) {
        resultOf {
            val email = correo.trim().lowercase()
            InputValidator.requireEmail(email)
            authRepository.reenviarCodigoRegistro(email)
        }
    }
}
