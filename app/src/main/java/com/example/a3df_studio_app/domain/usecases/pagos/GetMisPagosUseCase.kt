package com.example.a3df_studio_app.domain.usecases.pagos

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.PagoRepository
import com.example.a3df_studio_app.domain.models.Pago
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisPagosUseCase(
    private val authRepository: AuthRepository,
    private val pagoRepository: PagoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<Pago>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            pagoRepository.obtenerMisPagos()
        }
    }
}
