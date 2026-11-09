package com.example.a3df_studio_app.ui.classes

import com.example.a3df_studio_app.domain.models.AuthState
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.Cliente

data class MainUiState(
    val authState: AuthState = AuthState.Loading,
    val cliente: Cliente? = null
) {
    val usuario: AuthUser?
        get() = (authState as? AuthState.Authenticated)?.user

    val isLoggedIn: Boolean
        get() = authState is AuthState.Authenticated

    val avatarUrl: String?
        get() = cliente?.fotoUrl ?: usuario?.avatarUrl
}
