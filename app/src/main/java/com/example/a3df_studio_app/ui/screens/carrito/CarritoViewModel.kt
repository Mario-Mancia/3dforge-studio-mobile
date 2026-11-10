package com.example.a3df_studio_app.ui.screens.carrito

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.a3df_studio_app.domain.usecases.ClearCartUseCase
import com.example.a3df_studio_app.domain.usecases.ConfirmPurchaseUseCase
import com.example.a3df_studio_app.domain.usecases.ObserveCartUseCase
import com.example.a3df_studio_app.domain.usecases.RemoveFromCartUseCase
import com.example.a3df_studio_app.domain.usecases.UpdateCartQuantityUseCase
import com.example.a3df_studio_app.domain.usecases.ValidateCartUseCase
import com.example.a3df_studio_app.domain.utils.calcularTotales
import com.example.a3df_studio_app.ui.classes.CarritoUiState
import com.example.a3df_studio_app.ui.classes.mensajeUi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID

class CarritoViewModel(
    observeCart: ObserveCartUseCase,
    private val updateQuantity: UpdateCartQuantityUseCase,
    private val quitarProducto: RemoveFromCartUseCase,
    private val vaciarCarrito: ClearCartUseCase,
    private val validate: ValidateCartUseCase,
    private val confirm: ConfirmPurchaseUseCase
) : ViewModel() {

    private val _estado = MutableStateFlow(CarritoUiState())
    val estado: StateFlow<CarritoUiState> = _estado.asStateFlow()

    /** Identificador único de la compra en curso: un reintento por falla de red usa el MISMO token. */
    private var tokenCompra: String? = null

    init {
        viewModelScope.launch {
            observeCart().collect { items ->
                _estado.update { it.copy(items = items, totales = if (items.isEmpty()) null else calcularTotales(items)) }
            }
        }
    }

    private fun carritoCambio() {
        tokenCompra = null
        _estado.update { it.copy(validado = null, error = null) }
    }

    fun cambiarCantidad(productoId: Long, cantidad: Int) {
        viewModelScope.launch {
            carritoCambio()
            updateQuantity(productoId, cantidad).onFailure { e -> _estado.update { s -> s.copy(error = e.mensajeUi()) } }
        }
    }

    fun quitar(productoId: Long) {
        viewModelScope.launch {
            carritoCambio()
            quitarProducto(productoId)
        }
    }

    fun vaciar() {
        viewModelScope.launch {
            carritoCambio()
            vaciarCarrito()
        }
    }

    /** Valida con el servidor (requiere sesión) y muestra los totales reales antes de confirmar. */
    fun revisar() {
        val items = _estado.value.items
        if (items.isEmpty() || _estado.value.validando) return
        viewModelScope.launch {
            _estado.update { it.copy(validando = true, error = null) }
            validate(items)
                .onSuccess { validado -> _estado.update { s -> s.copy(validando = false, validado = validado) } }
                .onFailure { e -> _estado.update { s -> s.copy(validando = false, error = e.mensajeUi()) } }
        }
    }

    fun cerrarRevision() {
        _estado.update { it.copy(validado = null) }
    }

    fun confirmar() {
        val items = _estado.value.items
        if (items.isEmpty() || _estado.value.comprando) return
        val token = tokenCompra ?: UUID.randomUUID().toString().also { tokenCompra = it }
        viewModelScope.launch {
            _estado.update { it.copy(comprando = true, error = null) }
            confirm(items, token)
                .onSuccess { compra ->
                    tokenCompra = null
                    _estado.update { s -> s.copy(comprando = false, validado = null, pedidoCreado = compra.pedido.id) }
                }
                .onFailure { e -> _estado.update { s -> s.copy(comprando = false, validado = null, error = e.mensajeUi()) } }
        }
    }

    fun consumirPedido() {
        _estado.update { it.copy(pedidoCreado = null) }
    }
}
