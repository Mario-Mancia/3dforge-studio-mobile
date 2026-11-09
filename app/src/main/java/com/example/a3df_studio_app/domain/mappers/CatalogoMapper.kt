package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.remote.dto.CategoriaServicioDto
import com.example.a3df_studio_app.data.remote.dto.ConfiguracionNegocioDto
import com.example.a3df_studio_app.data.remote.dto.ProductoDto
import com.example.a3df_studio_app.data.remote.dto.PromocionDto
import com.example.a3df_studio_app.data.remote.dto.ServicioDto
import com.example.a3df_studio_app.domain.models.CategoriaServicio
import com.example.a3df_studio_app.domain.models.NegocioInfo
import com.example.a3df_studio_app.domain.models.Producto
import com.example.a3df_studio_app.domain.models.Promocion
import com.example.a3df_studio_app.domain.models.Servicio
import com.example.a3df_studio_app.domain.models.TipoDescuento

fun ProductoDto.toDomain(): Producto = Producto(
    id = id,
    sku = sku,
    nombre = nombre,
    descripcion = descripcion,
    precio = precio,
    stock = stock,
    imagenUrl = imagen?.takeIf { it.isNotBlank() }
)

fun CategoriaServicioDto.toDomain(): CategoriaServicio = CategoriaServicio(
    id = id,
    nombre = nombre,
    descripcion = descripcion
)

fun ServicioDto.toDomain(): Servicio = Servicio(
    id = id,
    idCategoria = idCategoria,
    categoriaNombre = categoria?.nombre,
    nombre = nombre,
    descripcion = descripcion,
    precioBase = precioBase,
    requiereArchivo = requiereArchivo
)

fun PromocionDto.toDomain(): Promocion = Promocion(
    id = id,
    nombre = nombre,
    descripcion = descripcion,
    tipoDescuento = TipoDescuento.desde(tipoDescuento),
    valorDescuento = valorDescuento,
    fechaFin = fechaFin.toFecha()
)

fun ConfiguracionNegocioDto.toDomain(): NegocioInfo = NegocioInfo(
    nombre = nombreNegocio,
    correo = correo,
    telefono = telefono,
    direccion = direccion,
    sitioWeb = sitioWeb,
    logoUrl = logo
)
