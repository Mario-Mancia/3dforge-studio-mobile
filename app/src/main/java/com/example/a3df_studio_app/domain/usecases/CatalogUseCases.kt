package com.example.a3df_studio_app.domain.usecases

import com.example.a3df_studio_app.data.repositories.CatalogRepository
import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.models.InfoNegocio
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.utils.resultOf

class GetProductosUseCase(private val repositorio: CatalogRepository) {
    suspend operator fun invoke(busqueda: String?, pagina: Int): Result<Pagina<Producto>> =
        resultOf { repositorio.productos(busqueda, pagina) }
}

class GetProductoUseCase(private val repositorio: CatalogRepository) {
    suspend operator fun invoke(id: Long): Result<Producto> = resultOf { repositorio.producto(id) }
}

class GetServiciosUseCase(private val repositorio: CatalogRepository) {
    suspend operator fun invoke(categoriaId: Long? = null): Result<List<Servicio>> =
        resultOf { repositorio.servicios(categoriaId) }
}

class GetServicioUseCase(private val repositorio: CatalogRepository) {
    suspend operator fun invoke(id: Long): Result<Servicio> = resultOf { repositorio.servicio(id) }
}

class GetCategoriasUseCase(private val repositorio: CatalogRepository) {
    suspend operator fun invoke(): Result<List<CategoriaServicio>> = resultOf { repositorio.categorias() }
}

class GetNegocioUseCase(private val repositorio: CatalogRepository) {
    suspend operator fun invoke(): Result<InfoNegocio> = resultOf { repositorio.negocio() }
}
