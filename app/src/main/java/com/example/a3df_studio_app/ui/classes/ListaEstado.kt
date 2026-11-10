package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.utils.mensajeParaUsuario
import java.io.File

/** Estado de una lista paginada. */
data class ListaEstado<T>(
    val items: List<T> = emptyList(),
    val cargando: Boolean = false,
    val cargandoMas: Boolean = false,
    val hayMas: Boolean = false,
    val pagina: Int = 0,
    val error: String? = null,
    val cargada: Boolean = false
)

/** Carga la primera página ([reiniciar]) o la siguiente, publicando cada cambio de estado. */
suspend fun <T> cargarPagina(
    actual: ListaEstado<T>,
    reiniciar: Boolean,
    pedir: suspend (Int) -> Result<Pagina<T>>,
    publicar: (ListaEstado<T>) -> Unit
) {
    if (actual.cargando || actual.cargandoMas) return
    if (!reiniciar && actual.cargada && !actual.hayMas) return
    val base: ListaEstado<T> = if (reiniciar) ListaEstado(cargando = true) else actual.copy(cargandoMas = true, error = null)
    publicar(base)
    val numero = if (reiniciar) 1 else actual.pagina + 1
    val resultado = pedir(numero)
    val respuesta = resultado.getOrNull()
    if (respuesta != null) {
        val previos = if (reiniciar) emptyList() else actual.items
        publicar(ListaEstado(items = previos + respuesta.items, hayMas = respuesta.hayMas, pagina = respuesta.pagina, cargada = true))
    } else {
        val error = resultado.exceptionOrNull()?.mensajeParaUsuario()
        publicar(base.copy(cargando = false, cargandoMas = false, error = error, cargada = true))
    }
}

fun Throwable.mensajeUi(): String? = if (this is AppError.Cancelado) null else mensajeParaUsuario()

fun Throwable.camposUi(): Map<String, List<String>> = (this as? AppError.DatosInvalidos)?.campos ?: emptyMap()

fun Map<String, List<String>>.primerError(campo: String): String? = this[campo]?.firstOrNull()

/** Archivo descargado listo para abrirse con una app del sistema. */
data class DescargaLista(val archivo: File, val mime: String?)
