package com.example.a3df_studio_app.ui.classes

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.a3df_studio_app.domain.models.ArchivoLocal

/** Lee el nombre y el tamaño de un archivo elegido con el selector del sistema (content://). */
fun leerArchivoLocal(contexto: Context, uri: Uri): ArchivoLocal {
    var nombre = "archivo"
    var tamano = 0L
    contexto.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val indiceNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val indiceTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (indiceNombre >= 0) nombre = cursor.getString(indiceNombre) ?: nombre
            if (indiceTamano >= 0 && !cursor.isNull(indiceTamano)) tamano = cursor.getLong(indiceTamano)
        }
    }
    return ArchivoLocal(uri = uri.toString(), nombre = nombre, tamano = tamano)
}
