package com.example.a3df_studio_app.domain.usecases

import android.content.Context
import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.domain.models.AppError
import com.example.a3df_studio_app.domain.models.AuthState
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.SolicitudRegistro
import com.example.a3df_studio_app.domain.utils.resultOf
import com.example.a3df_studio_app.domain.validation.InputValidator
import com.example.a3df_studio_app.domain.validation.erroresPorCampo
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(private val repositorio: AuthRepository) {
    operator fun invoke(): StateFlow<AuthState> = repositorio.estado
}

class RestoreSessionUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke() = repositorio.restaurar()
}

class GetLastEmailUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(): String = repositorio.ultimoCorreo()
}

class LoginUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(correo: String, clave: String): Result<AuthUser> = resultOf {
        val errores = erroresPorCampo(
            "correo" to InputValidator.correo(correo),
            "password" to if (clave.isBlank()) "Ingresa tu contraseña." else null
        )
        if (errores.isNotEmpty()) throw AppError.DatosInvalidos("Revisa los datos indicados.", errores)
        repositorio.login(correo, clave)
    }
}

class LoginWithGoogleUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(contexto: Context): Result<AuthUser> = resultOf { repositorio.loginConGoogle(contexto) }
}

class RegisterUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(datos: SolicitudRegistro, confirmacion: String, acepta: Boolean): Result<String> = resultOf {
        val errores = erroresPorCampo(
            "nombres" to InputValidator.nombrePersona(datos.nombres, "tus nombres"),
            "apellidos" to InputValidator.nombrePersona(datos.apellidos, "tus apellidos"),
            "correo" to InputValidator.correo(datos.correo),
            "telefono" to InputValidator.telefono(datos.telefono),
            "password" to InputValidator.password(datos.password),
            "password2" to if (datos.password != confirmacion) "Las contraseñas no coinciden." else null,
            "terminos" to if (!acepta) "Debes aceptar los términos y condiciones." else null
        )
        if (errores.isNotEmpty()) throw AppError.DatosInvalidos("Revisa los datos indicados.", errores)
        repositorio.registrar(datos)
    }
}

class ResendConfirmationUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(correo: String): Result<String> = resultOf {
        InputValidator.correo(correo)?.let { throw AppError.Local(it) }
        repositorio.reenviarConfirmacion(correo)
    }
}

class RecoverPasswordUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(correo: String): Result<String> = resultOf {
        InputValidator.correo(correo)?.let { throw AppError.Local(it) }
        repositorio.recuperarPassword(correo)
    }
}

class ChangePasswordUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke(actual: String, nueva: String, confirmacion: String): Result<String> = resultOf {
        val errores = erroresPorCampo(
            "password_actual" to if (actual.isBlank()) "Ingresa tu contraseña actual." else null,
            "password_nueva" to InputValidator.password(nueva),
            "confirmacion" to if (nueva != confirmacion) "Las contraseñas no coinciden." else null
        )
        if (errores.isNotEmpty()) throw AppError.DatosInvalidos("Revisa los datos indicados.", errores)
        repositorio.cambiarPassword(actual, nueva)
    }
}

class LogoutUseCase(private val repositorio: AuthRepository) {
    suspend operator fun invoke() = repositorio.cerrarSesion()
}
