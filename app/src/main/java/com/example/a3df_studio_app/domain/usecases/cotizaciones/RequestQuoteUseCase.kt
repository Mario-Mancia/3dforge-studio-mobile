package com.example.a3df_studio_app.domain.usecases.cotizaciones

import com.example.a3df_studio_app.data.external.WhatsAppLauncher
import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.CotizacionRepository
import com.example.a3df_studio_app.data.repositories.CrmRepository
import com.example.a3df_studio_app.data.repositories.NegocioRepository
import com.example.a3df_studio_app.domain.models.ItemCotizacion
import com.example.a3df_studio_app.domain.models.TipoInteraccion
import com.example.a3df_studio_app.domain.models.ValidationException
import com.example.a3df_studio_app.domain.models.WhatsAppRequest
import com.example.a3df_studio_app.domain.utils.WhatsAppMessages
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.utils.valorONull
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestQuoteUseCase(
    private val authRepository: AuthRepository,
    private val cotizacionRepository: CotizacionRepository,
    private val crmRepository: CrmRepository,
    private val negocioRepository: NegocioRepository,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        items: List<ItemCotizacion>,
        observaciones: String? = null
    ): Result<WhatsAppRequest> = withContext(dispatcher) {
        resultOf {
            val usuario = authRepository.requireUser()
            val notas = observaciones?.trim()?.takeIf { it.isNotEmpty() }
            val telefono = negocioRepository.obtenerTelefonoWhatsApp()

            if (items.isEmpty()) {
                if (notas == null || notas.length < 10) {
                    throw ValidationException("Describe tu proyecto (mínimo 10 caracteres) o agrega servicios.")
                }
                valorONull {
                    crmRepository.registrarInteraccion(
                        TipoInteraccion.SEGUIMIENTO,
                        "Solicitud de cotización personalizada (app)",
                        notas
                    )
                }
                val mensaje = WhatsAppMessages.cotizacionPersonalizada(usuario, notas)
                WhatsAppRequest(whatsAppLauncher.crearIntent(telefono, mensaje), null)
            } else {
                if (items.any { it.cantidad <= 0.0 }) {
                    throw ValidationException("Las cantidades deben ser mayores a cero.")
                }
                val creada = cotizacionRepository.crearCotizacion(items, notas)
                val mensaje = WhatsAppMessages.cotizacion(usuario, creada, items, notas)
                WhatsAppRequest(whatsAppLauncher.crearIntent(telefono, mensaje), creada.codigo)
            }
        }
    }
}
