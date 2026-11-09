package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.remote.dto.CotizacionCreadaDto
import com.example.a3df_studio_app.data.remote.dto.CotizacionDetalleDto
import com.example.a3df_studio_app.data.remote.dto.CotizacionDto
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.CotizacionCreada
import com.example.a3df_studio_app.domain.models.CotizacionDetalle
import com.example.a3df_studio_app.domain.models.EstadoCotizacion

fun CotizacionDto.toDomain(): Cotizacion = Cotizacion(
    id = id,
    codigo = codigo,
    fechaEmision = fechaEmision.toFecha(),
    fechaVencimiento = fechaVencimiento.toFecha(),
    subtotal = subtotal,
    descuento = descuento,
    impuesto = impuesto,
    total = total,
    estado = EstadoCotizacion.desde(estado),
    observaciones = observaciones,
    detalles = detalles.map { it.toDomain() }
)

fun CotizacionDetalleDto.toDomain(): CotizacionDetalle = CotizacionDetalle(
    id = id,
    idServicio = idServicio,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal
)

fun CotizacionCreadaDto.toDomain(): CotizacionCreada = CotizacionCreada(
    id = id,
    codigo = codigo,
    total = total
)
