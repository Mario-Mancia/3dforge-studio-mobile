package com.example.a3df_studio_app.domain.usecases.encargos

import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.EncargoRepository
import com.example.a3df_studio_app.domain.models.ItemPedido
import com.example.a3df_studio_app.domain.models.PedidoCreado
import com.example.a3df_studio_app.domain.models.ValidationException
import com.example.a3df_studio_app.domain.utils.resultOf
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CreateProductOrderUseCase(
    private val authRepository: AuthRepository,
    private val encargoRepository: EncargoRepository,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        items: List<ItemPedido>,
        observaciones: String? = null
    ): Result<PedidoCreado> = withContext(dispatcher) {
        resultOf {
            authRepository.requireUser()
            if (items.isEmpty()) throw ValidationException("Agrega al menos un producto al pedido.")
            items.forEach { item ->
                if (item.cantidad <= 0) throw ValidationException("Las cantidades deben ser mayores a cero.")
                if (item.cantidad > item.producto.stock) {
                    throw ValidationException("Solo hay ${item.producto.stock} unidades de ${item.producto.nombre}.")
                }
            }
            val agrupados = items.groupBy { it.producto.id }
                .map { (_, lista) -> ItemPedido(lista.first().producto, lista.sumOf { it.cantidad }) }
            encargoRepository.crearPedido(agrupados, observaciones?.trim()?.takeIf { it.isNotEmpty() })
        }
    }
}
