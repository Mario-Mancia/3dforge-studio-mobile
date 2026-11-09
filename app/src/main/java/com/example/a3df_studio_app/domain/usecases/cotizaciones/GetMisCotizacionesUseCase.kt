package com.example.a3df_studio_app.domain.usecases.cotizaciones

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.CotizacionRepository
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetMisCotizacionesUseCase(
    private val authRepository: AuthRepository,
    private val cotizacionRepository: CotizacionRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<Cotizacion>> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            cotizacionRepository.obtenerMisCotizaciones()
        }
    }
}
