package com.example.a3df_studio_app.domain.utils

import com.example.a3df_studio_app.domain.models.ItemCarrito
import com.example.a3df_studio_app.domain.models.TotalesCarrito
import java.math.BigDecimal
import java.math.RoundingMode

private val TASA = BigDecimal("0.13")

/** Estimación local (13 %). El valor real lo calcula el servidor al validar y al confirmar. */
fun calcularTotales(items: List<ItemCarrito>): TotalesCarrito {
    var subtotal = BigDecimal.ZERO
    var impuesto = BigDecimal.ZERO
    items.forEach { item ->
        val linea = item.subtotal.setScale(2, RoundingMode.HALF_UP)
        subtotal = subtotal.add(linea)
        impuesto = impuesto.add(linea.multiply(TASA).setScale(2, RoundingMode.HALF_UP))
    }
    return TotalesCarrito(subtotal = subtotal, impuesto = impuesto, total = subtotal.add(impuesto))
}
