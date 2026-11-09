package com.example.a3df_studio_app.domain.validation

import com.example.a3df_studio_app.domain.models.ValidationException

object InputValidator {

    private val EMAIL = Regex("""^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$""")
    private val TELEFONO = Regex("""^\+?[0-9 ()-]{8,20}$""")
    private val OTP = Regex("""^[0-9]{6,10}$""")

    fun requireEmail(valor: String) {
        if (!EMAIL.matches(valor.trim())) throw ValidationException("Ingresa un correo electrónico válido.")
    }

    fun requirePassword(valor: String) {
        if (valor.length < 8 || valor.none { it.isDigit() } || valor.none { it.isLetter() }) {
            throw ValidationException("La contraseña debe tener al menos 8 caracteres e incluir letras y números.")
        }
    }

    fun requireNombre(valor: String?, campo: String) {
        if (valor.isNullOrBlank() || valor.trim().length < 2) throw ValidationException("Ingresa $campo válido.")
    }

    fun requireTelefonoOpcional(valor: String?) {
        if (!valor.isNullOrBlank() && !TELEFONO.matches(valor.trim())) {
            throw ValidationException("Ingresa un número de teléfono válido.")
        }
    }

    fun requireOtp(valor: String) {
        if (!OTP.matches(valor.trim())) throw ValidationException("El código de verificación debe tener 6 dígitos.")
    }

    fun requireLongitud(valor: String?, campo: String, minimo: Int, maximo: Int) {
        val largo = valor?.trim()?.length ?: 0
        if (largo < minimo) throw ValidationException("$campo debe tener al menos $minimo caracteres.")
        if (largo > maximo) throw ValidationException("$campo no puede superar $maximo caracteres.")
    }
}
