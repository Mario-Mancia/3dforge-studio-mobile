package com.example.a3df_studio_app.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class AsegurarClienteParams(
    @SerialName("p_nombres") val nombres: String? = null,
    @SerialName("p_apellidos") val apellidos: String? = null,
    @SerialName("p_telefono") val telefono: String? = null,
    @SerialName("p_foto_url") val fotoUrl: String? = null
)

@Serializable
data class CotizacionItemParam(
    @SerialName("id_servicio") val idServicio: Long,
    val cantidad: Double,
    val descripcion: String? = null
)

@Serializable
data class CrearCotizacionParams(
    @SerialName("p_items") val items: List<CotizacionItemParam>,
    @SerialName("p_observaciones") val observaciones: String? = null
)

@Serializable
data class CotizacionCreadaDto(
    val id: Long,
    val codigo: String,
    val total: Double
)

@Serializable
data class PedidoItemParam(
    @SerialName("id_producto") val idProducto: Long,
    val cantidad: Int
)

@Serializable
data class CrearPedidoParams(
    @SerialName("p_items") val items: List<PedidoItemParam>,
    @SerialName("p_observaciones") val observaciones: String? = null
)

@Serializable
data class PedidoCreadoDto(
    @SerialName("id_encargo") val idEncargo: Long,
    val codigo: String,
    @SerialName("id_factura") val idFactura: Long,
    @SerialName("numero_factura") val numeroFactura: String,
    val total: Double
)

@Serializable
data class PagoSimuladoParams(
    @SerialName("p_id_factura") val idFactura: Long,
    @SerialName("p_monto") val monto: Double,
    @SerialName("p_marca") val marca: String,
    @SerialName("p_ultimos4") val ultimos4: String,
    @SerialName("p_titular") val titular: String? = null
)

@Serializable
data class PagoRegistradoDto(
    @SerialName("id_pago") val idPago: Long,
    val referencia: String,
    val estado: String,
    @SerialName("saldo_pendiente") val saldoPendiente: Double,
    @SerialName("estado_factura") val estadoFactura: String
)

@Serializable
data class InteraccionParams(
    @SerialName("p_tipo") val tipo: String,
    @SerialName("p_asunto") val asunto: String,
    @SerialName("p_descripcion") val descripcion: String? = null
)
