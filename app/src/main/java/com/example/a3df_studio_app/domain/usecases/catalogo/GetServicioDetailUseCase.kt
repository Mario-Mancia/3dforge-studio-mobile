package com.example.a3df_studio_app.domain.usecases.catalogo

import com.example.a3df_studio_app.data.repositories.CatalogRepository
import com.example.a3df_studio_app.domain.models.AppException
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetServicioDetailUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(id: Long): Result<Servicio> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerServicio(id) ?: throw AppException("El servicio no está disponible.") }
    }
}
