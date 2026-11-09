package com.example.a3df_studio_app.domain.usecases.crm

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.CrmRepository
import com.example.a3df_studio_app.domain.models.TipoInteraccion
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RegisterCrmInteractionUseCase(
    private val authRepository: AuthRepository,
    private val crmRepository: CrmRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        tipo: TipoInteraccion,
        asunto: String,
        descripcion: String? = null
    ): Result<Long> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            InputValidator.requireLongitud(asunto, "El asunto", 3, 150)
            crmRepository.registrarInteraccion(tipo, asunto.trim(), descripcion?.trim()?.takeIf { it.isNotEmpty() })
        }
    }
}
