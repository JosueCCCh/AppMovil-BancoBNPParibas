package com.example.proyectoarquiempresarial.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = Green700,
    onPrimary = Color.White,
    primaryContainer = Green50,
    onPrimaryContainer = Green900,
    secondary = Gold600,
    onSecondary = Color.White,
    secondaryContainer = Gold100,
    onSecondaryContainer = Color(0xFF3F3100),
    tertiary = Green500,
    onTertiary = Color.White,
    background = SurfaceLight,
    onBackground = Ink900,
    surface = Color.White,
    onSurface = Ink900,
    surfaceVariant = Ink50,
    onSurfaceVariant = Ink700,
    outline = Color(0xFF7A8A82),
    outlineVariant = Ink100,
    error = ErrorRed,
    onError = Color.White,
    errorContainer = ErrorContainerLight,
    onErrorContainer = Color(0xFF410E0B)
)

private val DarkColorScheme = darkColorScheme(
    primary = Green200,
    onPrimary = Green900,
    primaryContainer = Green800,
    onPrimaryContainer = Green50,
    secondary = Gold500,
    onSecondary = Color(0xFF231B00),
    secondaryContainer = Color(0xFF3F3100),
    onSecondaryContainer = Gold100,
    tertiary = Green200,
    onTertiary = Green900,
    background = SurfaceDark,
    onBackground = Color(0xFFE6ECE8),
    surface = CardDark,
    onSurface = Color(0xFFE6ECE8),
    surfaceVariant = Color(0xFF1E2B25),
    onSurfaceVariant = Color(0xFFB8C6BF),
    outline = Color(0xFF7A8A82),
    outlineVariant = Color(0xFF2C3B34),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = ErrorContainerDark,
    onErrorContainer = Color(0xFFFFDAD6)
)

@Composable
fun ProyectoArquiEmpresarialTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
