package com.example.a3df_studio_app.domain.usecases.catalogo

import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.data.repositories.NegocioRepository
import com.example.a3df_studio_app.domain.models.NegocioInfo
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetNegocioInfoUseCase(
    private val negocioRepository: NegocioRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(forzar: Boolean = false): Result<NegocioInfo> = withContext(dispatcher) {
        resultOf {
            negocioRepository.obtenerNegocio(forzar) ?: NegocioInfo(
                nombre = AppConfig.NOMBRE_NEGOCIO,
                correo = null,
                telefono = AppConfig.whatsappFallbackNumber.ifBlank { null },
                direccion = null,
                sitioWeb = null,
                logoUrl = null
            )
        }
    }
}
