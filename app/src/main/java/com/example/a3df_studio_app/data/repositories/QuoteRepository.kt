package com.example.a3df_studio_app.data.repositories

import android.content.Context
import android.net.Uri
import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.UriRequestBody
import com.example.a3df_studio_app.data.remote.dto.CotizacionDto
import com.example.a3df_studio_app.data.remote.dto.PaginaDto
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.mappers.aPagina
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.models.SolicitudCotizacion
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

class QuoteRepository(
    private val api: ApiClient,
    private val contexto: Context
) {
    suspend fun listar(pagina: Int): Pagina<Cotizacion> {
        val dto: PaginaDto<CotizacionDto> = api.get("cotizaciones/", mapOf("pagina" to pagina.toString(), "por_pagina" to "20"))
        return dto.aPagina { it.aDominio() }
    }

    suspend fun obtener(id: Long): Cotizacion {
        val dto: CotizacionDto = api.get("cotizaciones/$id/")
        return dto.aDominio()
    }

    /** Una sola petición multipart: formulario + archivos. */
    suspend fun crear(solicitud: SolicitudCotizacion): Cotizacion {
        val resolver = contexto.contentResolver
        val cuerpo = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart("servicio", solicitud.servicioId.toString())
            .addFormDataPart("cantidad", solicitud.cantidad.trim())
            .addFormDataPart("descripcion", solicitud.titulo.trim())
            .addFormDataPart("solicitud", solicitud.solicitud.trim())
        if (solicitud.material.isNotBlank()) cuerpo.addFormDataPart("material", solicitud.material.trim())
        if (solicitud.color.isNotBlank()) cuerpo.addFormDataPart("color", solicitud.color.trim())
        if (solicitud.dimensiones.isNotBlank()) cuerpo.addFormDataPart("dimensiones", solicitud.dimensiones.trim())
        solicitud.archivos.forEach { archivo ->
            val uri = Uri.parse(archivo.uri)
            val tipo = (resolver.getType(uri) ?: "application/octet-stream").toMediaTypeOrNull()
            cuerpo.addFormDataPart("archivos", archivo.nombre, UriRequestBody(resolver, uri, tipo, archivo.tamano))
        }
        val dto: CotizacionDto = api.postMultipart("cotizaciones/", cuerpo.build())
        return dto.aDominio()
    }

    /** accion: aceptar, rechazar o cancelar. Devuelve el detalle actualizado. */
    suspend fun responder(id: Long, accion: String): Cotizacion {
        val dto: CotizacionDto = api.postVacio("cotizaciones/$id/$accion/")
        return dto.aDominio()
    }
}
