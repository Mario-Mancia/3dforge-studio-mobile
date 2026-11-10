package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.dto.EncargoDto
import com.example.a3df_studio_app.data.remote.dto.PaginaDto
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.mappers.aPagina
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.Pagina

class OrderRepository(private val api: ApiClient) {

    suspend fun listar(pagina: Int): Pagina<Encargo> {
        val dto: PaginaDto<EncargoDto> = api.get("pedidos/", mapOf("pagina" to pagina.toString(), "por_pagina" to "20"))
        return dto.aPagina { it.aDominio() }
    }

    suspend fun obtener(id: Long): Encargo {
        val dto: EncargoDto = api.get("pedidos/$id/")
        return dto.aDominio()
    }

    suspend fun cancelar(id: Long): Encargo {
        val dto: EncargoDto = api.postVacio("pedidos/$id/cancelar/")
        return dto.aDominio()
    }
}
