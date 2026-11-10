package com.example.a3df_studio_app.data.repositories

import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.dto.ContadorDto
import com.example.a3df_studio_app.data.remote.dto.NotificacionDto
import com.example.a3df_studio_app.data.remote.dto.PaginaDto
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.mappers.aPagina
import com.example.a3df_studio_app.domain.models.Notificacion
import com.example.a3df_studio_app.domain.models.Pagina

class NotificationRepository(private val api: ApiClient) {

    suspend fun listar(pagina: Int): Pagina<Notificacion> {
        val dto: PaginaDto<NotificacionDto> = api.get("notificaciones/", mapOf("pagina" to pagina.toString(), "por_pagina" to "20"))
        return dto.aPagina { it.aDominio() }
    }

    suspend fun noLeidas(): Int {
        val dto: ContadorDto = api.get("notificaciones/contador/")
        return dto.noLeidas
    }

    suspend fun marcarLeida(id: Long): Int {
        val dto: ContadorDto = api.postVacio("notificaciones/$id/leer/")
        return dto.noLeidas
    }

    suspend fun marcarTodas(): Int {
        val dto: ContadorDto = api.postVacio("notificaciones/leer-todas/")
        return dto.noLeidas
    }
}
