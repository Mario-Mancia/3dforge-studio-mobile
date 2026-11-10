package com.example.a3df_studio_app.ui.navigation

import android.net.Uri
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Catalogo : Screen("catalogo")
    data object Carrito : Screen("carrito")
    data object Actividad : Screen("actividad")
    data object Cuenta : Screen("cuenta")
    data object Notificaciones : Screen("notificaciones")
    data object Soporte : Screen("soporte")
    data object SoporteNuevo : Screen("soporte-nuevo")
    data object Recuperar : Screen("recuperar")
    data object Cotizar3D : Screen("cotizar-3d")

    data object Login : Screen("login?redirect={redirect}") {
        fun crear(redirect: String? = null): String =
            if (redirect == null) "login" else "login?redirect=${Uri.encode(redirect)}"
    }

    data object Registro : Screen("registro?redirect={redirect}") {
        fun crear(redirect: String? = null): String =
            if (redirect == null) "registro" else "registro?redirect=${Uri.encode(redirect)}"
    }

    data object Cotizar : Screen("cotizar?servicioId={servicioId}") {
        fun crear(servicioId: Long? = null): String =
            if (servicioId == null) "cotizar" else "cotizar?servicioId=$servicioId"
    }

    data object Producto : Screen("producto/{id}") {
        fun crear(id: Long) = "producto/$id"
    }

    data object Servicio : Screen("servicio/{id}") {
        fun crear(id: Long) = "servicio/$id"
    }

    data object Pedido : Screen("pedido/{id}") {
        fun crear(id: Long) = "pedido/$id"
    }

    data object Cotizacion : Screen("cotizacion/{id}") {
        fun crear(id: Long) = "cotizacion/$id"
    }

    data object Factura : Screen("factura/{id}") {
        fun crear(id: Long) = "factura/$id"
    }

    data object SoporteDetalle : Screen("soporte/{id}") {
        fun crear(id: Long) = "soporte/$id"
    }
}

data class BottomItem(
    val screen: Screen,
    val etiqueta: String,
    val icono: ImageVector,
    val iconoSeleccionado: ImageVector
)

val bottomItems = listOf(
    BottomItem(Screen.Home, "Inicio", Icons.Outlined.Home, Icons.Rounded.Home),
    BottomItem(Screen.Catalogo, "Catálogo", Icons.Outlined.Inventory2, Icons.Rounded.Inventory2),
    BottomItem(Screen.Carrito, "Carrito", Icons.Outlined.ShoppingCart, Icons.Rounded.ShoppingCart),
    BottomItem(Screen.Actividad, "Actividad", Icons.Outlined.ReceiptLong, Icons.Rounded.ReceiptLong),
    BottomItem(Screen.Cuenta, "Cuenta", Icons.Outlined.Person, Icons.Rounded.Person)
)

val rutasPrincipales: Set<String> = bottomItems.map { it.screen.route }.toSet()
