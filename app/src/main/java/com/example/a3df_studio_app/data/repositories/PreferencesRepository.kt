package com.example.a3df_studio_app.data.repositories

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import com.example.a3df_studio_app.data.local.datastore.PreferenceKeys
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.forgeDataStore: DataStore<Preferences> by preferencesDataStore(name = "forge_preferences")

class PreferencesRepository(private val context: Context) {

    val onboardingVisto: Flow<Boolean> = context.forgeDataStore.data
        .map { it[PreferenceKeys.ONBOARDING_VISTO] ?: false }

    val ultimoCorreo: Flow<String?> = context.forgeDataStore.data
        .map { it[PreferenceKeys.ULTIMO_CORREO] }

    val correoPendienteVerificacion: Flow<String?> = context.forgeDataStore.data
        .map { it[PreferenceKeys.CORREO_PENDIENTE_VERIFICACION] }

    suspend fun marcarOnboardingVisto() {
        context.forgeDataStore.edit { it[PreferenceKeys.ONBOARDING_VISTO] = true }
    }

    suspend fun guardarUltimoCorreo(correo: String) {
        context.forgeDataStore.edit { it[PreferenceKeys.ULTIMO_CORREO] = correo }
    }

    suspend fun guardarCorreoPendiente(correo: String?) {
        context.forgeDataStore.edit { prefs ->
            if (correo == null) {
                prefs.remove(PreferenceKeys.CORREO_PENDIENTE_VERIFICACION)
            } else {
                prefs[PreferenceKeys.CORREO_PENDIENTE_VERIFICACION] = correo
            }
        }
    }
}
