package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.dto.FacturaDto
import com.example.a3df_studio_app.data.remote.dto.PaginaDto
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.mappers.aPagina
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.Pagina

class InvoiceRepository(private val api: ApiClient) {

    suspend fun listar(pagina: Int): Pagina<Factura> {
        val dto: PaginaDto<FacturaDto> = api.get("facturas/", mapOf("pagina" to pagina.toString(), "por_pagina" to "20"))
        return dto.aPagina { it.aDominio() }
    }

    suspend fun obtener(id: Long): Factura {
        val dto: FacturaDto = api.get("facturas/$id/")
        return dto.aDominio()
    }
}
