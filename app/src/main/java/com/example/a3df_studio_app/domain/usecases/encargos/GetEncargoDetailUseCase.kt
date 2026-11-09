package com.example.a3df_studio_app.domain.usecases.encargos

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.EncargoRepository
import com.example.a3df_studio_app.domain.models.AppException
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetEncargoDetailUseCase(
    private val authRepository: AuthRepository,
    private val encargoRepository: EncargoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(id: Long): Result<Encargo> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            encargoRepository.obtenerEncargo(id) ?: throw AppException("No se encontró el encargo.")
        }
    }
}
