package com.example.a3df_studio_app.domain.usecases.cotizaciones

import com.example.a3df_studio_app.data.external.WhatsAppLauncher
import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.CrmRepository
import com.example.a3df_studio_app.data.repositories.NegocioRepository
import com.example.a3df_studio_app.domain.models.TipoInteraccion
import com.example.a3df_studio_app.domain.models.WhatsAppRequest
import com.example.a3df_studio_app.domain.utils.WhatsAppMessages
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.utils.valorONull
import com.example.a3df_studio_app.domain.validation.InputValidator
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class RequestAdvisoryUseCase(
    private val authRepository: AuthRepository,
    private val crmRepository: CrmRepository,
    private val negocioRepository: NegocioRepository,
    private val whatsAppLauncher: WhatsAppLauncher,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    suspend operator fun invoke(
        tema: String,
        mensaje: String? = null,
        horarioPreferido: String? = null
    ): Result<WhatsAppRequest> = withContext(dispatcher) {
        resultOf {
            val usuario = authRepository.requireUser()
            InputValidator.requireLongitud(tema, "El tema", 3, 120)
            val detalle = mensaje?.trim()?.takeIf { it.isNotEmpty() }
            val horario = horarioPreferido?.trim()?.takeIf { it.isNotEmpty() }
            val telefono = negocioRepository.obtenerTelefonoWhatsApp()

            val descripcionCrm = listOfNotNull(detalle, horario?.let { "Horario preferido: $it" })
                .joinToString("\n")
                .ifBlank { null }
            valorONull {
                crmRepository.registrarInteraccion(TipoInteraccion.ASESORIA, "Asesoría: ${tema.trim()}", descripcionCrm)
            }

            val texto = WhatsAppMessages.asesoria(usuario, tema.trim(), detalle, horario)
            WhatsAppRequest(whatsAppLauncher.crearIntent(telefono, texto), null)
        }
    }
}
