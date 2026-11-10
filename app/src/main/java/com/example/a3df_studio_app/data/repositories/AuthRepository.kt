package com.example.a3df_studio_app.data.repositories

import android.content.Context
import com.example.a3df_studio_app.data.local.AppPreferences
import com.example.a3df_studio_app.data.remote.ApiClient
import com.example.a3df_studio_app.data.remote.auth.GoogleCredentialProvider
import com.example.a3df_studio_app.data.remote.dto.CambiarPasswordRequest
import com.example.a3df_studio_app.data.remote.dto.CorreoRequest
import com.example.a3df_studio_app.data.remote.dto.GoogleRequest
import com.example.a3df_studio_app.data.remote.dto.LoginRequest
import com.example.a3df_studio_app.data.remote.dto.MensajeDto
import com.example.a3df_studio_app.data.remote.dto.OkDto
import com.example.a3df_studio_app.data.remote.dto.RegistroRequest
import com.example.a3df_studio_app.data.remote.dto.SesionDto
import com.example.a3df_studio_app.domain.mappers.aSesionLocal
import com.example.a3df_studio_app.domain.mappers.aUsuario
import com.example.a3df_studio_app.domain.models.AuthState
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.SolicitudRegistro
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit

class AuthRepository(
    private val api: ApiClient,
    private val preferencias: AppPreferences,
    private val google: GoogleCredentialProvider
) {
    private val _estado = MutableStateFlow<AuthState>(AuthState.Cargando)
    val estado: StateFlow<AuthState> = _estado.asStateFlow()

    val usuarioActual: AuthUser?
        get() = (_estado.value as? AuthState.ConSesion)?.usuario

    /** Restaura la sesión guardada y la renueva si le quedan pocos días. */
    suspend fun restaurar() {
        val token = preferencias.leerToken()
        val sesion = preferencias.leerSesion()
        if (token == null || sesion == null) {
            api.token = null
            _estado.value = AuthState.SinSesion
            return
        }
        api.token = token
        _estado.value = AuthState.ConSesion(sesion.aUsuario())
        if (debeRenovar(sesion.expiraEn)) {
            try {
                refrescar()
            } catch (e: Exception) {
                // Si el token ya no sirve, ApiClient.alExpirar cierra la sesión local.
            }
        }
    }

    private fun debeRenovar(expiraEn: String?): Boolean {
        if (expiraEn.isNullOrBlank()) return false
        return runCatching {
            val limite = OffsetDateTime.parse(expiraEn).toInstant()
            Instant.now().plus(7, ChronoUnit.DAYS).isAfter(limite)
        }.getOrDefault(false)
    }

    private suspend fun refrescar() {
        val dto: SesionDto = api.postVacio("auth/refrescar/")
        guardar(dto)
    }

    private suspend fun guardar(dto: SesionDto): AuthUser {
        api.token = dto.token
        val sesion = dto.aSesionLocal()
        preferencias.guardarSesion(dto.token, sesion)
        preferencias.guardarUltimoCorreo(dto.usuario.correo)
        val usuario = sesion.aUsuario()
        _estado.value = AuthState.ConSesion(usuario)
        return usuario
    }

    suspend fun ultimoCorreo(): String = preferencias.leerUltimoCorreo().orEmpty()

    suspend fun login(correo: String, clave: String): AuthUser {
        val dto: SesionDto = api.post("auth/login/", LoginRequest(correo.trim().lowercase(), clave))
        return guardar(dto)
    }

    suspend fun loginConGoogle(contexto: Context): AuthUser {
        val credencial = google.requestIdToken(contexto)
        val dto: SesionDto = api.post("auth/google/", GoogleRequest(credencial.idToken, credencial.rawNonce))
        return guardar(dto)
    }

    suspend fun registrar(datos: SolicitudRegistro): String {
        val respuesta: MensajeDto = api.post(
            "auth/registro/",
            RegistroRequest(
                nombres = datos.nombres.trim(),
                apellidos = datos.apellidos.trim(),
                correo = datos.correo.trim().lowercase(),
                telefono = datos.telefono.trim(),
                password = datos.password
            )
        )
        preferencias.guardarUltimoCorreo(datos.correo.trim().lowercase())
        return respuesta.mensaje
    }

    suspend fun reenviarConfirmacion(correo: String): String {
        val respuesta: MensajeDto = api.post("auth/reenviar-confirmacion/", CorreoRequest(correo.trim().lowercase()))
        return respuesta.mensaje
    }

    suspend fun recuperarPassword(correo: String): String {
        val respuesta: MensajeDto = api.post("auth/recuperar/", CorreoRequest(correo.trim().lowercase()))
        return respuesta.mensaje
    }

    suspend fun cambiarPassword(actual: String, nueva: String): String {
        val respuesta: MensajeDto = api.post("auth/cambiar-password/", CambiarPasswordRequest(actual, nueva))
        return respuesta.mensaje
    }

    suspend fun cerrarSesion() {
        try {
            val ignorado: OkDto = api.postVacio("auth/logout/")
        } catch (e: Exception) {
            // El cierre remoto es opcional: siempre se limpia la sesión local.
        }
        cerrarLocal()
    }

    suspend fun cerrarLocal() {
        api.token = null
        preferencias.limpiarSesion()
        _estado.value = AuthState.SinSesion
    }
}
