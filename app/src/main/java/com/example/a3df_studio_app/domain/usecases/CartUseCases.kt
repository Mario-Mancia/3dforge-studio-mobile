package com.example.a3df_studio_app.domain.usecases

import com.example.a3df_studio_app.data.repositories.CartRepository
import com.example.a3df_studio_app.data.repositories.PurchaseRepository
import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.CarritoValidado
import com.example.a3df_studio_app.domain.models.CompraConfirmada
import com.example.a3df_studio_app.domain.models.Disponibilidad
import com.example.a3df_studio_app.domain.models.ItemCarrito
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.utils.aDinero
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.flow.Flow

class ObserveCartUseCase(private val repositorio: CartRepository) {
    operator fun invoke(): Flow<List<ItemCarrito>> = repositorio.items
}

class AddToCartUseCase(private val repositorio: CartRepository) {
    suspend operator fun invoke(producto: Producto, cantidad: Int = 1): Result<Unit> = resultOf {
        if (cantidad < 1) throw AppError.Local("La cantidad debe ser al menos 1.")
        repositorio.agregar(producto, cantidad)
    }
}

class UpdateCartQuantityUseCase(private val repositorio: CartRepository) {
    suspend operator fun invoke(productoId: Long, cantidad: Int): Result<Unit> =
        resultOf { repositorio.fijarCantidad(productoId, cantidad) }
}

class RemoveFromCartUseCase(private val repositorio: CartRepository) {
    suspend operator fun invoke(productoId: Long): Result<Unit> = resultOf { repositorio.quitar(productoId) }
}

class ClearCartUseCase(private val repositorio: CartRepository) {
    suspend operator fun invoke(): Result<Unit> = resultOf { repositorio.vaciar() }
}

/** Valida el carrito con el servidor (precios y existencias reales). Requiere sesión. */
class ValidateCartUseCase(
    private val compras: PurchaseRepository,
    private val carrito: CartRepository
) {
    suspend operator fun invoke(items: List<ItemCarrito>): Result<CarritoValidado> = resultOf {
        if (items.isEmpty()) throw AppError.Local("Tu carrito está vacío.")
        val validado = compras.validar(items)
        carrito.sincronizar(
            validado.lineas.mapNotNull { linea ->
                linea.precio?.let { precio ->
                    Producto(
                        id = linea.productoId,
                        sku = "",
                        nombre = linea.nombre,
                        descripcion = "",
                        precio = precio,
                        stock = linea.stock,
                        stockMinimo = 0,
                        disponibilidad = Disponibilidad.DISPONIBLE,
                        imagenUrl = items.firstOrNull { it.productoId == linea.productoId }?.imagenUrl
                    )
                }
            }
        )
        validado
    }
}

/** Confirma la compra con un token único (idempotente) y vacía el carrito. Requiere sesión. */
class ConfirmPurchaseUseCase(
    private val compras: PurchaseRepository,
    private val carrito: CartRepository
) {
    suspend operator fun invoke(items: List<ItemCarrito>, token: String): Result<CompraConfirmada> = resultOf {
        if (items.isEmpty()) throw AppError.Local("Tu carrito está vacío.")
        val resultado = compras.comprar(items, token)
        carrito.vaciar()
        resultado
    }
}
