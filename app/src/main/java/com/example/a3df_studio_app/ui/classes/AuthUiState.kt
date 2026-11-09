package com.example.a3df_studio_app.ui.classes

data class AuthUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val mensaje: String? = null,
    val correoVerificacion: String? = null,
    val autenticado: Boolean = false
)
