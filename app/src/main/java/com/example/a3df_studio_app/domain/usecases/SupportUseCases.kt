package com.example.a3df_studio_app.domain.usecases

import com.example.a3df_studio_app.data.repositories.AccountRepository
import com.example.a3df_studio_app.data.repositories.NotificationRepository
import com.example.a3df_studio_app.data.repositories.SupportRepository
import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.CambiosPerfil
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.Notificacion
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.ResumenPanel
import com.example.a3df_studio_app.domain.models.SolicitudSoporte
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import com.example.a3df_studio_app.domain.validation.erroresPorCampo

// ---- Notificaciones ----

class ListNotificationsUseCase(private val repositorio: NotificationRepository) {
    suspend operator fun invoke(pagina: Int): Result<Pagina<Notificacion>> = resultOf { repositorio.listar(pagina) }
}

class GetUnreadCountUseCase(private val repositorio: NotificationRepository) {
    suspend operator fun invoke(): Result<Int> = resultOf { repositorio.noLeidas() }
}

class MarkNotificationReadUseCase(private val repositorio: NotificationRepository) {
    suspend operator fun invoke(id: Long): Result<Int> = resultOf { repositorio.marcarLeida(id) }
}

class MarkAllNotificationsReadUseCase(private val repositorio: NotificationRepository) {
    suspend operator fun invoke(): Result<Int> = resultOf { repositorio.marcarTodas() }
}

// ---- Soporte ----

class ListSupportUseCase(private val repositorio: SupportRepository) {
    suspend operator fun invoke(pagina: Int): Result<Pagina<SolicitudSoporte>> = resultOf { repositorio.listar(pagina) }
}

class GetSupportUseCase(private val repositorio: SupportRepository) {
    suspend operator fun invoke(id: Long): Result<SolicitudSoporte> = resultOf { repositorio.obtener(id) }
}

class CreateSupportUseCase(private val repositorio: SupportRepository) {
    suspend operator fun invoke(asunto: String, descripcion: String, prioridad: PrioridadSoporte): Result<SolicitudSoporte> = resultOf {
        val errores = erroresPorCampo(
            "asunto" to InputValidator.textoRequerido(asunto, "el asunto", 1, 150),
            "descripcion" to InputValidator.textoRequerido(descripcion, "la descripción", 10, 3000)
        )
        if (errores.isNotEmpty()) throw AppError.DatosInvalidos("Revisa los datos indicados.", errores)
        repositorio.crear(asunto, descripcion, prioridad)
    }
}

class CloseSupportUseCase(private val repositorio: SupportRepository) {
    suspend operator fun invoke(id: Long): Result<SolicitudSoporte> = resultOf { repositorio.cerrar(id) }
}

// ---- Perfil y panel ----

class GetProfileUseCase(private val repositorio: AccountRepository) {
    suspend operator fun invoke(): Result<Cliente> = resultOf { repositorio.perfil() }
}

class UpdateProfileUseCase(private val repositorio: AccountRepository) {
    suspend operator fun invoke(cambios: CambiosPerfil): Result<Cliente> = resultOf {
        val esEmpresa = cambios.tipo == TipoCliente.EMPRESA
        val errores = erroresPorCampo(
            "nombres" to InputValidator.nombrePersona(cambios.nombres, "tus nombres"),
            "apellidos" to InputValidator.nombrePersona(cambios.apellidos, "tus apellidos"),
            "nombre_empresa" to if (esEmpresa && cambios.nombreEmpresa.isBlank()) "Ingresa el nombre de la empresa." else null,
            "telefono" to InputValidator.telefono(cambios.telefono),
            "identificacion_fiscal" to InputValidator.identificacionFiscal(cambios.identificacionFiscal)
        )
        if (errores.isNotEmpty()) throw AppError.DatosInvalidos("Revisa los datos indicados.", errores)
        repositorio.actualizarPerfil(cambios)
    }
}

class GetPanelUseCase(private val repositorio: AccountRepository) {
    suspend operator fun invoke(): Result<ResumenPanel> = resultOf { repositorio.panel() }
}
