package com.example.a3df_studio_app.data.remote.supabase

object Tables {
    const val CLIENTES = "clientes"
    const val CATEGORIAS_SERVICIOS = "categorias_servicios"
    const val SERVICIOS = "servicios"
    const val PRODUCTOS = "productos"
    const val COTIZACIONES = "cotizaciones"
    const val ENCARGOS = "encargos"
    const val FACTURAS = "facturas"
    const val PAGOS = "pagos"
    const val PROMOCIONES = "promociones"
    const val RESENAS = "resenas"
    const val SOLICITUDES_SOPORTE = "solicitudes_soporte"
    const val CONFIGURACION_NEGOCIO = "configuracion_negocio"
}

object Rpc {
    const val ASEGURAR_CLIENTE = "app_asegurar_cliente"
    const val CREAR_COTIZACION = "app_crear_cotizacion"
    const val CREAR_PEDIDO = "app_crear_pedido"
    const val REGISTRAR_PAGO_SIMULADO = "app_registrar_pago_simulado"
    const val REGISTRAR_INTERACCION = "app_registrar_interaccion"
}
