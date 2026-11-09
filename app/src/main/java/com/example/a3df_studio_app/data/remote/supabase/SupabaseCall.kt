package com.example.a3df_studio_app.data.remote.supabase

import com.example.a3df_studio_app.domain.models.AppException
import com.example.a3df_studio_app.domain.models.AuthRequiredException
import com.example.a3df_studio_app.domain.models.NotConfiguredException
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.postgrest.exception.PostgrestRestException
import kotlinx.serialization.SerializationException
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

internal suspend fun <T> supabaseCall(block: suspend () -> T): T {
    if (!Database.isConfigured) throw NotConfiguredException()
    return try {
        block()
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        throw e.toAppException()
    }
}

internal fun Throwable.toAppException(): AppException = when (this) {
    is AppException -> this
    is AuthRestException -> AppException(authMessage(), this)
    is PostgrestRestException -> postgrestException()
    is IOException -> AppException("No hay conexión a internet o el servidor no está disponible.", this)
    is SerializationException -> AppException("La respuesta del servidor no tiene el formato esperado.", this)
    else -> AppException(message ?: "Ocurrió un error inesperado.", this)
}

private fun AuthRestException.authMessage(): String = when (errorCode) {
    AuthErrorCode.InvalidCredentials -> "Correo o contraseña incorrectos."
    AuthErrorCode.EmailNotConfirmed -> "Debes verificar tu correo antes de iniciar sesión."
    AuthErrorCode.UserAlreadyExists, AuthErrorCode.EmailExists -> "Ya existe una cuenta registrada con este correo."
    AuthErrorCode.OtpExpired -> "El código expiró o no es válido. Solicita uno nuevo."
    AuthErrorCode.WeakPassword -> "La contraseña es demasiado débil."
    AuthErrorCode.OverEmailSendRateLimit -> "Se enviaron demasiados correos. Espera unos minutos e inténtalo de nuevo."
    else -> errorDescription.ifBlank { "No se pudo completar la autenticación." }
}

private fun PostgrestRestException.postgrestException(): AppException {
    val raw = error
    return when {
        raw.contains("AUTH_REQUIRED") || code == "PGRST301" -> AuthRequiredException()
        raw.contains("CLIENTE_NO_REGISTRADO") -> AppException("Tu perfil de cliente aún no existe. Vuelve a iniciar sesión.")
        raw.contains("SIN_ITEMS") -> AppException("Agrega al menos un elemento a la solicitud.")
        raw.contains("CANTIDAD_INVALIDA") -> AppException("Las cantidades deben ser mayores a cero.")
        raw.contains("SERVICIO_NO_DISPONIBLE") -> AppException("Uno de los servicios seleccionados ya no está disponible.")
        raw.contains("PRODUCTO_NO_DISPONIBLE") -> AppException("Uno de los productos seleccionados ya no está disponible.")
        raw.contains("STOCK_INSUFICIENTE") -> AppException("No hay stock suficiente de ${raw.substringAfter("STOCK_INSUFICIENTE:", "un producto").trim()}.")
        raw.contains("FACTURA_NO_ENCONTRADA") -> AppException("No se encontró la factura seleccionada.")
        raw.contains("FACTURA_NO_PAGABLE") -> AppException("Esta factura ya no admite pagos.")
        raw.contains("MONTO_INVALIDO") -> AppException("El monto no es válido para esta factura.")
        raw.contains("TARJETA_INVALIDA") -> AppException("Los datos de la tarjeta no son válidos.")
        raw.contains("TIPO_INVALIDO") -> AppException("Tipo de solicitud no válido.")
        raw.contains("usuario del sistema") -> AppException("El negocio aún no tiene un usuario responsable configurado.")
        code == "42501" -> AppException("No tienes permisos para realizar esta acción.")
        code == "PGRST202" -> AppException("Falta ejecutar el script supabase/app_movil.sql en la base de datos.")
        else -> AppException("Error del servidor: $raw", this)
    }
}
