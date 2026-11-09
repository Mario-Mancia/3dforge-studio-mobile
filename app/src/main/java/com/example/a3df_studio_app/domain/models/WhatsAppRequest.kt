package com.example.a3df_studio_app.domain.models

import android.content.Intent

data class WhatsAppRequest(
    val intent: Intent,
    val codigoReferencia: String?
)
