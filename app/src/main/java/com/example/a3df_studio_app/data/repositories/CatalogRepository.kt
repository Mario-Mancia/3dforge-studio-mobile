package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.dto.CategoriaDto
import com.example.a3df_studio_app.data.remote.dto.ListaDto
import com.example.a3df_studio_app.data.remote.dto.NegocioDto
import com.example.a3df_studio_app.data.remote.dto.PaginaDto
import com.example.a3df_studio_app.data.remote.dto.ProductoDto
import com.example.a3df_studio_app.data.remote.dto.ServicioDto
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.mappers.aPagina
import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.models.InfoNegocio
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.models.Servicio

/** Catálogo público: no requiere sesión. */
class CatalogRepository(private val api: ApiClient) {

    suspend fun productos(busqueda: String?, pagina: Int): Pagina<Producto> {
        val dto: PaginaDto<ProductoDto> = api.get(
            "catalogo/productos/",
            mapOf("q" to busqueda?.trim()?.takeIf { it.isNotEmpty() }, "pagina" to pagina.toString(), "por_pagina" to "20")
        )
        return dto.aPagina { it.aDominio() }
    }

    suspend fun producto(id: Long): Producto {
        val dto: ProductoDto = api.get("catalogo/productos/$id/")
        return dto.aDominio()
    }

    suspend fun categorias(): List<CategoriaServicio> {
        val dto: ListaDto<CategoriaDto> = api.get("catalogo/categorias/")
        return dto.resultados.map { it.aDominio() }
    }

    suspend fun servicios(categoriaId: Long?): List<Servicio> {
        val dto: ListaDto<ServicioDto> = api.get("catalogo/servicios/", mapOf("categoria" to categoriaId?.toString()))
        return dto.resultados.map { it.aDominio() }
    }

    suspend fun servicio(id: Long): Servicio {
        val dto: ServicioDto = api.get("catalogo/servicios/$id/")
        return dto.aDominio()
    }

    suspend fun negocio(): InfoNegocio {
        val dto: NegocioDto = api.get("negocio/")
        return dto.aDominio()
    }
}
