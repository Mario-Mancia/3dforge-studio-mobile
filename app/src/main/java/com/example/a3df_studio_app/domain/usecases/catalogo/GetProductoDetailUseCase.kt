package com.example.a3df_studio_app.domain.usecases.catalogo

import com.example.a3df_studio_app.data.repositories.CatalogRepository
import com.example.a3df_studio_app.domain.models.AppException
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetProductoDetailUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(id: Long): Result<Producto> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerProducto(id) ?: throw AppException("El producto no está disponible.") }
    }
}
