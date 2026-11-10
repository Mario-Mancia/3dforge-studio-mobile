package com.example.a3df_studio_app.ui.screens.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.usecases.GetNegocioUseCase
import com.example.a3df_studio_app.domain.usecases.GetPanelUseCase
import com.example.a3df_studio_app.domain.usecases.GetProductosUseCase
import com.example.a3df_studio_app.domain.usecases.GetServiciosUseCase
import com.example.a3df_studio_app.ui.classes.HomeUiState
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val getNegocio: GetNegocioUseCase,
    private val getProductos: GetProductosUseCase,
    private val getServicios: GetServiciosUseCase,
    private val getPanel: GetPanelUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(HomeUiState())
    val estado: StateFlow<HomeUiState> = _estado.asStateFlow()

    fun cargar(conSesion: Boolean) {
        viewModelScope.launch {
            _estado.update { it.copy(cargando = true, error = null) }
            coroutineScope {
                val negocioD = async { getNegocio() }
                val productosD = async { getProductos(null, 1) }
                val serviciosD = async { getServicios(null) }
                val panelD = if (conSesion) async { getPanel() } else null
                val negocio = negocioD.await()
                val productos = productosD.await()
                val servicios = serviciosD.await()
                val panel = panelD?.await()?.getOrNull()
                if (negocio.isFailure && productos.isFailure && servicios.isFailure) {
                    _estado.update { it.copy(cargando = false, error = productos.exceptionOrNull()?.mensajeUi()) }
                } else {
                    _estado.update {
                        it.copy(
                            cargando = false,
                            negocio = negocio.getOrNull() ?: it.negocio,
                            destacados = productos.getOrNull()?.items.orEmpty().take(6),
                            servicios = servicios.getOrNull().orEmpty().take(6),
                            panel = panel
                        )
                    }
                }
            }
        }
    }
}
