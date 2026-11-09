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
import com.example.a3df_studio_app.ui.screens.auth.AuthViewModel
import com.example.a3df_studio_app.ui.screens.cotizaciones.CotizacionesViewModel
import com.example.a3df_studio_app.ui.screens.feedback.FeedbackViewModel
import com.example.a3df_studio_app.ui.screens.home.HomeViewModel
import com.example.a3df_studio_app.ui.screens.pago.PagoViewModel
import com.example.a3df_studio_app.ui.screens.pedidos.PedidosViewModel
import com.example.a3df_studio_app.ui.screens.perfil.PerfilViewModel
import com.example.a3df_studio_app.ui.screens.productos.ProductosViewModel
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

        val viewModels = ForgeViewModels(
            main = crearViewModel {
                MainViewModel(c.observeAuthStateUseCase, c.observeClienteProfileUseCase, c.getClienteProfileUseCase)
            },
            home = crearViewModel {
                HomeViewModel(c.getNegocioInfoUseCase, c.getPromocionesActivasUseCase, c.getCategoriasServiciosUseCase, c.getServiciosUseCase)
            },
            productos = crearViewModel { ProductosViewModel(c.getProductosUseCase, c.createProductOrderUseCase) },
            pedidos = crearViewModel { PedidosViewModel(c.getMisEncargosUseCase, c.getMisFacturasUseCase, c.getMisPagosUseCase) },
            pago = crearViewModel { PagoViewModel(c.processSimulatedPaymentUseCase) },
            cotizaciones = crearViewModel {
                CotizacionesViewModel(c.getServiciosUseCase, c.requestQuoteUseCase, c.requestAdvisoryUseCase, c.getMisCotizacionesUseCase)
            },
            feedback = crearViewModel {
                FeedbackViewModel(
                    c.getServiciosUseCase, c.getMisEncargosUseCase, c.getMisResenasUseCase,
                    c.getMisSolicitudesSoporteUseCase, c.submitResenaUseCase, c.submitSupportRequestUseCase
                )
            },
            auth = crearViewModel {
                AuthViewModel(
                    c.loginWithEmailUseCase, c.registerWithEmailUseCase, c.verifyEmailCodeUseCase,
                    c.resendVerificationCodeUseCase, c.signInWithGoogleUseCase, c.sendPasswordResetUseCase,
                    c.getLastEmailUseCase
                )
            },
            perfil = crearViewModel { PerfilViewModel(c.getClienteProfileUseCase, c.updateClienteProfileUseCase, c.logoutUseCase) }
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
