package com.example.a3df_studio_app.ui.screens.pedidos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.usecases.encargos.GetMisEncargosUseCase
import com.example.a3df_studio_app.domain.usecases.pagos.GetMisFacturasUseCase
import com.example.a3df_studio_app.domain.usecases.pagos.GetMisPagosUseCase
import com.example.a3df_studio_app.ui.classes.PedidosUiState
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PedidosViewModel(
    private val getMisEncargosUseCase: GetMisEncargosUseCase,
    private val getMisFacturasUseCase: GetMisFacturasUseCase,
    private val getMisPagosUseCase: GetMisPagosUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(PedidosUiState())
    val uiState: StateFlow<PedidosUiState> = _uiState.asStateFlow()

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val encargos = async { getMisEncargosUseCase() }
            val facturas = async { getMisFacturasUseCase() }
            val pagos = async { getMisPagosUseCase() }
            val resultadoEncargos = encargos.await()
            _uiState.update {
                it.copy(
                    isLoading = false,
                    cargado = true,
                    encargos = resultadoEncargos.getOrDefault(emptyList()),
                    facturas = facturas.await().getOrDefault(emptyList()),
                    pagos = pagos.await().getOrDefault(emptyList()),
                    error = resultadoEncargos.exceptionOrNull()?.message
                )
            }
        }
    }

    fun factura(id: Long): Factura? = _uiState.value.facturas.firstOrNull { it.id == id }

    fun limpiar() {
        _uiState.value = PedidosUiState()
    }
}
