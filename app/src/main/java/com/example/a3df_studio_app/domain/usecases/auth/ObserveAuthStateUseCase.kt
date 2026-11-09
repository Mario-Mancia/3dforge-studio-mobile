package com.example.a3df_studio_app.domain.usecases.auth

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.domain.models.AuthState
import kotlinx.coroutines.flow.Flow

class ObserveAuthStateUseCase(private val authRepository: AuthRepository) {
    operator fun invoke(): Flow<AuthState> = authRepository.authState
}
