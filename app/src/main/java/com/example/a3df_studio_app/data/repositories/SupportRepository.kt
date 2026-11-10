package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.dto.PaginaDto
import com.example.a3df_studio_app.data.remote.dto.SoporteDto
import com.example.a3df_studio_app.data.remote.dto.SoporteRequest
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.mappers.aPagina
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.SolicitudSoporte

class SupportRepository(private val api: ApiClient) {

    suspend fun listar(pagina: Int): Pagina<SolicitudSoporte> {
        val dto: PaginaDto<SoporteDto> = api.get("soporte/", mapOf("pagina" to pagina.toString(), "por_pagina" to "20"))
        return dto.aPagina { it.aDominio() }
    }

    suspend fun obtener(id: Long): SolicitudSoporte {
        val dto: SoporteDto = api.get("soporte/$id/")
        return dto.aDominio()
    }

    suspend fun crear(asunto: String, descripcion: String, prioridad: PrioridadSoporte): SolicitudSoporte {
        val dto: SoporteDto = api.post("soporte/", SoporteRequest(asunto.trim(), descripcion.trim(), prioridad.name))
        return dto.aDominio()
    }

    suspend fun cerrar(id: Long): SolicitudSoporte {
        val dto: SoporteDto = api.postVacio("soporte/$id/cerrar/")
        return dto.aDominio()
    }
}
