package com.example.a3df_studio_app.ui.viewmodels

import com.example.a3df_studio_app.ui.screens.auth.AuthViewModel
import com.example.a3df_studio_app.ui.screens.cotizaciones.CotizacionesViewModel
import com.example.a3df_studio_app.ui.screens.feedback.FeedbackViewModel
import com.example.a3df_studio_app.ui.screens.home.HomeViewModel
import com.example.a3df_studio_app.ui.screens.pago.PagoViewModel
import com.example.a3df_studio_app.ui.screens.pedidos.PedidosViewModel
import com.example.a3df_studio_app.ui.screens.perfil.PerfilViewModel
import com.example.a3df_studio_app.ui.screens.productos.ProductosViewModel
import com.example.a3df_studio_app.ui.viewmodels.main.MainViewModel

data class ForgeViewModels(
    val main: MainViewModel,
    val home: HomeViewModel,
    val productos: ProductosViewModel,
    val pedidos: PedidosViewModel,
    val pago: PagoViewModel,
    val cotizaciones: CotizacionesViewModel,
    val feedback: FeedbackViewModel,
    val auth: AuthViewModel,
    val perfil: PerfilViewModel
)
