package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.dto.CarritoRequest
import com.example.a3df_studio_app.data.remote.dto.CarritoValidadoDto
import com.example.a3df_studio_app.data.remote.dto.CompraDto
import com.example.a3df_studio_app.data.remote.dto.CompraRequest
import com.example.a3df_studio_app.data.remote.dto.ItemRequest
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.models.CarritoValidado
import com.example.a3df_studio_app.domain.models.CompraConfirmada
import com.example.a3df_studio_app.domain.models.ItemCarrito

class PurchaseRepository(private val api: ApiClient) {

    private fun pedidos(items: List<ItemCarrito>): List<ItemRequest> =
        items.map { ItemRequest(productoId = it.productoId, cantidad = it.cantidad) }

    suspend fun validar(items: List<ItemCarrito>): CarritoValidado {
        val dto: CarritoValidadoDto = api.post("carrito/validar/", CarritoRequest(pedidos(items)))
        return dto.aDominio()
    }

    /** [token] debe ser único por compra: repetirlo es seguro (no duplica el pedido). */
    suspend fun comprar(items: List<ItemCarrito>, token: String): CompraConfirmada {
        val dto: CompraDto = api.post("compras/", CompraRequest(pedidos(items), token))
        return CompraConfirmada(pedido = dto.pedido.aDominio(), duplicado = dto.duplicado)
    }
}
