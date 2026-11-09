package com.example.a3df_studio_app.ui.screens.splash

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ViewInAr
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.a3df_studio_app.domain.models.AuthState
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    authState: AuthState,
    onFinish: () -> Unit
) {
    LaunchedEffect(authState is AuthState.Loading) {
        if (authState !is AuthState.Loading) {
            delay(1200)
            onFinish()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.primary),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            Icons.Rounded.ViewInAr,
            contentDescription = "3DForge Studio",
            tint = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier.size(120.dp)
        )
        Spacer(Modifier.height(16.dp))
        Text("3DForge Studio", style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.onPrimary)
        Text("Impresión 3D a tu medida", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onPrimary)
        Spacer(Modifier.height(40.dp))
        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
    }
}
