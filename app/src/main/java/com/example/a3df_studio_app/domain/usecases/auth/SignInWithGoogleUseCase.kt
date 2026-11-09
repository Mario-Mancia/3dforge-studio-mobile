package com.example.a3df_studio_app.domain.usecases.auth

import android.content.Context
import com.example.a3df_studio_app.data.repositories.AuthRepository
import com.example.a3df_studio_app.data.repositories.ClienteRepository
import com.example.a3df_studio_app.data.repositories.PreferencesRepository
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.utils.resultOf

class SignInWithGoogleUseCase(
    private val authRepository: AuthRepository,
    private val clienteRepository: ClienteRepository,
    private val preferencesRepository: PreferencesRepository
) {
    suspend operator fun invoke(activityContext: Context): Result<AuthUser> = resultOf {
        val usuario = authRepository.iniciarSesionConGoogle(activityContext)
        preferencesRepository.guardarUltimoCorreo(usuario.email)
        clienteRepository.asegurarCliente(usuario.nombres, usuario.apellidos, null, usuario.avatarUrl)
        usuario
    }
}
