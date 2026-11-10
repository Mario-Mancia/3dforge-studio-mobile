package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.dto.ClienteDto
import com.example.a3df_studio_app.data.remote.dto.PanelDto
import com.example.a3df_studio_app.data.remote.dto.PerfilRequest
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.models.CambiosPerfil
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.ResumenPanel

class AccountRepository(private val api: ApiClient) {

    suspend fun perfil(): Cliente {
        val dto: ClienteDto = api.get("perfil/")
        return dto.aDominio()
    }

    suspend fun actualizarPerfil(cambios: CambiosPerfil): Cliente {
        val dto: ClienteDto = api.patch(
            "perfil/",
            PerfilRequest(
                tipoCliente = cambios.tipo.name,
                nombres = cambios.nombres.trim(),
                apellidos = cambios.apellidos.trim(),
                nombreEmpresa = cambios.nombreEmpresa.trim(),
                telefono = cambios.telefono.trim(),
                identificacionFiscal = cambios.identificacionFiscal.trim(),
                direccion = cambios.direccion.trim()
            )
        )
        return dto.aDominio()
    }

    suspend fun panel(): ResumenPanel {
        val dto: PanelDto = api.get("panel/")
        return dto.kpis.aDominio()
    }
}
