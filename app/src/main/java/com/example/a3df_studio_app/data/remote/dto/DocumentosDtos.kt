package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonElement

@Serializable
data class ArchivoDto(
    val id: Long = 0,
    val nombre: String = "",
    val formato: String = "",
    @SerialName("tamano_bytes") val tamanoBytes: Long = 0,
    @SerialName("fecha_subida") val fechaSubida: String? = null
)

@Serializable
data class LineaDocDto(
    val id: Long = 0,
    val descripcion: String = "",
    val especificaciones: Map<String, JsonElement> = emptyMap(),
    val cantidad: String = "0",
    @SerialName("precio_unitario") val precioUnitario: String? = null,
    val descuento: String? = null,
    val impuesto: String? = null,
    val subtotal: String? = null,
    val archivos: List<ArchivoDto> = emptyList()
)

@Serializable
data class PedidoRefDto(val id: Long = 0, val codigo: String = "")

@Serializable
data class CotizacionDto(
    val id: Long = 0,
    val codigo: String = "",
    val estado: String = "",
    @SerialName("estado_texto") val estadoTexto: String = "",
    @SerialName("fecha_emision") val fechaEmision: String? = null,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String? = null,
    @SerialName("plazo_entrega_dias") val plazoEntregaDias: Int? = null,
    val subtotal: String? = null,
    val descuento: String? = null,
    val impuesto: String? = null,
    val total: String? = null,
    val vencida: Boolean = false,
    @SerialName("puede_responder") val puedeResponder: Boolean = false,
    @SerialName("puede_cancelar") val puedeCancelar: Boolean = false,
    val solicitud: String? = null,
    val observaciones: String? = null,
    val lineas: List<LineaDocDto> = emptyList(),
    val archivos: List<ArchivoDto> = emptyList(),
    val pedido: PedidoRefDto? = null
)

@Serializable
data class HistorialDto(
    @SerialName("estado_nuevo_texto") val estadoNuevoTexto: String = "",
    val comentario: String? = null,
    val fecha: String? = null
)

@Serializable
data class EncargoDto(
    val id: Long = 0,
    val codigo: String = "",
    val estado: String = "",
    @SerialName("estado_texto") val estadoTexto: String = "",
    val progreso: Int? = null,
    @SerialName("fecha_solicitud") val fechaSolicitud: String? = null,
    @SerialName("fecha_entrega_estimada") val fechaEntregaEstimada: String? = null,
    @SerialName("fecha_entrega_real") val fechaEntregaReal: String? = null,
    val descripcion: String? = null,
    @SerialName("cotizacion_id") val cotizacionId: Long? = null,
    val subtotal: String? = null,
    val descuento: String? = null,
    val impuesto: String? = null,
    val total: String? = null,
    @SerialName("puede_cancelar") val puedeCancelar: Boolean = false,
    val responsable: String? = null,
    val lineas: List<LineaDocDto> = emptyList(),
    val historial: List<HistorialDto> = emptyList()
)

@Serializable
data class PagoDto(
    val id: Long = 0,
    val monto: String? = null,
    @SerialName("fecha_pago") val fechaPago: String? = null,
    val referencia: String? = null,
    val metodo: String = ""
)

@Serializable
data class FacturaDto(
    val id: Long = 0,
    val numero: String = "",
    val estado: String = "",
    @SerialName("estado_texto") val estadoTexto: String = "",
    @SerialName("fecha_emision") val fechaEmision: String? = null,
    @SerialName("fecha_vencimiento") val fechaVencimiento: String? = null,
    val total: String? = null,
    @SerialName("saldo_pendiente") val saldoPendiente: String? = null,
    @SerialName("pedido_id") val pedidoId: Long? = null,
    @SerialName("nombre_facturacion") val nombreFacturacion: String? = null,
    @SerialName("identificacion_fiscal") val identificacionFiscal: String? = null,
    @SerialName("direccion_facturacion") val direccionFacturacion: String? = null,
    val subtotal: String? = null,
    val impuesto: String? = null,
    val lineas: List<LineaDocDto> = emptyList(),
    val pagos: List<PagoDto> = emptyList(),
    val aviso: String? = null
)
