package com.example.a3df_studio_app.data.repositories

import android.content.Context
import com.example.a3df_studio_app.data.remote.auth.GoogleCredentialProvider
import com.example.a3df_studio_app.data.remote.supabase.Database
import com.example.a3df_studio_app.data.remote.supabase.supabaseCall
import com.example.a3df_studio_app.domain.mappers.toAuthUser
import com.example.a3df_studio_app.domain.models.AppException
import com.example.a3df_studio_app.domain.models.AuthRequiredException
import com.example.a3df_studio_app.domain.models.AuthState
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.EmailNotVerifiedException
import com.example.a3df_studio_app.domain.models.NotConfiguredException
import com.example.a3df_studio_app.domain.models.SignUpResult
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

class AuthRepository(
    private val googleCredentialProvider: GoogleCredentialProvider
) {

    val authState: Flow<AuthState> = flow {
        if (!Database.isConfigured) {
            emit(AuthState.NotConfigured)
            return@flow
        }
        emitAll(Database.auth.sessionStatus.map { it.toAuthState() })
    }.distinctUntilChanged()

    fun usuarioActual(): AuthUser? {
        if (!Database.isConfigured) return null
        return Database.auth.currentUserOrNull()?.toAuthUser()
    }

    fun requireUser(): AuthUser {
        if (!Database.isConfigured) throw NotConfiguredException()
        return usuarioActual() ?: throw AuthRequiredException()
    }

    suspend fun registrar(
        correo: String,
        clave: String,
        nombres: String,
        apellidos: String
    ): SignUpResult = supabaseCall {
        val resultado = Database.auth.signUpWith(Email) {
            email = correo
            password = clave
            data = buildJsonObject {
                put("nombres", nombres)
                put("apellidos", apellidos)
                put("full_name", "$nombres $apellidos")
            }
        }
        val sesion = Database.auth.currentUserOrNull()
        if (resultado == null && sesion != null) {
            SignUpResult.SignedIn(sesion.toAuthUser())
        } else {
            SignUpResult.VerificationRequired(correo)
        }
    }

    suspend fun verificarCodigoRegistro(correo: String, codigo: String): AuthUser = supabaseCall {
        Database.auth.verifyEmailOtp(type = OtpType.Email.SIGNUP, email = correo, token = codigo)
        Database.auth.currentUserOrNull()?.toAuthUser()
            ?: throw AppException("No se pudo iniciar la sesión después de verificar el correo.")
    }

    suspend fun reenviarCodigoRegistro(correo: String) = supabaseCall {
        Database.auth.resendEmail(OtpType.Email.SIGNUP, correo)
    }

    suspend fun iniciarSesion(correo: String, clave: String): AuthUser = supabaseCall {
        try {
            Database.auth.signInWith(Email) {
                email = correo
                password = clave
            }
        } catch (e: AuthRestException) {
            if (e.errorCode == AuthErrorCode.EmailNotConfirmed) throw EmailNotVerifiedException(correo)
            throw e
        }
        Database.auth.currentUserOrNull()?.toAuthUser()
            ?: throw AppException("No se pudo recuperar la sesión del usuario.")
    }

    suspend fun iniciarSesionConGoogle(activityContext: Context): AuthUser = supabaseCall {
        val token = googleCredentialProvider.requestIdToken(activityContext)
        Database.auth.signInWith(IDToken) {
            idToken = token.idToken
            provider = Google
            nonce = token.rawNonce
        }
        Database.auth.currentUserOrNull()?.toAuthUser()
            ?: throw AppException("No se pudo recuperar la sesión de Google.")
    }

    suspend fun enviarRecuperacionClave(correo: String) = supabaseCall {
        Database.auth.resetPasswordForEmail(correo)
    }

    suspend fun cerrarSesion() = supabaseCall {
        Database.auth.signOut()
    }

    private fun SessionStatus.toAuthState(): AuthState = when (this) {
        is SessionStatus.Authenticated ->
            session.user?.let { AuthState.Authenticated(it.toAuthUser()) } ?: AuthState.Loading
        is SessionStatus.NotAuthenticated -> AuthState.Guest
        is SessionStatus.Initializing -> AuthState.Loading
        is SessionStatus.RefreshFailure ->
            Database.auth.currentUserOrNull()?.let { AuthState.Authenticated(it.toAuthUser()) } ?: AuthState.Guest
    }
}
