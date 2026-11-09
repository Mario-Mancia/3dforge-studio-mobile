package com.example.a3df_studio_app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Inventory2
import androidx.compose.material.icons.outlined.ReceiptLong
import androidx.compose.material.icons.outlined.RequestQuote
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Inventory2
import androidx.compose.material.icons.rounded.ReceiptLong
import androidx.compose.material.icons.rounded.RequestQuote
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String) {
    data object Splash : Screen("splash")
    data object Home : Screen("home")
    data object Productos : Screen("productos")
    data object Pedidos : Screen("pedidos")
    data object Cotizaciones : Screen("cotizaciones")
    data object Feedback : Screen("feedback")
    data object Login : Screen("login")
    data object Registro : Screen("registro")
    data object Perfil : Screen("perfil")

    data object Verificar : Screen("verificar/{correo}") {
        fun crear(correo: String) = "verificar/${android.net.Uri.encode(correo)}"
    }

    data object Pago : Screen("pago/{idFactura}") {
        fun crear(idFactura: Long) = "pago/$idFactura"
    }

    data object EncargoDetalle : Screen("encargo/{idEncargo}") {
        fun crear(idEncargo: Long) = "encargo/$idEncargo"
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
    BottomItem(Screen.Productos, "Productos", Icons.Outlined.Inventory2, Icons.Rounded.Inventory2),
    BottomItem(Screen.Pedidos, "Pedidos", Icons.Outlined.ReceiptLong, Icons.Rounded.ReceiptLong),
    BottomItem(Screen.Cotizaciones, "Cotizar", Icons.Outlined.RequestQuote, Icons.Rounded.RequestQuote),
    BottomItem(Screen.Feedback, "Opinión", Icons.Outlined.Feedback, Icons.Rounded.Feedback)
)
