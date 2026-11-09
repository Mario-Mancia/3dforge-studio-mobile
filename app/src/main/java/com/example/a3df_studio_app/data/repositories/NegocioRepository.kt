package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.data.remote.dto.ConfiguracionNegocioDto
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Tables
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toDomain
import com.example.a3df_studio_app.domain.models.AppException
import com.example.a3df_studio_app.domain.models.NegocioInfo
import com.example.a3df_studio_app.domain.utils.valorONull
import io.github.jan.supabase.postgrest.query.Order

class NegocioRepository {

    @Volatile
    private var cache: NegocioInfo? = null

    suspend fun obtenerNegocio(forzar: Boolean = false): NegocioInfo? {
        if (!forzar) cache?.let { return it }
        return supabaseCall {
            Database.postgrest.from(Tables.CONFIGURACION_NEGOCIO)
                .select {
                    order("id", Order.ASCENDING)
                    limit(1)
                }
                .decodeSingleOrNull<ConfiguracionNegocioDto>()
                ?.toDomain()
        }.also { cache = it }
    }

    suspend fun obtenerTelefonoWhatsApp(): String {
        val remoto = valorONull { obtenerNegocio()?.telefono }
        return listOfNotNull(remoto, AppConfig.whatsappFallbackNumber)
            .map { numero -> numero.filter { it.isDigit() } }
            .firstOrNull { it.length >= 8 }
            ?: throw AppException("No hay un número de WhatsApp configurado para el negocio.")
    }
}
