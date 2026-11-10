package com.example.a3df_studio_app.ui.navigation

import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import com.example.a3df_studio_app.data.config.AppConfig
import com.example.a3df_studio_app.domain.models.AuthState
import com.example.a3df_studio_app.ui.components.bottombar.ForgeBottomBar
import com.example.a3df_studio_app.ui.components.dialog.AuthGateDialog
import com.example.a3df_studio_app.ui.components.topbar.ForgeTopBar
import com.example.a3df_studio_app.ui.screens.actividad.ActividadScreen
import com.example.a3df_studio_app.ui.screens.actividad.CotizacionDetalleScreen
import com.example.a3df_studio_app.ui.screens.actividad.FacturaDetalleScreen
import com.example.a3df_studio_app.ui.screens.actividad.PedidoDetalleScreen
import com.example.a3df_studio_app.ui.screens.auth.LoginScreen
import com.example.a3df_studio_app.ui.screens.auth.RecuperarScreen
import com.example.a3df_studio_app.ui.screens.auth.RegisterScreen
import com.example.a3df_studio_app.ui.screens.carrito.CarritoScreen
import com.example.a3df_studio_app.ui.screens.catalogo.CatalogoScreen
import com.example.a3df_studio_app.ui.screens.catalogo.ProductoDetalleScreen
import com.example.a3df_studio_app.ui.screens.catalogo.ServicioDetalleScreen
import com.example.a3df_studio_app.ui.screens.cotizar.CotizarScreen
import com.example.a3df_studio_app.ui.screens.cuenta.CuentaScreen
import com.example.a3df_studio_app.ui.screens.home.HomeScreen
import com.example.a3df_studio_app.ui.screens.seguimiento.NotificacionesScreen
import com.example.a3df_studio_app.ui.screens.seguimiento.SoporteDetalleScreen
import com.example.a3df_studio_app.ui.screens.seguimiento.SoporteNuevoScreen
import com.example.a3df_studio_app.ui.screens.seguimiento.SoporteScreen
import com.example.a3df_studio_app.ui.screens.seguimiento.rutaDeNotificacion
import com.example.a3df_studio_app.ui.screens.splash.SplashScreen
import com.example.a3df_studio_app.ui.viewmodels.ForgeViewModels

private val argumentoId = listOf(navArgument("id") { type = NavType.LongType })
private val argumentoRedirect = listOf(
    navArgument("redirect") {
        type = NavType.StringType
        nullable = true
        defaultValue = null
    }
)

private fun NavBackStackEntry.id(): Long = arguments?.getLong("id") ?: 0L

private fun NavBackStackEntry.redirect(): String? = arguments?.getString("redirect")?.let { Uri.decode(it) }

/** Acción que requiere sesión: [motivo] se muestra al usuario y [redirect] es la ruta a la que volver después. */
private data class AccionConSesion(val motivo: String, val redirect: String)

@Composable
fun AppNavHost(
    navController: NavHostController,
    viewModels: ForgeViewModels
) {
    val authState by viewModels.main.authState.collectAsStateWithLifecycle()
    val usuario = (authState as? AuthState.ConSesion)?.usuario
    val negocio by viewModels.main.negocio.collectAsStateWithLifecycle()
    val noLeidas by viewModels.main.noLeidas.collectAsStateWithLifecycle()
    val cantidadCarrito by viewModels.main.cantidadCarrito.collectAsStateWithLifecycle()
    val authUi by viewModels.auth.estado.collectAsStateWithLifecycle()
    val entradaActual by navController.currentBackStackEntryAsState()
    val rutaActual = entradaActual?.destination?.route
    val contexto = LocalContext.current
    var gate by remember { mutableStateOf<AccionConSesion?>(null) }

    // Al cerrar sesión se limpian los datos del usuario que están en memoria.
    LaunchedEffect(authState) {
        if (authState is AuthState.SinSesion) {
            viewModels.actividad.limpiar()
            viewModels.cuenta.limpiar()
            viewModels.soporte.limpiar()
            viewModels.notificaciones.limpiar()
        }
    }

    // Google desde el diálogo de "necesitas iniciar sesión".
    LaunchedEffect(authUi.exito, gate) {
        val pendiente = gate
        if (authUi.exito && pendiente != null) {
            viewModels.auth.consumirExito()
            gate = null
            navController.navigate(pendiente.redirect) { launchSingleTop = true }
        }
    }

    fun irA(ruta: String) {
        navController.navigate(ruta) { launchSingleTop = true }
    }

    fun irAPrincipal(pantalla: Screen) {
        navController.navigate(pantalla.route) {
            popUpTo(Screen.Home.route) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun requiereSesion(motivo: String, redirect: String) {
        if (usuario != null) irA(redirect) else gate = AccionConSesion(motivo, redirect)
    }

    fun volverAlLogin(redirect: String?) {
        if (redirect != null) {
            navController.navigate(redirect) {
                popUpTo(Screen.Home.route)
                launchSingleTop = true
            }
        } else {
            navController.navigate(Screen.Home.route) {
                popUpTo(Screen.Home.route) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    gate?.let { accion ->
        AuthGateDialog(
            motivo = accion.motivo,
            googleDisponible = AppConfig.isGoogleConfigured,
            cargandoGoogle = authUi.cargandoGoogle,
            error = authUi.error,
            onLogin = {
                gate = null
                irA(Screen.Login.crear(accion.redirect))
            },
            onRegistro = {
                gate = null
                irA(Screen.Registro.crear(accion.redirect))
            },
            onGoogle = { viewModels.auth.continuarConGoogle(contexto) },
            onCerrar = {
                gate = null
                viewModels.auth.limpiar()
            }
        )
    }

    val esPrincipal = rutaActual in rutasPrincipales

    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            if (esPrincipal) {
                ForgeTopBar(
                    nombreNegocio = negocio?.nombre ?: AppConfig.NOMBRE_NEGOCIO,
                    usuario = usuario,
                    noLeidas = noLeidas,
                    onLogoClick = { irAPrincipal(Screen.Home) },
                    onNotificaciones = { irA(Screen.Notificaciones.route) },
                    onAvatarClick = { irAPrincipal(Screen.Cuenta) }
                )
            }
        },
        bottomBar = {
            if (esPrincipal) {
                ForgeBottomBar(rutaActual = rutaActual, cantidadCarrito = cantidadCarrito, onNavigate = { irAPrincipal(it) })
            }
        }
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(padding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreen(
                    listo = authState !is AuthState.Cargando,
                    onListo = {
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Home.route) {
                HomeScreen(
                    viewModel = viewModels.home,
                    usuario = usuario,
                    onCatalogo = { irAPrincipal(Screen.Catalogo) },
                    onProducto = { irA(Screen.Producto.crear(it)) },
                    onServicio = { irA(Screen.Servicio.crear(it)) },
                    onCotizar = { servicioId -> requiereSesion("Para solicitar una cotización necesitas una cuenta.", Screen.Cotizar.crear(servicioId)) },
                    onAgregar = { producto ->
                        viewModels.catalogo.agregarAlCarrito(producto) { texto -> Toast.makeText(contexto, texto, Toast.LENGTH_SHORT).show() }
                    },
                    onCotizaciones = { irAPrincipal(Screen.Actividad) },
                    onFacturas = { irAPrincipal(Screen.Actividad) },
                    onSoporte = { irA(Screen.Soporte.route) },
                    onIniciarSesion = { irA(Screen.Login.crear()) }
                )
            }

            composable(Screen.Catalogo.route) {
                CatalogoScreen(
                    viewModel = viewModels.catalogo,
                    onProducto = { irA(Screen.Producto.crear(it)) },
                    onServicio = { irA(Screen.Servicio.crear(it)) },
                    onCotizar = { servicioId -> requiereSesion("Para solicitar una cotización necesitas una cuenta.", Screen.Cotizar.crear(servicioId)) }
                )
            }

            composable(Screen.Carrito.route) {
                CarritoScreen(
                    viewModel = viewModels.carrito,
                    usuario = usuario,
                    onCatalogo = { irAPrincipal(Screen.Catalogo) },
                    onRequiereSesion = { gate = AccionConSesion("Para confirmar tu compra necesitas iniciar sesión. Tu carrito se conserva.", Screen.Carrito.route) },
                    onPedidoCreado = { pedidoId ->
                        navController.navigate(Screen.Pedido.crear(pedidoId)) { launchSingleTop = true }
                    }
                )
            }

            composable(Screen.Actividad.route) {
                ActividadScreen(
                    viewModel = viewModels.actividad,
                    usuario = usuario,
                    onIniciarSesion = { irA(Screen.Login.crear(Screen.Actividad.route)) },
                    onPedido = { irA(Screen.Pedido.crear(it)) },
                    onCotizacion = { irA(Screen.Cotizacion.crear(it)) },
                    onFactura = { irA(Screen.Factura.crear(it)) },
                    onNuevaCotizacion = { irA(Screen.Cotizar.crear()) }
                )
            }

            composable(Screen.Cuenta.route) {
                CuentaScreen(
                    viewModel = viewModels.cuenta,
                    usuario = usuario,
                    negocio = negocio,
                    googleCargando = authUi.cargandoGoogle,
                    onLogin = { irA(Screen.Login.crear()) },
                    onRegistro = { irA(Screen.Registro.crear()) },
                    onGoogle = { viewModels.auth.continuarConGoogle(contexto) },
                    onNotificaciones = { irA(Screen.Notificaciones.route) },
                    onCotizaciones = { irAPrincipal(Screen.Actividad) },
                    onFacturas = { irAPrincipal(Screen.Actividad) },
                    onSoporte = { irA(Screen.Soporte.route) }
                )
            }

            composable(Screen.Login.route, arguments = argumentoRedirect) { entrada ->
                val redirect = entrada.redirect()
                LoginScreen(
                    viewModel = viewModels.auth,
                    onBack = { navController.popBackStack() },
                    onRegistro = { irA(Screen.Registro.crear(redirect)) },
                    onRecuperar = { irA(Screen.Recuperar.route) },
                    onExito = { volverAlLogin(redirect) }
                )
            }

            composable(Screen.Registro.route, arguments = argumentoRedirect) { entrada ->
                val redirect = entrada.redirect()
                RegisterScreen(
                    viewModel = viewModels.auth,
                    onBack = { navController.popBackStack() },
                    onLogin = { irA(Screen.Login.crear(redirect)) },
                    onExito = { volverAlLogin(redirect) }
                )
            }

            composable(Screen.Recuperar.route) {
                RecuperarScreen(viewModel = viewModels.auth, onBack = { navController.popBackStack() })
            }

            composable(Screen.Producto.route, arguments = argumentoId) { entrada ->
                ProductoDetalleScreen(
                    id = entrada.id(),
                    viewModel = viewModels.detalleCatalogo,
                    onBack = { navController.popBackStack() },
                    onComprarAhora = {
                        viewModels.detalleCatalogo.agregar(1)
                        irAPrincipal(Screen.Carrito)
                    }
                )
            }

            composable(Screen.Servicio.route, arguments = argumentoId) { entrada ->
                ServicioDetalleScreen(
                    id = entrada.id(),
                    viewModel = viewModels.detalleCatalogo,
                    onBack = { navController.popBackStack() },
                    onCotizar = { servicioId -> requiereSesion("Para solicitar una cotización necesitas una cuenta.", Screen.Cotizar.crear(servicioId)) }
                )
            }

            composable(
                Screen.Cotizar.route,
                arguments = listOf(navArgument("servicioId") { type = NavType.LongType; defaultValue = -1L })
            ) { entrada ->
                CotizarScreen(
                    servicioId = entrada.arguments?.getLong("servicioId")?.takeIf { it > 0 },
                    viewModel = viewModels.cotizar,
                    onBack = { navController.popBackStack() },
                    onCreada = { cotizacionId ->
                        navController.navigate(Screen.Cotizacion.crear(cotizacionId)) {
                            popUpTo(Screen.Cotizar.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.Pedido.route, arguments = argumentoId) { entrada ->
                PedidoDetalleScreen(
                    id = entrada.id(),
                    viewModel = viewModels.pedidoDetalle,
                    onBack = { navController.popBackStack() },
                    onCotizacion = { irA(Screen.Cotizacion.crear(it)) }
                )
            }

            composable(Screen.Cotizacion.route, arguments = argumentoId) { entrada ->
                CotizacionDetalleScreen(
                    id = entrada.id(),
                    viewModel = viewModels.cotizacionDetalle,
                    onBack = { navController.popBackStack() },
                    onPedido = { irA(Screen.Pedido.crear(it)) }
                )
            }

            composable(Screen.Factura.route, arguments = argumentoId) { entrada ->
                FacturaDetalleScreen(
                    id = entrada.id(),
                    viewModel = viewModels.facturaDetalle,
                    onBack = { navController.popBackStack() },
                    onPedido = { irA(Screen.Pedido.crear(it)) }
                )
            }

            composable(Screen.Notificaciones.route) {
                NotificacionesScreen(
                    viewModel = viewModels.notificaciones,
                    onBack = { navController.popBackStack() },
                    onAbrir = { notificacion -> rutaDeNotificacion(notificacion)?.let { irA(it) } }
                )
            }

            composable(Screen.Soporte.route) {
                if (usuario == null) {
                    LaunchedEffect(Unit) { gate = AccionConSesion("Para contactar a soporte necesitas iniciar sesión.", Screen.Soporte.route); navController.popBackStack() }
                } else {
                    SoporteScreen(
                        viewModel = viewModels.soporte,
                        negocio = negocio,
                        onBack = { navController.popBackStack() },
                        onNueva = { irA(Screen.SoporteNuevo.route) },
                        onDetalle = { irA(Screen.SoporteDetalle.crear(it)) }
                    )
                }
            }

            composable(Screen.SoporteNuevo.route) {
                SoporteNuevoScreen(
                    viewModel = viewModels.soporte,
                    onBack = { navController.popBackStack() },
                    onCreada = { soporteId ->
                        navController.navigate(Screen.SoporteDetalle.crear(soporteId)) {
                            popUpTo(Screen.SoporteNuevo.route) { inclusive = true }
                        }
                    }
                )
            }

            composable(Screen.SoporteDetalle.route, arguments = argumentoId) { entrada ->
                SoporteDetalleScreen(id = entrada.id(), viewModel = viewModels.soporte, onBack = { navController.popBackStack() })
            }
        }
    }
}
