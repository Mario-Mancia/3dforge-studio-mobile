package com.example.a3df_studio_app.data.external

import android.content.Intent
import android.net.Uri
import com.example.a3df_studio_app.domain.models.AppException

class WhatsAppLauncher {

    fun crearIntent(telefono: String, mensaje: String): Intent {
        val digitos = telefono.filter { it.isDigit() }
        if (digitos.length < 8) throw AppException("El número de WhatsApp del negocio no es válido.")
        val uri = Uri.parse("https://wa.me/$digitos?text=${Uri.encode(mensaje)}")
        return Intent(Intent.ACTION_VIEW, uri).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    }
}
