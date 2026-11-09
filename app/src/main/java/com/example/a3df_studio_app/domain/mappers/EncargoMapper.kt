package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.remote.dto.EncargoDetalleDto
import com.example.a3df_studio_app.data.remote.dto.EncargoDto
import com.example.a3df_studio_app.data.remote.dto.HistorialEncargoDto
import com.example.a3df_studio_app.data.remote.dto.PedidoCreadoDto
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.EncargoDetalle
import com.example.a3df_studio_app.domain.models.EstadoEncargo
import com.example.a3df_studio_app.domain.models.HistorialEncargo
import com.example.a3df_studio_app.domain.models.PedidoCreado

fun EncargoDto.toDomain(): Encargo = Encargo(
    id = id,
    codigo = codigo,
    fechaSolicitud = fechaSolicitud.toFecha(),
    fechaEntregaEstimada = fechaEntregaEstimada.toFecha(),
    fechaEntregaReal = fechaEntregaReal.toFecha(),
    descripcion = descripcion,
    subtotal = subtotal,
    descuento = descuento,
    impuesto = impuesto,
    total = total,
    estado = EstadoEncargo.desde(estado),
    observaciones = observaciones,
    detalles = detalles.map { it.toDomain() },
    historial = historial.map { it.toDomain() }.sortedBy { it.fecha }
)

fun EncargoDetalleDto.toDomain(): EncargoDetalle = EncargoDetalle(
    id = id,
    idServicio = idServicio,
    idProducto = idProducto,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal
)

fun HistorialEncargoDto.toDomain(): HistorialEncargo = HistorialEncargo(
    id = id,
    estadoAnterior = estadoAnterior?.let { EstadoEncargo.desde(it) },
    estadoNuevo = EstadoEncargo.desde(estadoNuevo),
    comentario = comentario,
    fecha = fechaCambio.toFecha()
)

fun PedidoCreadoDto.toDomain(): PedidoCreado = PedidoCreado(
    idEncargo = idEncargo,
    codigo = codigo,
    idFactura = idFactura,
    numeroFactura = numeroFactura,
    total = total
)
