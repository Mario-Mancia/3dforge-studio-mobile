package com.example.a3df_studio_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.usecases.catalogo.GetCategoriasServiciosUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetNegocioInfoUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetPromocionesActivasUseCase
import com.example.a3df_studio_app.domain.usecases.catalogo.GetServiciosUseCase
import com.example.a3df_studio_app.ui.classes.HomeUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getNegocioInfoUseCase: GetNegocioInfoUseCase,
    private val getPromocionesActivasUseCase: GetPromocionesActivasUseCase,
    private val getCategoriasServiciosUseCase: GetCategoriasServiciosUseCase,
    private val getServiciosUseCase: GetServiciosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val negocio = async { getNegocioInfoUseCase() }
            val promociones = async { getPromocionesActivasUseCase() }
            val categorias = async { getCategoriasServiciosUseCase() }
            val servicios = async { getServiciosUseCase() }

            val resultadoServicios = servicios.await()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    negocio = negocio.await().getOrNull(),
                    promociones = promociones.await().getOrDefault(emptyList()),
                    categorias = categorias.await().getOrDefault(emptyList()),
                    servicios = resultadoServicios.getOrDefault(emptyList()),
                    error = resultadoServicios.exceptionOrNull()?.message
                )
            }
        }
    }

    fun seleccionarCategoria(id: Long?) {
        _uiState.update { it.copy(categoriaSeleccionada = if (it.categoriaSeleccionada == id) null else id) }
    }
}
