package com.example.a3df_studio_app.domain.usecases.auth

import com.example.a3df_studio_app.data.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class GetLastEmailUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(): Flow<String?> = preferencesRepository.ultimoCorreo
}
