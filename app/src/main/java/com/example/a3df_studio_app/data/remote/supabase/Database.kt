package com.example.a3df_studio_app.data.remote.supabase

import com.example.a3df_studio_app.data.config.AppConfig
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.serializer.KotlinXSerializer
import kotlinx.serialization.json.Json

object Database {

    val json: Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        coerceInputValues = true
        encodeDefaults = true
    }

    val isConfigured: Boolean
        get() = AppConfig.isSupabaseConfigured

    val client: SupabaseClient by lazy {
        check(isConfigured) { NOT_CONFIGURED_MESSAGE }
        createSupabaseClient(
            supabaseUrl = AppConfig.supabaseUrl,
            supabaseKey = AppConfig.supabaseAnonKey
        ) {
            defaultSerializer = KotlinXSerializer(json)
            install(Auth) {
                scheme = AppConfig.AUTH_SCHEME
                host = AppConfig.AUTH_HOST
            }
            install(Postgrest)
        }
    }

    val auth: Auth
        get() = client.auth

    val postgrest: Postgrest
        get() = client.postgrest

    const val NOT_CONFIGURED_MESSAGE =
        "Supabase no está configurado. Define SUPABASE_URL y SUPABASE_ANON_KEY en local.properties."
}
