package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.data.local.AppPreferences
import com.example.a3df_studio_app.data.local.ItemCarritoLocal
import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.ItemCarrito
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.utils.aDinero
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/** Carrito local (en el dispositivo): el servidor no guarda carritos. */
class CartRepository(private val preferencias: AppPreferences) {

    val items: Flow<List<ItemCarrito>> = preferencias.carrito.map { lista -> lista.map { it.aDominio() } }

    private fun ItemCarritoLocal.aDominio(): ItemCarrito = ItemCarrito(
        productoId = productoId,
        nombre = nombre,
        precio = precio.aDinero(),
        imagenUrl = imagenUrl,
        stock = stock,
        cantidad = cantidad
    )

    private suspend fun actuales(): MutableList<ItemCarritoLocal> = preferencias.carrito.first().toMutableList()

    suspend fun agregar(producto: Producto, cantidad: Int) {
        if (producto.agotado || producto.stock <= 0) throw AppError.Local("${producto.nombre} está agotado.")
        val lista = actuales()
        val indice = lista.indexOfFirst { it.productoId == producto.id }
        if (indice < 0 && lista.size >= AppConfig.MAX_PRODUCTOS_DISTINTOS) {
            throw AppError.Local("El carrito alcanzó el máximo de ${AppConfig.MAX_PRODUCTOS_DISTINTOS} productos distintos.")
        }
        val previa = if (indice >= 0) lista[indice].cantidad else 0
        val nueva = previa + cantidad
        val tope = minOf(producto.stock, AppConfig.LIMITE_POR_PRODUCTO)
        if (nueva > tope) {
            throw AppError.Local("Solo hay $tope unidades disponibles de ${producto.nombre}.")
        }
        val item = ItemCarritoLocal(
            productoId = producto.id,
            nombre = producto.nombre,
            precio = producto.precio.toPlainString(),
            imagenUrl = producto.imagenUrl,
            stock = producto.stock,
            cantidad = nueva
        )
        if (indice >= 0) lista[indice] = item else lista.add(item)
        preferencias.guardarCarrito(lista)
    }

    suspend fun fijarCantidad(productoId: Long, cantidad: Int) {
        val lista = actuales()
        val indice = lista.indexOfFirst { it.productoId == productoId }
        if (indice < 0) return
        if (cantidad <= 0) {
            lista.removeAt(indice)
        } else {
            val actual = lista[indice]
            val tope = minOf(actual.stock, AppConfig.LIMITE_POR_PRODUCTO)
            if (cantidad > tope) throw AppError.Local("Solo hay $tope unidades disponibles de ${actual.nombre}.")
            lista[indice] = actual.copy(cantidad = cantidad)
        }
        preferencias.guardarCarrito(lista)
    }

    suspend fun quitar(productoId: Long) {
        preferencias.guardarCarrito(actuales().filterNot { it.productoId == productoId })
    }

    suspend fun vaciar() {
        preferencias.guardarCarrito(emptyList())
    }

    /** Actualiza precio y existencias con lo que respondió el servidor al validar. */
    suspend fun sincronizar(productos: List<Producto>) {
        val porId = productos.associateBy { it.id }
        val lista = actuales().map { item ->
            val producto = porId[item.productoId]
            if (producto == null) item else item.copy(
                nombre = producto.nombre,
                precio = producto.precio.toPlainString(),
                imagenUrl = producto.imagenUrl,
                stock = producto.stock
            )
        }
        preferencias.guardarCarrito(lista)
    }
}
