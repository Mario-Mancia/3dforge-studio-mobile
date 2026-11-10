package com.example.a3df_studio_app.data.repositories

import android.content.Context
import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.domain.models.ArchivoAdjunto
import java.io.File

enum class TipoDocumento(val ruta: String) {
    COTIZACION("cotizaciones"),
    PEDIDO("pedidos")
}

/** Descarga archivos de cotizaciones y pedidos (con el token de sesión) a la caché de la app. */
class FileRepository(
    private val api: ApiClient,
    private val contexto: Context
) {
    suspend fun descargar(tipo: TipoDocumento, documentoId: Long, archivo: ArchivoAdjunto): File {
        val limpio = archivo.nombre.replace(Regex("[^A-Za-z0-9._-]"), "_").take(80)
        val destino = File(File(contexto.cacheDir, "descargas"), "${tipo.name.lowercase()}-$documentoId-${archivo.id}-$limpio")
        api.descargar("${tipo.ruta}/$documentoId/archivos/${archivo.id}/", destino)
        return destino
    }
}
