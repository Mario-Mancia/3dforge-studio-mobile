package com.example.a3df_studio_app.domain.models

sealed interface AuthState {
    data object Loading : AuthState
    data object NotConfigured : AuthState
    data object Guest : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
}

sealed interface SignUpResult {
    data class VerificationRequired(val correo: String) : SignUpResult
    data class SignedIn(val user: AuthUser) : SignUpResult
}
