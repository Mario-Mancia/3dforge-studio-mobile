package com.example.a3df_studio_app.domain.usecases.feedback

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.data.repositories.FeedbackRepository
import com.example.a3df_studio_app.domain.models.Resena
import com.example.a3df_studio_app.domain.models.ValidationException
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubmitResenaUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val feedbackRepository: FeedbackRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        puntuacion: Int,
        comentario: String?,
        idServicio: Long? = null,
        idEncargo: Long? = null
    ): Result<Resena> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            if (puntuacion !in 1..5) throw ValidationException("Selecciona una calificación de 1 a 5 estrellas.")
            if (idServicio == null && idEncargo == null) {
                throw ValidationException("Selecciona el servicio o encargo que deseas calificar.")
            }
            val texto = comentario?.trim()?.takeIf { it.isNotEmpty() }
            if (texto != null && texto.length > 1000) throw ValidationException("El comentario es demasiado largo.")
            val idCliente = clienteRepository.requireClienteId()
            feedbackRepository.enviarResena(idCliente, puntuacion, texto, idServicio, idEncargo)
        }
    }
}
