package com.shreejicls.app.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = SaffronOrange,
    onPrimary = WarmWhite,
    primaryContainer = SaffronSurface,
    onPrimaryContainer = SaffronDark,
    secondary = LeafGreen,
    onSecondary = WarmWhite,
    secondaryContainer = GreenSurface,
    onSecondaryContainer = LeafGreenDark,
    tertiary = GoldAccent,
    onTertiary = DarkText,
    tertiaryContainer = GoldLight,
    onTertiaryContainer = GoldDark,
    background = WarmWhite,
    onBackground = DarkText,
    surface = WarmWhite,
    onSurface = DarkText,
    surfaceVariant = WarmGray,
    onSurfaceVariant = LightText,
    error = ErrorRed,
    onError = WarmWhite,
    outline = MediumGray
)

private val DarkColorScheme = darkColorScheme(
    primary = DarkSaffron,
    onPrimary = DarkText,
    primaryContainer = SaffronDark,
    onPrimaryContainer = SaffronLight,
    secondary = DarkGreen,
    onSecondary = DarkText,
    secondaryContainer = LeafGreenDark,
    onSecondaryContainer = DarkGreen,
    tertiary = DarkGold,
    onTertiary = DarkText,
    tertiaryContainer = GoldDark,
    onTertiaryContainer = GoldLight,
    background = DarkBackground,
    onBackground = WarmWhite,
    surface = DarkSurface,
    onSurface = WarmWhite,
    surfaceVariant = DarkCard,
    onSurfaceVariant = MediumGray,
    error = ErrorRed,
    onError = WarmWhite,
    outline = MediumGray
)

@Composable
fun ShreeJiTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
