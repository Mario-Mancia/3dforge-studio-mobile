package com.example.a3df_studio_app.domain.usecases.feedback

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.FeedbackRepository
import com.example.a3df_studio_app.domain.models.SolicitudSoporte
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisSolicitudesSoporteUseCase(
    private val authRepository: AuthRepository,
    private val feedbackRepository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<SolicitudSoporte>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            feedbackRepository.obtenerMisSolicitudes()
        }
    }
}
