package com.example.a3df_studio_app.ui.screens.productos

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.usecases.catalogo.GetProductosUseCase
import com.example.a3df_studio_app.domain.usecases.encargos.CreateProductOrderUseCase
import com.example.a3df_studio_app.ui.classes.ProductosUiState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class ProductosViewModel(
    private val getProductosUseCase: GetProductosUseCase,
    private val createProductOrderUseCase: CreateProductOrderUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProductosUiState())
    val uiState: StateFlow<ProductosUiState> = _uiState.asStateFlow()
    private var busquedaJob: Job? = null

    init {
        cargar()
    }

    fun cargar() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getProductosUseCase(_uiState.value.busqueda).fold(
                onSuccess = { lista -> _uiState.update { it.copy(isLoading = false, productos = lista) } },
                onFailure = { e -> _uiState.update { it.copy(isLoading = false, error = e.message) } }
            )
        }
    }

    fun onBusqueda(texto: String) {
        _uiState.update { it.copy(busqueda = texto) }
        busquedaJob?.cancel()
        busquedaJob = viewModelScope.launch {
            delay(400)
            cargar()
        }
    }

    fun agregar(idProducto: Long) {
        _uiState.update { estado ->
            val stock = estado.productos.firstOrNull { it.id == idProducto }?.stock ?: 0
            val actual = estado.carrito[idProducto] ?: 0
            if (actual >= stock) estado else estado.copy(carrito = estado.carrito + (idProducto to actual + 1))
        }
    }

    fun quitar(idProducto: Long) {
        _uiState.update { estado ->
            val actual = estado.carrito[idProducto] ?: return@update estado
            val nuevo = if (actual <= 1) estado.carrito - idProducto else estado.carrito + (idProducto to actual - 1)
            estado.copy(carrito = nuevo)
        }
    }

    fun onObservaciones(texto: String) {
        _uiState.update { it.copy(observaciones = texto) }
    }

    fun realizarPedido() {
        val estado = _uiState.value
        if (estado.isOrdering) return
        viewModelScope.launch {
            _uiState.update { it.copy(isOrdering = true, mensaje = null) }
            createProductOrderUseCase(estado.itemsCarrito, estado.observaciones).fold(
                onSuccess = { pedido ->
                    _uiState.update {
                        it.copy(isOrdering = false, carrito = emptyMap(), observaciones = "", pedidoCreado = pedido)
                    }
                    cargar()
                },
                onFailure = { e -> _uiState.update { it.copy(isOrdering = false, mensaje = e.message) } }
            )
        }
    }

    fun consumirMensaje() {
        _uiState.update { it.copy(mensaje = null) }
    }

    fun cerrarPedidoCreado() {
        _uiState.update { it.copy(pedidoCreado = null) }
    }
}
