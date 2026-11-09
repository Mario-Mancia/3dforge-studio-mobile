package com.example.a3df_studio_app.domain.usecases.auth

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.domain.models.AuthUser

class GetCurrentUserUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): AuthUser? = authRepository.usuarioActual()
}
