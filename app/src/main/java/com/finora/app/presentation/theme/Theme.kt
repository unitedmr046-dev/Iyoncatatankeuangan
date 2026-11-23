package com.finora.app.presentation.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val EmeraldPrimary = Color(0xFF10B981)
private val EmeraldDark = Color(0xFF059669)
private val SlateBackground = Color(0xFFF8FAFC)
private val SlateDarkBackground = Color(0xFF0F172A)
private val SlateDarkSurface = Color(0xFF1E293B)

private val LightColors = lightColorScheme(
    primary = EmeraldDark,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1FAE5),
    onPrimaryContainer = Color(0xFF064E3B),
    secondary = Color(0xFF0F172A),
    onSecondary = Color.White,
    background = SlateBackground,
    onBackground = Color(0xFF0F172A),
    surface = Color.White,
    onSurface = Color(0xFF0F172A),
    surfaceVariant = Color(0xFFF1F5F9),
    onSurfaceVariant = Color(0xFF475569),
    error = Color(0xFFEF4444)
)

private val DarkColors = darkColorScheme(
    primary = EmeraldPrimary,
    onPrimary = Color(0xFF022C22),
    primaryContainer = Color(0xFF064E3B),
    onPrimaryContainer = Color(0xFFA7F3D0),
    secondary = Color(0xFF34D399),
    onSecondary = Color(0xFF0F172A),
    background = SlateDarkBackground,
    onBackground = Color(0xFFF8FAFC),
    surface = SlateDarkSurface,
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFFCBD5E1),
    error = Color(0xFFF87171)
)

@Composable
fun FinoraTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
