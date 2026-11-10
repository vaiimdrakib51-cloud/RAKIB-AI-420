package com.nova.ai.presentation.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val NovaDarkColorScheme = darkColorScheme(
    primary = NovaPrimaryPurple,
    secondary = NovaSecondaryBlue,
    tertiary = NovaAccentCyan,
    background = NovaBackground,
    surface = NovaSecondaryBackground,
    surfaceVariant = NovaCardBackground,
    onPrimary = NovaPrimaryText,
    onSecondary = NovaPrimaryText,
    onTertiary = NovaBackground,
    onBackground = NovaPrimaryText,
    onSurface = NovaPrimaryText,
    onSurfaceVariant = NovaSecondaryText,
    error = NovaError
)

@Composable
fun NovaAITheme(
    content: @Composable () -> Unit
) {
    val colorScheme = NovaDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = NovaBackground.toArgb()
            window.navigationBarColor = NovaBackground.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = NovaTypography,
        content = content
    )
}
