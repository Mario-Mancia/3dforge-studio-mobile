package com.example.a3df_studio_app.domain.usecases.preferencias

import com.example.a3df_studio_app.data.repositories.PreferencesRepository

class MarkOnboardingSeenUseCase(private val preferencesRepository: PreferencesRepository) {
    suspend operator fun invoke() = preferencesRepository.marcarOnboardingVisto()
}
