package com.example.a3df_studio_app.ui.viewmodels

import com.example.a3df_studio_app.ui.screens.actividad.ActividadViewModel
import com.example.a3df_studio_app.ui.screens.actividad.CotizacionDetalleViewModel
import com.example.a3df_studio_app.ui.screens.actividad.FacturaDetalleViewModel
import com.example.a3df_studio_app.ui.screens.actividad.PedidoDetalleViewModel
import com.example.a3df_studio_app.ui.screens.auth.AuthViewModel
import com.example.a3df_studio_app.ui.screens.carrito.CarritoViewModel
import com.example.a3df_studio_app.ui.screens.catalogo.CatalogoViewModel
import com.example.a3df_studio_app.ui.screens.catalogo.DetalleCatalogoViewModel
import com.example.a3df_studio_app.ui.screens.cotizar.CotizarViewModel
import com.example.a3df_studio_app.ui.screens.cuenta.CuentaViewModel
import com.example.a3df_studio_app.ui.screens.home.HomeViewModel
import com.example.a3df_studio_app.ui.screens.seguimiento.NotificacionesViewModel
import com.example.a3df_studio_app.ui.screens.seguimiento.SoporteViewModel
import com.example.a3df_studio_app.ui.viewmodels.main.MainViewModel

class ForgeViewModels(
    val main: MainViewModel,
    val auth: AuthViewModel,
    val home: HomeViewModel,
    val catalogo: CatalogoViewModel,
    val detalleCatalogo: DetalleCatalogoViewModel,
    val carrito: CarritoViewModel,
    val actividad: ActividadViewModel,
    val pedidoDetalle: PedidoDetalleViewModel,
    val cotizacionDetalle: CotizacionDetalleViewModel,
    val facturaDetalle: FacturaDetalleViewModel,
    val cotizar: CotizarViewModel,
    val notificaciones: NotificacionesViewModel,
    val soporte: SoporteViewModel,
    val cuenta: CuentaViewModel
)
