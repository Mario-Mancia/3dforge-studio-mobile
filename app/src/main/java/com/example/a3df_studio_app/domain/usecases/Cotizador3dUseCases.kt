package com.example.a3df_studio_app.domain.usecases

import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.data.repositories.Cotizador3dRepository
import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.ArchivoLocal
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.Opciones3d
import com.example.a3df_studio_app.domain.models.Resultado3d
import com.example.a3df_studio_app.domain.models.Seleccion3d
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.erroresPorCampo

private val FORMATOS_3D = setOf("stl", "obj", "3mf")

/** Mensaje de error si el archivo no sirve para el cotizador 3D (el servidor lo valida de nuevo). */
fun errorDeArchivo3d(archivo: ArchivoLocal): String? {
    val extension = archivo.nombre.substringAfterLast('.', "").lowercase()
    return when {
        extension !in FORMATOS_3D -> "Usa un archivo STL, OBJ o 3MF."
        archivo.tamano <= 0 -> "El archivo está vacío."
        archivo.tamano > AppConfig.MAX_BYTES_ARCHIVO -> "El archivo supera 50 MB."
        else -> null
    }
}

class GetOpciones3dUseCase(private val repositorio: Cotizador3dRepository) {
    suspend operator fun invoke(): Result<Opciones3d> = resultOf { repositorio.opciones() }
}

class Estimar3dUseCase(private val repositorio: Cotizador3dRepository) {
    suspend operator fun invoke(archivo: ArchivoLocal?, analisisJson: String?, seleccion: Seleccion3d): Result<Resultado3d> = resultOf {
        if (archivo == null && analisisJson.isNullOrBlank()) throw AppError.Local("Primero elige el archivo del modelo 3D.")
        archivo?.let { errorDeArchivo3d(it) }?.let { throw AppError.Local(it) }
        repositorio.estimar(archivo, analisisJson, seleccion)
    }
}

class Solicitar3dUseCase(private val repositorio: Cotizador3dRepository) {
    suspend operator fun invoke(
        archivo: ArchivoLocal?,
        seleccion: Seleccion3d,
        nombrePieza: String,
        comentarios: String
    ): Result<Cotizacion> = resultOf {
        if (archivo == null) throw AppError.Local("Elige el archivo del modelo 3D.")
        val errores = erroresPorCampo(
            "archivo" to errorDeArchivo3d(archivo),
            "descripcion" to if (nombrePieza.length > 200) "Máximo 200 caracteres." else null,
            "comentarios" to if (comentarios.length > 2000) "Máximo 2000 caracteres." else null
        )
        if (errores.isNotEmpty()) throw AppError.DatosInvalidos("Revisa los datos indicados.", errores)
        repositorio.solicitar(archivo, seleccion, nombrePieza, comentarios)
    }
}
