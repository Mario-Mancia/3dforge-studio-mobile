package com.example.a3df_studio_app.domain.usecases

import com.example.a3df_studio_app.data.repositories.FileRepository
import com.example.a3df_studio_app.data.repositories.InvoiceRepository
import com.example.a3df_studio_app.data.repositories.OrderRepository
import com.example.a3df_studio_app.data.repositories.QuoteRepository
import com.example.a3df_studio_app.data.repositories.TipoDocumento
import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.ArchivoAdjunto
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.models.SolicitudCotizacion
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import com.example.a3df_studio_app.domain.validation.erroresPorCampo
import com.example.a3df_studio_app.data.config.AppConfig
import java.io.File

// ---- Cotizaciones ----

class ListQuotesUseCase(private val repositorio: QuoteRepository) {
    suspend operator fun invoke(pagina: Int): Result<Pagina<Cotizacion>> = resultOf { repositorio.listar(pagina) }
}

class GetQuoteUseCase(private val repositorio: QuoteRepository) {
    suspend operator fun invoke(id: Long): Result<Cotizacion> = resultOf { repositorio.obtener(id) }
}

class RequestQuoteUseCase(private val repositorio: QuoteRepository) {
    suspend operator fun invoke(solicitud: SolicitudCotizacion): Result<Cotizacion> = resultOf {
        val errores = erroresPorCampo(
            "servicio" to if (solicitud.servicioId <= 0) "Elige un servicio." else null,
            "cantidad" to InputValidator.cantidadDecimal(solicitud.cantidad),
            "descripcion" to InputValidator.textoRequerido(solicitud.titulo, "un título", 1, 250),
            "solicitud" to InputValidator.textoRequerido(solicitud.solicitud, "la descripción", 10, 5000),
            "material" to if (solicitud.material.length > 60) "Máximo 60 caracteres." else null,
            "color" to if (solicitud.color.length > 60) "Máximo 60 caracteres." else null,
            "dimensiones" to if (solicitud.dimensiones.length > 100) "Máximo 100 caracteres." else null,
            "archivos" to errorDeArchivos(solicitud)
        )
        if (errores.isNotEmpty()) throw AppError.DatosInvalidos("Revisa los datos indicados.", errores)
        repositorio.crear(solicitud)
    }

    private fun errorDeArchivos(solicitud: SolicitudCotizacion): String? {
        if (solicitud.archivos.size > AppConfig.MAX_ARCHIVOS) return "Máximo ${AppConfig.MAX_ARCHIVOS} archivos."
        solicitud.archivos.forEach { archivo ->
            val extension = archivo.nombre.substringAfterLast('.', "").lowercase()
            if (extension !in AppConfig.EXTENSIONES_PERMITIDAS) return "${archivo.nombre}: formato no permitido."
            if (archivo.tamano <= 0) return "${archivo.nombre}: el archivo está vacío."
            if (archivo.tamano > AppConfig.MAX_BYTES_ARCHIVO) return "${archivo.nombre}: supera 50 MB."
        }
        return null
    }
}

/** accion: "aceptar", "rechazar" o "cancelar". */
class RespondQuoteUseCase(private val repositorio: QuoteRepository) {
    suspend operator fun invoke(cotizacion: Cotizacion, accion: String): Result<Cotizacion> = resultOf {
        val permitido = when (accion) {
            "aceptar", "rechazar" -> cotizacion.puedeResponder
            "cancelar" -> cotizacion.puedeCancelar
            else -> false
        }
        if (!permitido) throw AppError.Local("Esta cotización ya no permite esa acción.")
        repositorio.responder(cotizacion.id, accion)
    }
}

// ---- Pedidos ----

class ListOrdersUseCase(private val repositorio: OrderRepository) {
    suspend operator fun invoke(pagina: Int): Result<Pagina<Encargo>> = resultOf { repositorio.listar(pagina) }
}

class GetOrderUseCase(private val repositorio: OrderRepository) {
    suspend operator fun invoke(id: Long): Result<Encargo> = resultOf { repositorio.obtener(id) }
}

class CancelOrderUseCase(private val repositorio: OrderRepository) {
    suspend operator fun invoke(encargo: Encargo): Result<Encargo> = resultOf {
        if (!encargo.puedeCancelar) throw AppError.Local("Este pedido ya no se puede cancelar.")
        repositorio.cancelar(encargo.id)
    }
}

// ---- Facturas ----

class ListInvoicesUseCase(private val repositorio: InvoiceRepository) {
    suspend operator fun invoke(pagina: Int): Result<Pagina<Factura>> = resultOf { repositorio.listar(pagina) }
}

class GetInvoiceUseCase(private val repositorio: InvoiceRepository) {
    suspend operator fun invoke(id: Long): Result<Factura> = resultOf { repositorio.obtener(id) }
}

// ---- Archivos ----

class DownloadFileUseCase(private val repositorio: FileRepository) {
    suspend operator fun invoke(tipo: TipoDocumento, documentoId: Long, archivo: ArchivoAdjunto): Result<File> =
        resultOf { repositorio.descargar(tipo, documentoId, archivo) }
}
