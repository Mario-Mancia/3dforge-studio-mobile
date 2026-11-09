package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.dto.InteraccionParams
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Rpc
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.models.TipoInteraccion
import io.github.jan.supabase.postgrest.rpc

class CrmRepository {

    suspend fun registrarInteraccion(
        tipo: TipoInteraccion,
        asunto: String,
        descripcion: String?
    ): Long = supabaseCall {
        val params = InteraccionParams(tipo.name, asunto.take(150), descripcion)
        Database.postgrest.rpc(Rpc.REGISTRAR_INTERACCION, params).decodeAs<Long>()
    }
}
