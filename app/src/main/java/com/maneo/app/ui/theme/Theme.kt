package com.maneo.app.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

// Spec §7 — all colour tokens mapped to Material3 roles
private val ManeoColorScheme = lightColorScheme(
    primary          = Primary,
    onPrimary        = OnPrimary,
    background       = Background,
    surface          = Surface,
    onBackground     = TextPrimary,
    onSurface        = TextPrimary,
    onSurfaceVariant = TextSecondary,
    outline          = Border,
    surfaceVariant   = Surface,
)

@Composable
fun ManeoTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ManeoColorScheme,
        typography  = ManeoTypography,
        shapes      = ManeoShapes,
        content     = content,
    )
}
