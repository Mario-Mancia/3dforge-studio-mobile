package com.example.a3df_studio_app.domain.models

enum class TipoCliente(val etiqueta: String) {
    PERSONA("Persona"),
    EMPRESA("Empresa");

    companion object {
        fun desde(valor: String?): TipoCliente = entries.firstOrNull { it.name == valor } ?: PERSONA
    }
}

enum class EstadoCotizacion(val etiqueta: String) {
    BORRADOR("En revisión"),
    ENVIADA("Enviada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada"),
    VENCIDA("Vencida"),
    CANCELADA("Cancelada");

    companion object {
        fun desde(valor: String?): EstadoCotizacion = entries.firstOrNull { it.name == valor } ?: BORRADOR
    }
}

enum class EstadoEncargo(val etiqueta: String, val paso: Int) {
    PENDIENTE("Pendiente", 0),
    EN_REVISION("En revisión", 1),
    APROBADO("Aprobado", 2),
    EN_PRODUCCION("En producción", 3),
    EN_ESPERA("En espera", 3),
    FINALIZADO("Finalizado", 4),
    ENTREGADO("Entregado", 5),
    CANCELADO("Cancelado", -1);

    val finalizado: Boolean
        get() = this == ENTREGADO || this == CANCELADO

    companion object {
        const val TOTAL_PASOS = 5

        fun desde(valor: String?): EstadoEncargo = entries.firstOrNull { it.name == valor } ?: PENDIENTE
    }
}

enum class EstadoFactura(val etiqueta: String, val pagable: Boolean) {
    PENDIENTE("Pendiente", true),
    PARCIALMENTE_PAGADA("Pago parcial", true),
    PAGADA("Pagada", false),
    ANULADA("Anulada", false),
    VENCIDA("Vencida", true);

    companion object {
        fun desde(valor: String?): EstadoFactura = entries.firstOrNull { it.name == valor } ?: PENDIENTE
    }
}

enum class EstadoPago(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    CONFIRMADO("Confirmado"),
    RECHAZADO("Rechazado"),
    REEMBOLSADO("Reembolsado"),
    CANCELADO("Cancelado");

    companion object {
        fun desde(valor: String?): EstadoPago = entries.firstOrNull { it.name == valor } ?: PENDIENTE
    }
}

enum class PrioridadSoporte(val etiqueta: String) {
    BAJA("Baja"),
    MEDIA("Media"),
    ALTA("Alta"),
    URGENTE("Urgente");

    companion object {
        fun desde(valor: String?): PrioridadSoporte = entries.firstOrNull { it.name == valor } ?: MEDIA
    }
}

enum class EstadoSoporte(val etiqueta: String) {
    ABIERTA("Abierta"),
    EN_PROCESO("En proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada");

    companion object {
        fun desde(valor: String?): EstadoSoporte = entries.firstOrNull { it.name == valor } ?: ABIERTA
    }
}

enum class TipoInteraccion {
    LLAMADA,
    CORREO,
    ASESORIA,
    REUNION,
    SEGUIMIENTO,
    RECLAMO
}

enum class TipoDescuento {
    PORCENTAJE,
    MONTO_FIJO;

    companion object {
        fun desde(valor: String?): TipoDescuento = entries.firstOrNull { it.name == valor } ?: PORCENTAJE
    }
}

enum class MarcaTarjeta(val etiqueta: String) {
    VISA("Visa"),
    MASTERCARD("Mastercard"),
    AMEX("American Express"),
    DESCONOCIDA("Tarjeta")
}
