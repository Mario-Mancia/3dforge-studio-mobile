package com.example.a3df_studio_app.data.config

import com.example.a3df_studio_app.BuildConfig

object AppConfig {
    val supabaseUrl: String = BuildConfig.SUPABASE_URL.trim()
    val supabaseAnonKey: String = BuildConfig.SUPABASE_ANON_KEY.trim()
    val googleWebClientId: String = BuildConfig.GOOGLE_WEB_CLIENT_ID.trim()
    val whatsappFallbackNumber: String = BuildConfig.WHATSAPP_NUMBER.trim()

    const val AUTH_SCHEME = "forgestudio"
    const val AUTH_HOST = "auth-callback"
    const val NOMBRE_NEGOCIO = "3DForge Studio"

    val isSupabaseConfigured: Boolean
        get() = supabaseUrl.startsWith("https://") && supabaseAnonKey.isNotBlank()

    val isGoogleConfigured: Boolean
        get() = googleWebClientId.isNotBlank()
}
