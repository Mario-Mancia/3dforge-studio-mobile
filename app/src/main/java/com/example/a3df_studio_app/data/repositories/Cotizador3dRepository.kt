package com.example.a3df_studio_app.data.repositories

import android.content.Context
import android.net.Uri
import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.UriRequestBody
import com.example.a3df_studio_app.data.remote.dto.CotizacionDto
import com.example.a3df_studio_app.data.remote.dto.Estimar3dRespuestaDto
import com.example.a3df_studio_app.data.remote.dto.Opciones3dDto
import com.example.a3df_studio_app.domain.mappers.aDominio
import com.example.a3df_studio_app.domain.models.ArchivoLocal
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.Opciones3d
import com.example.a3df_studio_app.domain.models.Resultado3d
import com.example.a3df_studio_app.domain.models.Seleccion3d
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody

/** Cotizador automático de impresión 3D: el análisis y el precio los calcula el servidor (mismo motor que la web). */
class Cotizador3dRepository(
    private val api: ApiClient,
    private val contexto: Context
) {
    suspend fun opciones(): Opciones3d {
        val dto: Opciones3dDto = api.get("cotizador3d/opciones/")
        return dto.aDominio()
    }

    private fun MultipartBody.Builder.agregarOpciones(seleccion: Seleccion3d): MultipartBody.Builder = this
        .addFormDataPart("filamento", seleccion.filamentoId.toString())
        .addFormDataPart("relleno", seleccion.relleno.toString())
        .addFormDataPart("calidad", seleccion.calidad)
        .addFormDataPart("escala", seleccion.escala.toString())
        .addFormDataPart("unidades", seleccion.unidades)
        .addFormDataPart("soportes", seleccion.soportes)
        .addFormDataPart("cantidad", seleccion.cantidad.toString())

    private fun MultipartBody.Builder.agregarArchivo(archivo: ArchivoLocal): MultipartBody.Builder {
        val resolver = contexto.contentResolver
        val uri = Uri.parse(archivo.uri)
        val tipo = (resolver.getType(uri) ?: "application/octet-stream").toMediaTypeOrNull()
        return addFormDataPart("archivo", archivo.nombre, UriRequestBody(resolver, uri, tipo, archivo.tamano))
    }

    /** Primera vez: se sube [archivo]. Después se manda solo [analisisJson] (el servidor no vuelve a analizar el modelo). */
    suspend fun estimar(archivo: ArchivoLocal?, analisisJson: String?, seleccion: Seleccion3d): Resultado3d {
        val cuerpo = MultipartBody.Builder().setType(MultipartBody.FORM).agregarOpciones(seleccion)
        if (archivo != null) {
            cuerpo.agregarArchivo(archivo)
        } else {
            cuerpo.addFormDataPart("analisis", analisisJson.orEmpty())
        }
        val dto: Estimar3dRespuestaDto = api.postMultipart("cotizador3d/estimar/", cuerpo.build())
        return dto.aDominio()
    }

    /** Crea la cotización normal (estado Solicitada, importes preliminares) que revisará el equipo. */
    suspend fun solicitar(archivo: ArchivoLocal, seleccion: Seleccion3d, nombrePieza: String, comentarios: String): Cotizacion {
        val cuerpo = MultipartBody.Builder().setType(MultipartBody.FORM).agregarOpciones(seleccion).agregarArchivo(archivo)
        if (nombrePieza.isNotBlank()) cuerpo.addFormDataPart("descripcion", nombrePieza.trim())
        if (comentarios.isNotBlank()) cuerpo.addFormDataPart("comentarios", comentarios.trim())
        val dto: CotizacionDto = api.postMultipart("cotizaciones/3d/", cuerpo.build())
        return dto.aDominio()
    }
}
