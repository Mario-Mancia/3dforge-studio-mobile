package com.example.a3df_studio_app.domain.mappers

import com.example.a3df_studio_app.data.remote.dto.Estimacion3dDto
import com.example.a3df_studio_app.data.remote.dto.Estimar3dRespuestaDto
import com.example.a3df_studio_app.data.remote.dto.OpcionTextoDto
import com.example.a3df_studio_app.data.remote.dto.Opciones3dDto
import com.example.a3df_studio_app.domain.models.Estimacion3d
import com.example.a3df_studio_app.domain.models.Filamento3d
import com.example.a3df_studio_app.domain.models.OpcionTexto
import com.example.a3df_studio_app.domain.models.Opciones3d
import com.example.a3df_studio_app.domain.models.Resultado3d
import com.example.a3df_studio_app.domain.utils.aDinero

private fun OpcionTextoDto.aDominio(): OpcionTexto = OpcionTexto(codigo = codigo, texto = texto.ifBlank { codigo })

fun Opciones3dDto.aDominio(): Opciones3d = Opciones3d(
    disponible = disponible,
    filamentos = filamentos.map { Filamento3d(id = it.id, nombre = it.nombre.ifBlank { "${it.material} ${it.color}".trim() }) },
    rellenos = rellenos,
    calidades = calidades.map { it.aDominio() },
    escalas = escalas,
    unidades = unidades.map { it.aDominio() },
    soportes = soportes.map { it.aDominio() },
    cantidadMaxima = cantidadMaxima,
    formatos = formatos,
    tamanoMaximoMb = tamanoMaximoMb,
    camaMm = camaMm,
    aviso = aviso
)

fun Estimacion3dDto.aDominio(): Estimacion3d = Estimacion3d(
    cabe = cabe,
    mensajeCabida = mensajeCabida,
    dimensionesTexto = dimensionesTexto,
    gramos = gramos,
    tiempoTexto = tiempoTexto,
    soportes = soportes,
    confianza = confianza,
    confianzaTexto = confianzaTexto.ifBlank { confianza },
    advertencias = advertencias,
    cantidad = cantidad,
    precioUnitario = precioUnitario.aDinero(),
    subtotal = subtotal.aDinero(),
    impuesto = impuesto.aDinero(),
    total = total.aDinero(),
    aviso = aviso
)

fun Estimar3dRespuestaDto.aDominio(): Resultado3d = Resultado3d(analisisJson = analisis.toString(), estimacion = estimacion.aDominio())
