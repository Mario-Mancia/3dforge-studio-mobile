package com.example.a3df_studio_app.data.config

import com.example.a3df_studio_app.BuildConfig

object AppConfig {
    /** Dirección fija del servidor (red Tailscale). Se puede sobrescribir con API_BASE_URL en local.properties. */
    val apiBaseUrl: String = BuildConfig.API_BASE_URL.trim()
    val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()

    const val NOMBRE_NEGOCIO = "3DForge Studio"
    const val TASA_IMPUESTO = "0.13"

    const val LIMITE_POR_PRODUCTO = 99
    const val MAX_PRODUCTOS_DISTINTOS = 30
    const val MAX_ARCHIVOS = 5
    const val MAX_BYTES_ARCHIVO = 50L * 1024L * 1024L
    val EXTENSIONES_PERMITIDAS: Set<String> = setOf("stl", "obj", "3mf", "step", "stp", "png", "jpg", "jpeg", "pdf")

    val isGoogleConfigured: Boolean
        get() = googleWebClientId.isNotBlank()
}
