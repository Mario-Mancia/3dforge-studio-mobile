package com.example.a3df_studio_app.ui.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.a3df_studio_app.ui.components.bottombar.ForgeBottomBar
import com.example.a3df_studio_app.ui.components.topbar.BackTopBar
import com.example.a3df_studio_app.ui.components.topbar.ForgeTopBar
import com.example.a3df_studio_app.ui.screens.auth.LoginScreen
import com.example.a3df_studio_app.ui.screens.auth.RegisterScreen
import com.example.a3df_studio_app.ui.screens.auth.VerifyEmailScreen
import com.example.a3df_studio_app.ui.screens.cotizaciones.CotizacionesScreen
import com.example.a3df_studio_app.ui.screens.feedback.FeedbackScreen
import com.example.a3df_studio_app.ui.screens.home.HomeScreen
import com.example.a3df_studio_app.ui.screens.pago.PagoScreen
import com.example.a3df_studio_app.ui.screens.pedidos.EncargoDetalleScreen
import com.example.a3df_studio_app.ui.screens.pedidos.PedidosScreen
import com.example.a3df_studio_app.ui.screens.perfil.PerfilScreen
import com.example.a3df_studio_app.ui.screens.productos.ProductosScreen
import com.example.a3df_studio_app.ui.screens.splash.SplashScreen
import com.example.a3df_studio_app.ui.viewmodels.ForgeViewModels

private val rutasPrincipales = bottomItems.map { it.screen.route }.toSet()

private val titulos = mapOf(
    Screen.Login.route to "Iniciar sesión",
    Screen.Registro.route to "Registro",
    Screen.Verificar.route to "Verificación",
    Screen.Perfil.route to "Mi perfil",
    Screen.Pago.route to "Pago en línea",
    Screen.EncargoDetalle.route to "Detalle del encargo"
)

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModels: ForgeViewModels
) {
    val mainUiState by viewModels.main.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val entrada by navController.currentBackStackEntryAsState()
    val rutaActual = entrada?.destination?.route
    var pestanaCotizacion by rememberSaveable { mutableIntStateOf(0) }

    val irA: (Screen) -> Unit = { destino ->
        navController.navigate(destino.route) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }
    val requerirLogin: () -> Unit = { navController.navigate(Screen.Login.route) { launchSingleTop = true } }
    val salirDeAuth: () -> Unit = {
        if (!navController.popBackStack(Screen.Login.route, inclusive = true) &&
            !navController.popBackStack(Screen.Registro.route, inclusive = true)
        ) {
            irA(Screen.Home)
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            when (rutaActual) {
                null, Screen.Splash.route -> Unit
                in rutasPrincipales -> ForgeTopBar(
                    usuario = mainUiState.usuario,
                    avatarUrl = mainUiState.avatarUrl,
                    onLogoClick = { irA(Screen.Home) },
                    onAvatarClick = { if (mainUiState.isLoggedIn) navController.navigate(Screen.Perfil.route) else requerirLogin() }
                )
                else -> BackTopBar(titulos[rutaActual].orEmpty()) { navController.popBackStack() }
            }
        },
        bottomBar = {
            if (rutaActual in rutasPrincipales) ForgeBottomBar(rutaActual, irA)
        }
    ) { padding ->
        Box(Modifier.fillMaxSize().padding(padding)) {
            NavHost(navController = navController, startDestination = Screen.Splash.route) {

                composable(Screen.Splash.route) {
                    SplashScreen(authState = mainUiState.authState) {
                        navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                    }
                }

                composable(Screen.Home.route) {
                    HomeScreen(
                        homeViewModel = viewModels.home,
                        usuario = mainUiState.usuario,
                        onCotizarServicio = { servicio ->
                            viewModels.cotizaciones.agregarServicio(servicio)
                            pestanaCotizacion = 0
                            irA(Screen.Cotizaciones)
                        },
                        onAsesoria = {
                            pestanaCotizacion = 1
                            irA(Screen.Cotizaciones)
                        },
                        onVerProductos = { irA(Screen.Productos) }
                    )
                }

                composable(Screen.Productos.route) {
                    ProductosScreen(
                        productosViewModel = viewModels.productos,
                        isLoggedIn = mainUiState.isLoggedIn,
                        snackbarHostState = snackbarHostState,
                        onRequireLogin = requerirLogin,
                        onPagar = { idFactura ->
                            viewModels.pedidos.cargar()
                            navController.navigate(Screen.Pago.crear(idFactura))
                        },
                        onVerPedidos = { irA(Screen.Pedidos) }
                    )
                }

                composable(Screen.Pedidos.route) {
                    PedidosScreen(
                        pedidosViewModel = viewModels.pedidos,
                        isLoggedIn = mainUiState.isLoggedIn,
                        onRequireLogin = requerirLogin,
                        onEncargo = { navController.navigate(Screen.EncargoDetalle.crear(it)) },
                        onPagar = { navController.navigate(Screen.Pago.crear(it)) },
                        onIrProductos = { irA(Screen.Productos) }
                    )
                }

                composable(Screen.Cotizaciones.route) {
                    CotizacionesScreen(
                        cotizacionesViewModel = viewModels.cotizaciones,
                        isLoggedIn = mainUiState.isLoggedIn,
                        snackbarHostState = snackbarHostState,
                        pestanaInicial = pestanaCotizacion,
                        onRequireLogin = requerirLogin
                    )
                }

                composable(Screen.Feedback.route) {
                    FeedbackScreen(
                        feedbackViewModel = viewModels.feedback,
                        isLoggedIn = mainUiState.isLoggedIn,
                        snackbarHostState = snackbarHostState,
                        onRequireLogin = requerirLogin
                    )
                }

                composable(Screen.Login.route) {
                    LoginScreen(
                        authViewModel = viewModels.auth,
                        onAutenticado = salirDeAuth,
                        onVerificar = { navController.navigate(Screen.Verificar.crear(it)) },
                        onRegistro = { navController.navigate(Screen.Registro.route) }
                    )
                }

                composable(Screen.Registro.route) {
                    RegisterScreen(
                        authViewModel = viewModels.auth,
                        onAutenticado = salirDeAuth,
                        onVerificar = { navController.navigate(Screen.Verificar.crear(it)) },
                        onLogin = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.Verificar.route,
                    arguments = listOf(navArgument("correo") { type = NavType.StringType })
                ) { backStack ->
                    VerifyEmailScreen(
                        authViewModel = viewModels.auth,
                        correo = backStack.arguments?.getString("correo").orEmpty(),
                        onVerificado = salirDeAuth
                    )
                }

                composable(Screen.Perfil.route) {
                    PerfilScreen(
                        perfilViewModel = viewModels.perfil,
                        usuario = mainUiState.usuario,
                        avatarUrl = mainUiState.avatarUrl,
                        snackbarHostState = snackbarHostState,
                        onLogout = {
                            viewModels.pedidos.limpiar()
                            navController.navigate(Screen.Home.route) { popUpTo(0) { inclusive = true } }
                        }
                    )
                }

                composable(
                    route = Screen.Pago.route,
                    arguments = listOf(navArgument("idFactura") { type = NavType.LongType })
                ) { backStack ->
                    PagoScreen(
                        idFactura = backStack.arguments?.getLong("idFactura") ?: 0L,
                        pedidosViewModel = viewModels.pedidos,
                        pagoViewModel = viewModels.pago,
                        onFinalizado = { navController.popBackStack() }
                    )
                }

                composable(
                    route = Screen.EncargoDetalle.route,
                    arguments = listOf(navArgument("idEncargo") { type = NavType.LongType })
                ) { backStack ->
                    EncargoDetalleScreen(
                        pedidosViewModel = viewModels.pedidos,
                        idEncargo = backStack.arguments?.getLong("idEncargo") ?: 0L
                    )
                }
            }
        }
    }
}
