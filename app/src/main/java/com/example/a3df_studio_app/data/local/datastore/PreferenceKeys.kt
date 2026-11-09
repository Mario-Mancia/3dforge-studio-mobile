package com.example.a3df_studio_app.data.local.datastore

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val ONBOARDING_VISTO = booleanPreferencesKey("onboarding_visto")
    val ULTIMO_CORREO = stringPreferencesKey("ultimo_correo")
    val CORREO_PENDIENTE_VERIFICACION = stringPreferencesKey("correo_pendiente_verificacion")
}
