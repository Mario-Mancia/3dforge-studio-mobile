package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.dto.AsegurarClienteParams
import com.example.a3df_studio_app.data.remote.dto.ClienteDto
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.Rpc
import com.example.a3df_studio_app.data.remote.supabase.Tables
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toDomain
import com.example.a3df_studio_app.domain.models.AuthRequiredException
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.ClienteUpdate
import io.github.jan.supabase.postgrest.rpc
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.time.Instant

class ClienteRepository {

    private val _clienteActual = MutableStateFlow<Cliente?>(null)
    val clienteActual: StateFlow<Cliente?> = _clienteActual.asStateFlow()

    suspend fun asegurarCliente(
        nombres: String?,
        apellidos: String?,
        telefono: String?,
        fotoUrl: String?
    ): Cliente = supabaseCall {
        val params = AsegurarClienteParams(
            nombres = nombres?.trim()?.takeIf { it.isNotEmpty() },
            apellidos = apellidos?.trim()?.takeIf { it.isNotEmpty() },
            telefono = telefono?.trim()?.takeIf { it.isNotEmpty() },
            fotoUrl = fotoUrl?.takeIf { it.isNotBlank() }
        )
        Database.postgrest.rpc(Rpc.ASEGURAR_CLIENTE, params)
            .decodeAs<ClienteDto>()
            .toDomain()
            .also { _clienteActual.value = it }
    }

    suspend fun obtenerMiCliente(): Cliente? = supabaseCall {
        val uid = Database.auth.currentUserOrNull()?.id ?: throw AuthRequiredException()
        Database.postgrest.from(Tables.CLIENTES)
            .select { filter { eq("auth_uid", uid) } }
            .decodeSingleOrNull<ClienteDto>()
            ?.toDomain()
            ?.also { _clienteActual.value = it }
    }

    suspend fun requireClienteId(): Long =
        _clienteActual.value?.id
            ?: obtenerMiCliente()?.id
            ?: asegurarCliente(null, null, null, null).id

    suspend fun actualizar(idCliente: Long, cambios: ClienteUpdate): Cliente = supabaseCall {
        val cuerpo = buildJsonObject {
            put("tipo_cliente", cambios.tipo.name)
            put("nombres", cambios.nombres.limpio())
            put("apellidos", cambios.apellidos.limpio())
            put("nombre_empresa", cambios.nombreEmpresa.limpio())
            put("telefono", cambios.telefono.limpio())
            put("identificacion_fiscal", cambios.identificacionFiscal.limpio())
            put("direccion", cambios.direccion.limpio())
            put("actualizado_en", Instant.now().toString())
        }
        Database.postgrest.from(Tables.CLIENTES)
            .update(cuerpo) {
                select()
                filter { eq("id", idCliente) }
            }
            .decodeSingle<ClienteDto>()
            .toDomain()
            .also { _clienteActual.value = it }
    }

    fun limpiarCache() {
        _clienteActual.value = null
    }

    private fun String?.limpio(): String? = this?.trim()?.takeIf { it.isNotEmpty() }
}
