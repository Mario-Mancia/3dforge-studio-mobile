package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.dto.FacturaDto
import com.example.a3df_studio_app.data.remote.dto.PagoDto
import com.example.a3df_studio_app.data.remote.dto.PagoRegistradoDto
import com.example.a3df_studio_app.data.remote.dto.PagoSimuladoParams
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Rpc
import com.example.a3df_studio_app.data.remote.supabase.Tables
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toDomain
import com.example.a3df_studio_app.domain.models.EstadoFactura
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.Pago
import com.example.a3df_studio_app.domain.models.PagoRegistrado
import io.github.jan.supabase.postgrest.query.Columns
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.postgrest.rpc

class PagoRepository {

    suspend fun obtenerMisFacturas(soloPendientes: Boolean = false): List<Factura> = supabaseCall {
        val pendientes = EstadoFactura.entries.filter { it.pagable }.map { it.name }
        Database.postgrest.from(Tables.FACTURAS)
            .select(Columns.raw("*, facturas_detalles(*)")) {
                if (soloPendientes) filter { isIn("estado", pendientes) }
                order("fecha_emision", Order.DESCENDING)
            }
            .decodeList<FacturaDto>()
            .map { it.toDomain() }
    }

    suspend fun obtenerMisPagos(): List<Pago> = supabaseCall {
        Database.postgrest.from(Tables.PAGOS)
            .select(Columns.raw("*, metodos_pago(nombre)")) {
                order("fecha_pago", Order.DESCENDING)
            }
            .decodeList<PagoDto>()
            .map { it.toDomain() }
    }

    suspend fun registrarPagoSimulado(
        idFactura: Long,
        monto: Double,
        marca: String,
        ultimos4: String,
        titular: String?
    ): PagoRegistrado = supabaseCall {
        val params = PagoSimuladoParams(idFactura, monto, marca, ultimos4, titular)
        Database.postgrest.rpc(Rpc.REGISTRAR_PAGO_SIMULADO, params)
            .decodeAs<PagoRegistradoDto>()
            .toDomain()
    }
}
