package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.remote.dto.FacturaDetalleDto
import com.example.a3df_studio_app.data.remote.dto.FacturaDto
import com.example.a3df_studio_app.data.remote.dto.PagoDto
import com.example.a3df_studio_app.data.remote.dto.PagoRegistradoDto
import com.example.a3df_studio_app.domain.models.EstadoFactura
import com.example.a3df_studio_app.domain.models.EstadoPago
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.FacturaDetalle
import com.example.a3df_studio_app.domain.models.Pago
import com.example.a3df_studio_app.domain.models.PagoRegistrado

fun FacturaDto.toDomain(): Factura = Factura(
    id = id,
    idEncargo = idEncargo,
    numero = numeroFactura,
    fechaEmision = fechaEmision.toFecha(),
    fechaVencimiento = fechaVencimiento.toFecha(),
    nombreFacturacion = nombreFacturacion,
    subtotal = subtotal,
    descuento = descuento,
    impuesto = impuesto,
    total = total,
    saldoPendiente = saldoPendiente,
    estado = EstadoFactura.desde(estado),
    detalles = detalles.map { it.toDomain() }
)

fun FacturaDetalleDto.toDomain(): FacturaDetalle = FacturaDetalle(
    id = id,
    descripcion = descripcion,
    cantidad = cantidad,
    precioUnitario = precioUnitario,
    subtotal = subtotal
)

fun PagoDto.toDomain(): Pago = Pago(
    id = id,
    idFactura = idFactura,
    referencia = referencia,
    monto = monto,
    fecha = fechaPago.toFecha(),
    estado = EstadoPago.desde(estado),
    metodo = metodo?.nombre,
    observaciones = observaciones
)

fun PagoRegistradoDto.toDomain(): PagoRegistrado = PagoRegistrado(
    idPago = idPago,
    referencia = referencia,
    estado = EstadoPago.desde(estado),
    saldoPendiente = saldoPendiente,
    estadoFactura = EstadoFactura.desde(estadoFactura)
)
