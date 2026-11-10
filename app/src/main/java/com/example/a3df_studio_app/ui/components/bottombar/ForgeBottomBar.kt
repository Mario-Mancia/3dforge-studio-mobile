package com.example.a3df_studio_app.ui.components.bottombar

import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.example.a3df_studio_app.ui.navigation.Screen
import com.example.a3df_studio_app.ui.navigation.bottomItems

@Composable
fun ForgeBottomBar(
    rutaActual: String?,
    cantidadCarrito: Int,
    onNavigate: (Screen) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
        bottomItems.forEach { item ->
            val seleccionado = rutaActual == item.screen.route
            NavigationBarItem(
                selected = seleccionado,
                onClick = { if (!seleccionado) onNavigate(item.screen) },
                icon = {
                    val icono = if (seleccionado) item.iconoSeleccionado else item.icono
                    if (item.screen == Screen.Carrito && cantidadCarrito > 0) {
                        BadgedBox(badge = { Badge { Text(if (cantidadCarrito > 99) "99+" else cantidadCarrito.toString()) } }) {
                            Icon(imageVector = icono, contentDescription = item.etiqueta)
                        }
                    } else {
                        Icon(imageVector = icono, contentDescription = item.etiqueta)
                    }
                },
                label = { Text(item.etiqueta) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                )
            )
        }
    }
}
