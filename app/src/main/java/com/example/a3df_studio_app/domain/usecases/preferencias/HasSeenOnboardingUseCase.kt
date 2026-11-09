package com.example.a3df_studio_app.domain.usecases.preferencias

import com.example.a3df_studio_app.data.repositories.PreferencesRepository
import kotlinx.coroutines.flow.Flow

class HasSeenOnboardingUseCase(private val preferencesRepository: PreferencesRepository) {
    operator fun invoke(): Flow<Boolean> = preferencesRepository.onboardingVisto
}
