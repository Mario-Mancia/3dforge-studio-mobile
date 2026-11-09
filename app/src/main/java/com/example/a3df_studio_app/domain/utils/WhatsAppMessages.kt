package com.example.a3df_studio_app.domain.utils

import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.CotizacionCreada
import com.example.a3df_studio_app.domain.models.ItemCotizacion

object WhatsAppMessages {

    fun cotizacion(
        usuario: AuthUser,
        cotizacion: CotizacionCreada,
        items: List<ItemCotizacion>,
        notas: String?
    ): String = buildString {
        appendLine("¡Hola, 3DForge Studio! 👋")
        appendLine("Soy ${usuario.nombreVisible} y solicité la cotización ${cotizacion.codigo} desde la app.")
        appendLine()
        items.forEach { item ->
            append("• ${item.servicio.nombre} x ${item.cantidad.comoCantidad()}")
            item.detalle?.takeIf { it.isNotBlank() }?.let { append(" — $it") }
            appendLine()
        }
        appendLine()
        appendLine("Total estimado: ${cotizacion.total.comoMoneda()} (IVA incluido)")
        notas?.let { appendLine("Notas: $it") }
        append("Correo de contacto: ${usuario.email}")
    }

    fun cotizacionPersonalizada(usuario: AuthUser, descripcion: String): String = buildString {
        appendLine("¡Hola, 3DForge Studio! 👋")
        appendLine("Soy ${usuario.nombreVisible} y quiero cotizar un proyecto de impresión 3D:")
        appendLine()
        appendLine(descripcion)
        appendLine()
        append("Correo de contacto: ${usuario.email}")
    }

    fun asesoria(usuario: AuthUser, tema: String, mensaje: String?, horario: String?): String = buildString {
        appendLine("¡Hola, 3DForge Studio! 👋")
        appendLine("Soy ${usuario.nombreVisible} y me gustaría agendar una asesoría.")
        appendLine()
        appendLine("Tema: $tema")
        mensaje?.let { appendLine("Detalle: $it") }
        horario?.let { appendLine("Horario preferido: $it") }
        append("Correo de contacto: ${usuario.email}")
    }
}
