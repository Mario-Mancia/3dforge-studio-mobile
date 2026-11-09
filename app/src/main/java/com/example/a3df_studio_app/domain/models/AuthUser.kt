package com.example.a3df_studio_app.domain.models

data class AuthUser(
    val id: String,
    val email: String,
    val nombreCompleto: String?,
    val nombres: String?,
    val apellidos: String?,
    val avatarUrl: String?,
    val emailVerificado: Boolean,
    val proveedor: String
) {
    val nombreVisible: String
        get() = nombreCompleto?.takeIf { it.isNotBlank() } ?: email.substringBefore("@")

    val primerNombre: String
        get() = nombreVisible.substringBefore(" ")

    val iniciales: String
        get() = nombreVisible.split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
}
