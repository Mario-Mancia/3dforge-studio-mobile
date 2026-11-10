package com.example.a3df_studio_app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.rememberNavController
import com.example.a3df_studio_app.data.di.AppContainer
import com.example.a3df_studio_app.ui.navigation.AppNavHost
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
import com.example.a3df_studio_app.ui.theme.ForgeStudioTheme
import com.example.a3df_studio_app.ui.viewmodels.ForgeViewModelFactory
import com.example.a3df_studio_app.ui.viewmodels.ForgeViewModels
import com.example.a3df_studio_app.ui.viewmodels.main.MainViewModel

class MainActivity : ComponentActivity() {

    private inline fun <reified VM : ViewModel> crearViewModel(noinline crear: () -> VM): VM =
        ViewModelProvider(this, ForgeViewModelFactory(crear))[VM::class.java]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppContainer.initialize(applicationContext)
        val c = AppContainer

        val main = crearViewModel {
            MainViewModel(c.observeAuthStateUseCase, c.restoreSessionUseCase, c.getUnreadCountUseCase, c.getNegocioUseCase, c.observeCartUseCase)
        }

        val viewModels = ForgeViewModels(
            main = main,
            auth = crearViewModel {
                AuthViewModel(
                    c.loginUseCase, c.loginWithGoogleUseCase, c.registerUseCase,
                    c.resendConfirmationUseCase, c.recoverPasswordUseCase, c.getLastEmailUseCase
                )
            },
            home = crearViewModel { HomeViewModel(c.getNegocioUseCase, c.getProductosUseCase, c.getServiciosUseCase, c.getPanelUseCase) },
            catalogo = crearViewModel { CatalogoViewModel(c.getProductosUseCase, c.getServiciosUseCase, c.getCategoriasUseCase, c.addToCartUseCase) },
            detalleCatalogo = crearViewModel { DetalleCatalogoViewModel(c.getProductoUseCase, c.getServicioUseCase, c.addToCartUseCase) },
            carrito = crearViewModel {
                CarritoViewModel(
                    c.observeCartUseCase, c.updateCartQuantityUseCase, c.removeFromCartUseCase,
                    c.clearCartUseCase, c.validateCartUseCase, c.confirmPurchaseUseCase
                )
            },
            actividad = crearViewModel { ActividadViewModel(c.listOrdersUseCase, c.listQuotesUseCase, c.listInvoicesUseCase) },
            pedidoDetalle = crearViewModel { PedidoDetalleViewModel(c.getOrderUseCase, c.cancelOrderUseCase, c.downloadFileUseCase) },
            cotizacionDetalle = crearViewModel { CotizacionDetalleViewModel(c.getQuoteUseCase, c.respondQuoteUseCase, c.downloadFileUseCase) },
            facturaDetalle = crearViewModel { FacturaDetalleViewModel(c.getInvoiceUseCase) },
            cotizar = crearViewModel { CotizarViewModel(c.getServiciosUseCase, c.requestQuoteUseCase) },
            notificaciones = crearViewModel {
                NotificacionesViewModel(
                    c.listNotificationsUseCase, c.markNotificationReadUseCase, c.markAllNotificationsReadUseCase, main::fijarNoLeidas
                )
            },
            soporte = crearViewModel { SoporteViewModel(c.listSupportUseCase, c.getSupportUseCase, c.createSupportUseCase, c.closeSupportUseCase) },
            cuenta = crearViewModel {
                CuentaViewModel(c.getProfileUseCase, c.updateProfileUseCase, c.changePasswordUseCase, c.logoutUseCase, c.getNegocioUseCase)
            }
        )

        enableEdgeToEdge()
        setContent {
            ForgeStudioTheme {
                ForgeStudioApplication(viewModels)
            }
        }
    }
}

@Composable
fun ForgeStudioApplication(viewModels: ForgeViewModels) {
    val navController = rememberNavController()
    AppNavHost(navController = navController, viewModels = viewModels)
}
