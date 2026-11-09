package com.example.a3df_studio_app.domain.validation

import com.example.a3df_studio_app.domain.models.MarcaTarjeta
import com.example.a3df_studio_app.domain.models.TarjetaPago
import com.example.a3df_studio_app.domain.models.ValidationException
import java.time.YearMonth

object CardValidator {

    fun limpiarNumero(numero: String): String = numero.filter { it.isDigit() }

    fun detectarMarca(numero: String): MarcaTarjeta {
        val n = limpiarNumero(numero)
        val dos = n.take(2).toIntOrNull() ?: -1
        val cuatro = n.take(4).toIntOrNull() ?: -1
        return when {
            n.startsWith("4") -> MarcaTarjeta.VISA
            dos in 51..55 || cuatro in 2221..2720 -> MarcaTarjeta.MASTERCARD
            dos == 34 || dos == 37 -> MarcaTarjeta.AMEX
            else -> MarcaTarjeta.DESCONOCIDA
        }
    }

    fun luhn(numero: String): Boolean {
        val n = limpiarNumero(numero)
        if (n.isEmpty()) return false
        var suma = 0
        var duplicar = false
        for (i in n.indices.reversed()) {
            var digito = n[i] - '0'
            if (duplicar) {
                digito *= 2
                if (digito > 9) digito -= 9
            }
            suma += digito
            duplicar = !duplicar
        }
        return suma % 10 == 0
    }

    fun enmascarar(numero: String): String = "•••• " + limpiarNumero(numero).takeLast(4)

    fun validar(tarjeta: TarjetaPago): MarcaTarjeta {
        val numero = limpiarNumero(tarjeta.numero)
        if (numero.length !in 13..19 || !luhn(numero)) {
            throw ValidationException("El número de tarjeta no es válido.")
        }
        if (tarjeta.titular.trim().length < 3) {
            throw ValidationException("Ingresa el nombre del titular como aparece en la tarjeta.")
        }
        if (tarjeta.mesExpiracion !in 1..12) {
            throw ValidationException("El mes de expiración no es válido.")
        }
        val anio = if (tarjeta.anioExpiracion < 100) 2000 + tarjeta.anioExpiracion else tarjeta.anioExpiracion
        if (YearMonth.of(anio, tarjeta.mesExpiracion).isBefore(YearMonth.now())) {
            throw ValidationException("La tarjeta está vencida.")
        }
        val marca = detectarMarca(numero)
        val largoCvv = if (marca == MarcaTarjeta.AMEX) 4 else 3
        if (tarjeta.cvv.length != largoCvv || tarjeta.cvv.any { !it.isDigit() }) {
            throw ValidationException("El código de seguridad (CVV) no es válido.")
        }
        return marca
    }
}
