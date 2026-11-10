package com.example.a3df_studio_app.domain.models

enum class EstadoCotizacion(val etiqueta: String) {
    SOLICITADA("Solicitada"),
    BORRADOR("En revisión"),
    ENVIADA("Enviada"),
    ACEPTADA("Aceptada"),
    RECHAZADA("Rechazada"),
    VENCIDA("Vencida"),
    CANCELADA("Cancelada"),
    DESCONOCIDO("Desconocido");

    companion object {
        fun desde(valor: String?): EstadoCotizacion = entries.firstOrNull { it.name == valor } ?: DESCONOCIDO
    }
}

enum class EstadoEncargo(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    EN_REVISION("En revisión"),
    APROBADO("Aprobado"),
    EN_PRODUCCION("En producción"),
    EN_ESPERA("En espera"),
    FINALIZADO("Finalizado"),
    ENTREGADO("Entregado"),
    CANCELADO("Cancelado"),
    DESCONOCIDO("Desconocido");

    companion object {
        fun desde(valor: String?): EstadoEncargo = entries.firstOrNull { it.name == valor } ?: DESCONOCIDO
    }
}

enum class EstadoFactura(val etiqueta: String) {
    PENDIENTE("Pendiente"),
    PARCIALMENTE_PAGADA("Parcialmente pagada"),
    PAGADA("Pagada"),
    ANULADA("Anulada"),
    VENCIDA("Vencida"),
    DESCONOCIDO("Desconocido");

    companion object {
        fun desde(valor: String?): EstadoFactura = entries.firstOrNull { it.name == valor } ?: DESCONOCIDO
    }
}

enum class EstadoSoporte(val etiqueta: String) {
    ABIERTA("Abierta"),
    EN_PROCESO("En proceso"),
    RESUELTA("Resuelta"),
    CERRADA("Cerrada"),
    DESCONOCIDO("Desconocido");

    companion object {
        fun desde(valor: String?): EstadoSoporte = entries.firstOrNull { it.name == valor } ?: DESCONOCIDO
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

enum class TipoNotificacion {
    COTIZACION, ENCARGO, FACTURA, SOPORTE, OTRO;

    companion object {
        fun desde(valor: String?): TipoNotificacion = when (valor?.lowercase()) {
            "cotizacion" -> COTIZACION
            "encargo" -> ENCARGO
            "factura" -> FACTURA
            "soporte" -> SOPORTE
            else -> OTRO
        }
    }
}

enum class Disponibilidad(val etiqueta: String) {
    DISPONIBLE("Disponible"),
    ULTIMAS("Últimas unidades"),
    AGOTADO("Agotado");

    companion object {
        fun desde(valor: String?): Disponibilidad = when (valor) {
            "agotado" -> AGOTADO
            "ultimas" -> ULTIMAS
            else -> DISPONIBLE
        }
    }
}

enum class TipoCliente(val etiqueta: String) {
    PERSONA("Persona"),
    EMPRESA("Empresa");

    companion object {
        fun desde(valor: String?): TipoCliente = if (valor == "EMPRESA") EMPRESA else PERSONA
    }
}
