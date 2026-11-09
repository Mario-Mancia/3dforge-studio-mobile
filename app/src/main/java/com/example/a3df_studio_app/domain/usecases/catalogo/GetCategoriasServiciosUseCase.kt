package com.example.a3df_studio_app.domain.usecases.catalogo

import com.example.a3df_studio_app.data.repositories.CatalogRepository
import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCategoriasServiciosUseCase(
    private val catalogRepository: CatalogRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(): Result<List<CategoriaServicio>> = withContext(dispatcher) {
        resultOf { catalogRepository.obtenerCategorias() }
    }
}
