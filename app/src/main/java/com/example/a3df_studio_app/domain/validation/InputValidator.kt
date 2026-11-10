package com.example.a3df_studio_app.domain.validation

object InputValidator {

    private val REGEX_NOMBRE = Regex("^\\p{L}+(?:[ '.\\-]\\p{L}+)*$")
    private val REGEX_TELEFONO = Regex("^\\+?[0-9][0-9 ()\\-]{6,24}$")
    private val REGEX_FISCAL = Regex("^[A-Za-z0-9][A-Za-z0-9 .\\-]{3,29}$")
    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+\\-]+@[A-Za-z0-9.\\-]+\\.[A-Za-z]{2,}$")

    fun correo(valor: String): String? = when {
        valor.isBlank() -> "Ingresa tu correo."
        valor.length > 150 || !REGEX_CORREO.matches(valor.trim()) -> "Ingresa un correo válido."
        else -> null
    }

    fun password(valor: String): String? = when {
        valor.isBlank() -> "Ingresa una contraseña."
        valor.length < 10 -> "Debe tener al menos 10 caracteres."
        else -> null
    }

    fun nombrePersona(valor: String, etiqueta: String): String? = when {
        valor.isBlank() -> "Ingresa $etiqueta."
        valor.length > 100 -> "Máximo 100 caracteres."
        !REGEX_NOMBRE.matches(valor.trim()) -> "Solo letras, espacios y . ' -"
        else -> null
    }

    fun telefono(valor: String): String? =
        if (valor.isBlank() || REGEX_TELEFONO.matches(valor.trim())) null else "Teléfono no válido (ej. +503 7000-1111)."

    fun identificacionFiscal(valor: String): String? =
        if (valor.isBlank() || REGEX_FISCAL.matches(valor.trim())) null else "Identificación no válida."

    fun textoRequerido(valor: String, etiqueta: String, minimo: Int, maximo: Int): String? = when {
        valor.isBlank() -> "Ingresa $etiqueta."
        valor.trim().length < minimo -> "Mínimo $minimo caracteres."
        valor.trim().length > maximo -> "Máximo $maximo caracteres."
        else -> null
    }

    fun cantidadDecimal(valor: String): String? {
        val numero = valor.trim().toBigDecimalOrNull() ?: return "Cantidad no válida."
        return when {
            numero.signum() <= 0 -> "Debe ser mayor que cero."
            numero.scale() > 2 -> "Máximo 2 decimales."
            else -> null
        }
    }
}

/** Convierte pares campo → mensaje en el mapa de errores por campo que entiende la UI. */
fun erroresPorCampo(vararg pares: Pair<String, String?>): Map<String, List<String>> =
    pares.mapNotNull { (campo, mensaje) -> mensaje?.let { campo to listOf(it) } }.toMap()
