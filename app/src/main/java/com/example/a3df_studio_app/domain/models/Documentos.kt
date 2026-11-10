package com.example.a3df_studio_app.domain.models

import java.math.BigDecimal

data class ArchivoAdjunto(
    val id: Long,
    val nombre: String,
    val formato: String,
    val tamanoBytes: Long,
    val fecha: String
)

data class DetalleDocumento(
    val id: Long,
    val descripcion: String,
    val especificaciones: Map<String, String>,
    val cantidad: String,
    val precioUnitario: BigDecimal,
    val descuento: BigDecimal,
    val impuesto: BigDecimal,
    val subtotal: BigDecimal,
    val archivos: List<ArchivoAdjunto>
)

data class Cotizacion(
    val id: Long,
    val codigo: String,
    val estado: EstadoCotizacion,
    val estadoTexto: String,
    val fechaEmision: String,
    val fechaVencimiento: String?,
    val plazoEntregaDias: Int?,
    val subtotal: BigDecimal,
    val descuento: BigDecimal,
    val impuesto: BigDecimal,
    val total: BigDecimal,
    val vencida: Boolean,
    val puedeResponder: Boolean,
    val puedeCancelar: Boolean,
    val solicitud: String,
    val observaciones: String,
    val lineas: List<DetalleDocumento>,
    val archivos: List<ArchivoAdjunto>,
    val pedidoId: Long?,
    val pedidoCodigo: String?
)

data class HistorialEstado(
    val estadoNuevoTexto: String,
    val comentario: String,
    val fecha: String
)

data class Encargo(
    val id: Long,
    val codigo: String,
    val estado: EstadoEncargo,
    val estadoTexto: String,
    val progreso: Int?,
    val fechaSolicitud: String,
    val fechaEntregaEstimada: String?,
    val fechaEntregaReal: String?,
    val descripcion: String,
    val cotizacionId: Long?,
    val subtotal: BigDecimal,
    val descuento: BigDecimal,
    val impuesto: BigDecimal,
    val total: BigDecimal,
    val puedeCancelar: Boolean,
    val responsable: String?,
    val lineas: List<DetalleDocumento>,
    val historial: List<HistorialEstado>
)

data class CompraConfirmada(
    val pedido: Encargo,
    val duplicado: Boolean
)

data class Pago(
    val id: Long,
    val monto: BigDecimal,
    val fecha: String,
    val referencia: String,
    val metodo: String
)

data class Factura(
    val id: Long,
    val numero: String,
    val estado: EstadoFactura,
    val estadoTexto: String,
    val fechaEmision: String,
    val fechaVencimiento: String?,
    val total: BigDecimal,
    val saldoPendiente: BigDecimal,
    val pedidoId: Long?,
    val nombreFacturacion: String,
    val identificacionFiscal: String,
    val direccionFacturacion: String,
    val subtotal: BigDecimal,
    val impuesto: BigDecimal,
    val lineas: List<DetalleDocumento>,
    val pagos: List<Pago>,
    val aviso: String
)
