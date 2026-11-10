package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.local.SesionLocal
import com.example.a3df_studio_app.data.remote.dto.ArchivoDto
import com.example.a3df_studio_app.data.remote.dto.CarritoValidadoDto
import com.example.a3df_studio_app.data.remote.dto.CategoriaDto
import com.example.a3df_studio_app.data.remote.dto.ClienteDto
import com.example.a3df_studio_app.data.remote.dto.CotizacionDto
import com.example.a3df_studio_app.data.remote.dto.EncargoDto
import com.example.a3df_studio_app.data.remote.dto.FacturaDto
import com.example.a3df_studio_app.data.remote.dto.KpisDto
import com.example.a3df_studio_app.data.remote.dto.LineaDocDto
import com.example.a3df_studio_app.data.remote.dto.NegocioDto
import com.example.a3df_studio_app.data.remote.dto.NotificacionDto
import com.example.a3df_studio_app.data.remote.dto.PagoDto
import com.example.a3df_studio_app.data.remote.dto.PaginaDto
import com.example.a3df_studio_app.data.remote.dto.ProductoDto
import com.example.a3df_studio_app.data.remote.dto.ServicioDto
import com.example.a3df_studio_app.data.remote.dto.SesionDto
import com.example.a3df_studio_app.data.remote.dto.SoporteDto
import com.example.a3df_studio_app.domain.models.ArchivoAdjunto
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.domain.models.CarritoValidado
import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.Cotizacion
import com.example.a3df_studio_app.domain.models.DetalleDocumento
import com.example.a3df_studio_app.domain.models.Disponibilidad
import com.example.a3df_studio_app.domain.models.Encargo
import com.example.a3df_studio_app.domain.models.EstadoCotizacion
import com.example.a3df_studio_app.domain.models.EstadoEncargo
import com.example.a3df_studio_app.domain.models.EstadoFactura
import com.example.a3df_studio_app.domain.models.EstadoSoporte
import com.example.a3df_studio_app.domain.models.Factura
import com.example.a3df_studio_app.domain.models.HistorialEstado
import com.example.a3df_studio_app.domain.models.InfoNegocio
import com.example.a3df_studio_app.domain.models.LineaValidada
import com.example.a3df_studio_app.domain.models.Notificacion
import com.example.a3df_studio_app.domain.models.Pagina
import com.example.a3df_studio_app.domain.models.Pago
import com.example.a3df_studio_app.domain.models.PrioridadSoporte
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.models.ResumenPanel
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.models.SolicitudSoporte
import com.example.a3df_studio_app.domain.models.TipoCliente
import com.example.a3df_studio_app.domain.models.TipoNotificacion
import com.example.a3df_studio_app.domain.utils.aDinero
import com.example.a3df_studio_app.domain.utils.formatearFecha
import com.example.a3df_studio_app.domain.utils.formatearFechaHora
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonPrimitive

// ---- Cuenta ----

fun SesionDto.aSesionLocal(): SesionLocal = SesionLocal(
    usuarioId = usuario.id,
    clienteId = cliente.id,
    correo = usuario.correo,
    nombres = usuario.nombres,
    apellidos = usuario.apellidos,
    expiraEn = expiraEn
)

fun SesionLocal.aUsuario(): AuthUser = AuthUser(id = usuarioId, correo = correo, nombres = nombres, apellidos = apellidos)

fun ClienteDto.aDominio(): Cliente = Cliente(
    id = id,
    tipo = TipoCliente.desde(tipoCliente),
    nombreVisible = nombreVisible,
    nombres = nombres.orEmpty(),
    apellidos = apellidos.orEmpty(),
    nombreEmpresa = nombreEmpresa.orEmpty(),
    correo = correo.orEmpty(),
    telefono = telefono.orEmpty(),
    identificacionFiscal = identificacionFiscal.orEmpty(),
    direccion = direccion.orEmpty()
)

fun NegocioDto.aDominio(): InfoNegocio = InfoNegocio(
    nombre = nombre.ifBlank { "3DForge Studio" },
    correo = correo?.takeIf { it.isNotBlank() },
    telefono = telefono?.takeIf { it.isNotBlank() },
    direccion = direccion?.takeIf { it.isNotBlank() },
    sitioWeb = sitioWeb?.takeIf { it.isNotBlank() },
    logoUrl = logoUrl?.takeIf { it.isNotBlank() }
)

fun KpisDto.aDominio(): ResumenPanel = ResumenPanel(
    porResponder = porResponder,
    enRevision = enRevision,
    pedidosEnCurso = pedidosEnCurso,
    saldoPendiente = saldoPendiente.aDinero(),
    soporteAbierto = soporteAbierto
)

fun <D, T> PaginaDto<D>.aPagina(transformar: (D) -> T): Pagina<T> =
    Pagina(items = resultados.map(transformar), total = total, pagina = pagina, hayMas = hayMas)

// ---- Catálogo ----

fun ProductoDto.aDominio(): Producto = Producto(
    id = id,
    sku = sku,
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    precio = precio.aDinero(),
    stock = stock,
    stockMinimo = stockMinimo,
    disponibilidad = Disponibilidad.desde(disponibilidad),
    imagenUrl = imagenUrl?.takeIf { it.isNotBlank() }
)

fun CategoriaDto.aDominio(): CategoriaServicio = CategoriaServicio(id = id, nombre = nombre, descripcion = descripcion.orEmpty())

fun ServicioDto.aDominio(): Servicio = Servicio(
    id = id,
    categoriaId = categoriaId,
    categoria = categoria,
    nombre = nombre,
    descripcion = descripcion.orEmpty(),
    precioBase = precioBase.aDinero(),
    requiereArchivo = requiereArchivo
)

fun CarritoValidadoDto.aDominio(): CarritoValidado = CarritoValidado(
    lineas = lineas.map { linea ->
        LineaValidada(
            productoId = linea.productoId,
            nombre = linea.producto?.nombre.orEmpty().ifBlank { "Producto #${linea.productoId}" },
            cantidad = linea.cantidad,
            disponible = linea.disponible,
            stock = linea.producto?.stock ?: 0,
            precio = linea.producto?.precio?.aDinero()
        )
    },
    subtotal = totales.subtotal.aDinero(),
    descuento = totales.descuento.aDinero(),
    impuesto = totales.impuesto.aDinero(),
    total = totales.total.aDinero(),
    hayProblemas = hayProblemas
)

// ---- Documentos ----

fun ArchivoDto.aDominio(): ArchivoAdjunto = ArchivoAdjunto(
    id = id,
    nombre = nombre,
    formato = formato,
    tamanoBytes = tamanoBytes,
    fecha = formatearFecha(fechaSubida)
)

private fun JsonElement.comoTexto(): String = when (this) {
    is JsonNull -> ""
    is JsonPrimitive -> content
    else -> toString()
}

fun LineaDocDto.aDominio(): DetalleDocumento = DetalleDocumento(
    id = id,
    descripcion = descripcion,
    especificaciones = especificaciones.mapValues { entrada -> entrada.value.comoTexto() },
    cantidad = cantidad,
    precioUnitario = precioUnitario.aDinero(),
    descuento = descuento.aDinero(),
    impuesto = impuesto.aDinero(),
    subtotal = subtotal.aDinero(),
    archivos = archivos.map { it.aDominio() }
)

fun CotizacionDto.aDominio(): Cotizacion = Cotizacion(
    id = id,
    codigo = codigo,
    estado = EstadoCotizacion.desde(estado),
    estadoTexto = estadoTexto.ifBlank { EstadoCotizacion.desde(estado).etiqueta },
    fechaEmision = formatearFecha(fechaEmision),
    fechaVencimiento = fechaVencimiento?.let { formatearFecha(it) },
    plazoEntregaDias = plazoEntregaDias,
    subtotal = subtotal.aDinero(),
    descuento = descuento.aDinero(),
    impuesto = impuesto.aDinero(),
    total = total.aDinero(),
    vencida = vencida,
    puedeResponder = puedeResponder,
    puedeCancelar = puedeCancelar,
    solicitud = solicitud.orEmpty(),
    observaciones = observaciones.orEmpty(),
    lineas = lineas.map { it.aDominio() },
    archivos = archivos.map { it.aDominio() },
    pedidoId = pedido?.id,
    pedidoCodigo = pedido?.codigo
)

fun EncargoDto.aDominio(): Encargo = Encargo(
    id = id,
    codigo = codigo,
    estado = EstadoEncargo.desde(estado),
    estadoTexto = estadoTexto.ifBlank { EstadoEncargo.desde(estado).etiqueta },
    progreso = progreso,
    fechaSolicitud = formatearFecha(fechaSolicitud),
    fechaEntregaEstimada = fechaEntregaEstimada?.let { formatearFecha(it) },
    fechaEntregaReal = fechaEntregaReal?.let { formatearFecha(it) },
    descripcion = descripcion.orEmpty(),
    cotizacionId = cotizacionId,
    subtotal = subtotal.aDinero(),
    descuento = descuento.aDinero(),
    impuesto = impuesto.aDinero(),
    total = total.aDinero(),
    puedeCancelar = puedeCancelar,
    responsable = responsable?.takeIf { it.isNotBlank() },
    lineas = lineas.map { it.aDominio() },
    historial = historial.map {
        HistorialEstado(estadoNuevoTexto = it.estadoNuevoTexto, comentario = it.comentario.orEmpty(), fecha = formatearFechaHora(it.fecha))
    }
)

fun PagoDto.aDominio(): Pago = Pago(
    id = id,
    monto = monto.aDinero(),
    fecha = formatearFecha(fechaPago),
    referencia = referencia.orEmpty(),
    metodo = metodo
)

fun FacturaDto.aDominio(): Factura = Factura(
    id = id,
    numero = numero,
    estado = EstadoFactura.desde(estado),
    estadoTexto = estadoTexto.ifBlank { EstadoFactura.desde(estado).etiqueta },
    fechaEmision = formatearFecha(fechaEmision),
    fechaVencimiento = fechaVencimiento?.let { formatearFecha(it) },
    total = total.aDinero(),
    saldoPendiente = saldoPendiente.aDinero(),
    pedidoId = pedidoId,
    nombreFacturacion = nombreFacturacion.orEmpty(),
    identificacionFiscal = identificacionFiscal.orEmpty(),
    direccionFacturacion = direccionFacturacion.orEmpty(),
    subtotal = subtotal.aDinero(),
    impuesto = impuesto.aDinero(),
    lineas = lineas.map { it.aDominio() },
    pagos = pagos.map { it.aDominio() },
    aviso = aviso.orEmpty()
)

// ---- Seguimiento ----

fun NotificacionDto.aDominio(): Notificacion = Notificacion(
    id = id,
    titulo = titulo,
    mensaje = mensaje,
    tipo = TipoNotificacion.desde(tipo),
    referenciaId = referenciaId,
    leida = leida,
    fecha = formatearFechaHora(fecha)
)

fun SoporteDto.aDominio(): SolicitudSoporte = SolicitudSoporte(
    id = id,
    asunto = asunto,
    descripcion = descripcion,
    prioridad = PrioridadSoporte.desde(prioridad),
    estado = EstadoSoporte.desde(estado),
    estadoTexto = estadoTexto.ifBlank { EstadoSoporte.desde(estado).etiqueta },
    creada = formatearFechaHora(creadaEn),
    actualizada = formatearFechaHora(actualizadaEn)
)
