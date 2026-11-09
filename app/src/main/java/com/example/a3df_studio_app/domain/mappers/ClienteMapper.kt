package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.remote.dto.ClienteDto
import com.example.a3df_studio_app.domain.models.Cliente
import com.example.a3df_studio_app.domain.models.TipoCliente

fun ClienteDto.toDomain(): Cliente = Cliente(
    id = id,
    tipo = TipoCliente.desde(tipoCliente),
    nombres = nombres,
    apellidos = apellidos,
    nombreEmpresa = nombreEmpresa,
    correo = correo,
    telefono = telefono,
    identificacionFiscal = identificacionFiscal,
    direccion = direccion,
    fotoUrl = fotoUrl
)
