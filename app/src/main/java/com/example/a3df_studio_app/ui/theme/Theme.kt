package com.example.a3df_studio_app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors = lightColorScheme(
    primary = ForgePrimary,
    onPrimary = Color.White,
    primaryContainer = ForgePrimaryContainer,
    onPrimaryContainer = ForgeOnPrimaryContainer,
    secondary = ForgePrimaryHover,
    onSecondary = Color.White,
    secondaryContainer = ForgePrimaryContainer,
    onSecondaryContainer = ForgeOnPrimaryContainer,
    tertiary = ForgePrimaryHover,
    background = ForgeBackground,
    onBackground = ForgeTextPrimary,
    surface = ForgeSurface,
    onSurface = ForgeTextPrimary,
    surfaceVariant = ForgeSurfaceSecondary,
    onSurfaceVariant = ForgeTextSecondary,
    surfaceContainerLowest = ForgeSurface,
    surfaceContainerLow = ForgeSurface,
    surfaceContainer = ForgeSurface,
    surfaceContainerHigh = ForgeSurfaceSecondary,
    surfaceContainerHighest = ForgeSurfaceSecondary,
    outline = ForgeBorder,
    outlineVariant = ForgeBorder,
    error = ForgeError
)

private val DarkColors = darkColorScheme(
    primary = ForgePrimaryDark,
    onPrimary = ForgeBackgroundDark,
    primaryContainer = ForgePrimaryContainerDark,
    onPrimaryContainer = ForgeOnPrimaryContainerDark,
    secondary = ForgePrimaryHoverDark,
    onSecondary = ForgeBackgroundDark,
    secondaryContainer = ForgePrimaryContainerDark,
    onSecondaryContainer = ForgeOnPrimaryContainerDark,
    tertiary = ForgePrimaryHoverDark,
    background = ForgeBackgroundDark,
    onBackground = ForgeTextPrimaryDark,
    surface = ForgeSurfaceDark,
    onSurface = ForgeTextPrimaryDark,
    surfaceVariant = ForgeSurfaceSecondaryDark,
    onSurfaceVariant = ForgeTextSecondaryDark,
    surfaceContainerLowest = ForgeBackgroundDark,
    surfaceContainerLow = ForgeSurfaceDark,
    surfaceContainer = ForgeSurfaceDark,
    surfaceContainerHigh = ForgeSurfaceElevatedDark,
    surfaceContainerHighest = ForgeSurfaceElevatedDark,
    outline = ForgeBorderDark,
    outlineVariant = ForgeBorderDark,
    error = ForgeErrorDark
)

@Composable
fun ForgeStudioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = ForgeTypography,
        content = content
    )
}
