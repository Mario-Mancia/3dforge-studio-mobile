package com.example.a3df_studio_app.ui.components.topbar

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.AuthUser
import com.example.a3df_studio_app.ui.components.common.UserAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgeTopBar(
    nombreNegocio: String,
    usuario: AuthUser?,
    noLeidas: Int,
    onLogoClick: () -> Unit,
    onNotificaciones: () -> Unit,
    onAvatarClick: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onLogoClick) {
                Icon(
                    imageVector = Icons.Rounded.ViewInAr,
                    contentDescription = nombreNegocio,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(30.dp)
                )
            }
        },
        title = { Text(nombreNegocio, style = MaterialTheme.typography.titleLarge) },
        actions = {
            if (usuario != null) {
                IconButton(onClick = onNotificaciones) {
                    BadgedBox(
                        badge = { if (noLeidas > 0) Badge { Text(if (noLeidas > 99) "99+" else noLeidas.toString()) } }
                    ) {
                        Icon(Icons.Rounded.Notifications, contentDescription = "Notificaciones")
                    }
                }
            }
            IconButton(onClick = onAvatarClick, modifier = Modifier.padding(end = 4.dp)) {
                UserAvatar(avatarUrl = null, iniciales = usuario?.iniciales)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackTopBar(
    titulo: String,
    onBack: () -> Unit
) {
    TopAppBar(
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Volver")
            }
        },
        title = { Text(titulo, style = MaterialTheme.typography.titleLarge) },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
    )
}
