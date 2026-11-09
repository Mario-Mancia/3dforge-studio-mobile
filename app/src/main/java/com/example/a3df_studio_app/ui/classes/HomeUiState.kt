package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.models.NegocioInfo
import com.example.a3df_studio_app.domain.models.Promocion
import com.example.a3df_studio_app.domain.models.Servicio

data class HomeUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val negocio: NegocioInfo? = null,
    val promociones: List<Promocion> = emptyList(),
    val categorias: List<CategoriaServicio> = emptyList(),
    val servicios: List<Servicio> = emptyList(),
    val categoriaSeleccionada: Long? = null
) {
    val serviciosFiltrados: List<Servicio>
        get() = categoriaSeleccionada?.let { id -> servicios.filter { it.idCategoria == id } } ?: servicios
}
