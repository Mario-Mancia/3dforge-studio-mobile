package com.example.a3df_studio_app.data.remote

import android.content.ContentResolver
import android.net.Uri
import okhttp3.MediaType
import okhttp3.RequestBody
import okio.BufferedSink
import okio.source
import java.io.IOException

/** Envía un archivo elegido por el usuario (content://) en streaming, sin cargarlo completo en memoria. */
class UriRequestBody(
    private val resolver: ContentResolver,
    private val uri: Uri,
    private val tipo: MediaType?,
    private val longitud: Long
) : RequestBody() {

    override fun contentType(): MediaType? = tipo

    override fun contentLength(): Long = longitud

    override fun writeTo(sink: BufferedSink) {
        val entrada = resolver.openInputStream(uri) ?: throw IOException("No se pudo leer el archivo.")
        entrada.source().use { origen -> sink.writeAll(origen) }
    }
}
