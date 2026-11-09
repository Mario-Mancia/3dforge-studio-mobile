package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.dto.CrearPedidoParams
import com.example.a3df_studio_app.data.remote.dto.EncargoDto
import com.example.a3df_studio_app.data.remote.dto.PedidoCreadoDto
import com.example.a3df_studio_app.data.remote.dto.PedidoItemParam
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Rpc
import com.example.a3df_studio_app.data.remote.supabase.Tables
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toDomain
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.ItemPedido
import com.example.a3df_studio_app.domain.models.PedidoCreado
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc

class EncargoRepository {

    private val columnas = Columns.raw("*, encargos_detalles(*), historial_encargos(*)")

    suspend fun crearPedido(items: List<ItemPedido>, observaciones: String?): PedidoCreado = supabaseCall {
        val params = CrearPedidoParams(
            items = items.map { PedidoItemParam(it.producto.id, it.cantidad) },
            observaciones = observaciones
        )
        Database.postgrest.rpc(Rpc.CREAR_PEDIDO, params)
            .decodeAs<PedidoCreadoDto>()
            .toDomain()
    }

    suspend fun obtenerMisEncargos(): List<Encargo> = supabaseCall {
        Database.postgrest.from(Tables.ENCARGOS)
            .select(columnas) { order("fecha_solicitud", Order.DESCENDING) }
            .decodeList<EncargoDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerEncargo(id: Long): Encargo? = supabaseCall {
        Database.postgrest.from(Tables.ENCARGOS)
            .select(columnas) { filter { eq("id", id) } }
            .decodeSingleOrNull<EncargoDto>()
            ?.toDomain()
    }
}
