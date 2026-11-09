package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.dto.CotizacionCreadaDto
import com.example.a3df_studio_app.data.remote.dto.CotizacionDto
import com.example.a3df_studio_app.data.remote.dto.CotizacionItemParam
import com.example.a3df_studio_app.data.remote.dto.CrearCotizacionParams
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Rpc
import com.example.a3df_studio_app.data.remote.supabase.Tables
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toDomain
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.CotizacionCreada
import com.example.a3df_studio_app.domain.models.ItemCotizacion
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc

class CotizacionRepository {

    suspend fun crearCotizacion(items: List<ItemCotizacion>, observaciones: String?): CotizacionCreada = supabaseCall {
        val params = CrearCotizacionParams(
            items = items.map { CotizacionItemParam(it.servicio.id, it.cantidad, it.detalle) },
            observaciones = observaciones
        )
        Database.postgrest.rpc(Rpc.CREAR_COTIZACION, params)
            .decodeAs<CotizacionCreadaDto>()
            .toDomain()
    }

    suspend fun obtenerMisCotizaciones(): List<Cotizacion> = supabaseCall {
        Database.postgrest.from(Tables.COTIZACIONES)
            .select(Columns.raw("*, cotizaciones_detalles(*)")) {
                order("fecha_emision", Order.DESCENDING)
            }
            .decodeList<CotizacionDto>()
            .map { it.toDomain() }
    }
}
