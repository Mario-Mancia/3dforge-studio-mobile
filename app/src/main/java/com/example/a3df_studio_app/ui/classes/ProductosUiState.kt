package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.ItemPedido
import com.example.a3df_studio_app.domain.models.PedidoCreado
import com.example.a3df_studio_app.domain.models.Producto

data class ProductosUiState(
    val isLoading: Boolean = true,
    val error: String? = null,
    val busqueda: String = "",
    val productos: List<Producto> = emptyList(),
    val carrito: Map<Long, Int> = emptyMap(),
    val observaciones: String = "",
    val isOrdering: Boolean = false,
    val mensaje: String? = null,
    val pedidoCreado: PedidoCreado? = null
) {
    val itemsCarrito: List<ItemPedido>
        get() = carrito.mapNotNull { (id, cantidad) ->
            productos.firstOrNull { it.id == id }?.let { ItemPedido(it, cantidad) }
        }

    val totalCarrito: Double
        get() = itemsCarrito.sumOf { it.subtotal }

    val unidadesCarrito: Int
        get() = carrito.values.sum()
}
