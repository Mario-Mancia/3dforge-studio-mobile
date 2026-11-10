package com.example.a3df_studio_app.data.external

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import java.io.File

object ArchivosExternos {

    private fun uriDe(contexto: Context, archivo: File): Uri =
        FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)

    /** Abre el archivo con una app del sistema; si no hay ninguna, ofrece compartirlo. */
    fun abrir(contexto: Context, archivo: File, mime: String?): Boolean {
        val uri = uriDe(contexto, archivo)
        val intento = Intent(Intent.ACTION_VIEW)
            .setDataAndType(uri, mime ?: "*/*")
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            contexto.startActivity(intento)
            true
        } catch (e: ActivityNotFoundException) {
            compartir(contexto, archivo, mime)
        }
    }

    fun compartir(contexto: Context, archivo: File, mime: String?): Boolean {
        val uri = uriDe(contexto, archivo)
        val envio = Intent(Intent.ACTION_SEND)
            .setType(mime ?: "*/*")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        val selector = Intent.createChooser(envio, "Compartir archivo").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            contexto.startActivity(selector)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    fun abrirEnlace(contexto: Context, url: String): Boolean {
        val intento = Intent(Intent.ACTION_VIEW, Uri.parse(url)).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        return try {
            contexto.startActivity(intento)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }

    fun mimeDe(nombre: String): String? = when (nombre.substringAfterLast('.', "").lowercase()) {
        "pdf" -> "application/pdf"
        "png" -> "image/png"
        "jpg", "jpeg" -> "image/jpeg"
        "stl" -> "model/stl"
        "obj" -> "model/obj"
        "3mf" -> "model/3mf"
        else -> null
    }
}
