package com.example.a3df_studio_app.domain.usecases.feedback

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.data.repositories.FeedbackRepository
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.SolicitudSoporte
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubmitSupportRequestUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val feedbackRepository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        asunto: String,
        descripcion: String,
        prioridad: PrioridadSoporte = PrioridadSoporte.MEDIA
    ): Result<SolicitudSoporte> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            InputValidator.requireLongitud(asunto, "El asunto", 3, 150)
            InputValidator.requireLongitud(descripcion, "La descripción", 10, 2000)
            val idCliente = clienteRepository.requireClienteId()
            feedbackRepository.enviarSolicitudSoporte(idCliente, asunto.trim(), descripcion.trim(), prioridad)
        }
    }
}
