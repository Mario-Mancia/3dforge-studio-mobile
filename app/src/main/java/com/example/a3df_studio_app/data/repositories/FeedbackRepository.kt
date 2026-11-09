package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.dto.ResenaDto
import com.example.a3df_studio_app.data.remote.dto.ResenaInsertDto
import com.example.a3df_studio_app.data.remote.dto.SolicitudSoporteDto
import com.example.a3df_studio_app.data.remote.dto.SolicitudSoporteInsertDto
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Tables
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toDomain
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.Resena
import com.example.a3df_studio_app.domain.models.SolicitudSoporte
import io.github.jan.supabase.postgrest.query.Order

class FeedbackRepository {

    suspend fun enviarResena(
        idCliente: Long,
        puntuacion: Int,
        comentario: String?,
        idServicio: Long?,
        idEncargo: Long?
    ): Resena = supabaseCall {
        val dto = ResenaInsertDto(idCliente, idServicio, idEncargo, puntuacion, comentario)
        Database.postgrest.from(Tables.RESENAS)
            .insert(dto) { select() }
            .decodeSingle<ResenaDto>()
            .toDomain()
    }

    suspend fun obtenerMisResenas(): List<Resena> = supabaseCall {
        Database.postgrest.from(Tables.RESENAS)
            .select { order("fecha_creacion", Order.DESCENDING) }
            .decodeList<ResenaDto>()
            .map { it.toDomain() }
    }

    suspend fun enviarSolicitudSoporte(
        idCliente: Long,
        asunto: String,
        descripcion: String,
        prioridad: PrioridadSoporte
    ): SolicitudSoporte = supabaseCall {
        val dto = SolicitudSoporteInsertDto(idCliente, asunto, descripcion, prioridad.name)
        Database.postgrest.from(Tables.SOLICITUDES_SOPORTE)
            .insert(dto) { select() }
            .decodeSingle<SolicitudSoporteDto>()
            .toDomain()
    }

    suspend fun obtenerMisSolicitudes(): List<SolicitudSoporte> = supabaseCall {
        Database.postgrest.from(Tables.SOLICITUDES_SOPORTE)
            .select { order("creado_en", Order.DESCENDING) }
            .decodeList<SolicitudSoporteDto>()
            .map { it.toDomain() }
    }
}
