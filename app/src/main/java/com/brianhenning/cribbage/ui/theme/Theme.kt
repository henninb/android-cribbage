package com.brianhenning.cribbage.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import java.time.LocalDate

// CompositionLocal for accessing the current seasonal theme throughout the app
val LocalSeasonalTheme = staticCompositionLocalOf { ThemeCalculator.getCurrentTheme() }

private val LightColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    onError = md_theme_light_onError,
    errorContainer = md_theme_light_errorContainer,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline
)

private val DarkColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    onError = md_theme_dark_onError,
    errorContainer = md_theme_dark_errorContainer,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline
)

@Composable
fun CribbageTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,  // Disabled to use seasonal themes
    useSeasonalThemes: Boolean = true,  // Enable seasonal theming
    overrideTheme: CribbageTheme? = null,  // Optional theme override for manual selection
    content: @Composable () -> Unit
) {
    // Get the current seasonal theme (use override if provided)
    val seasonalTheme = overrideTheme ?: ThemeCalculator.getCurrentTheme()

    // Determine if this is a dark theme (Fall/Winter have dark backgrounds)
    val isDarkSeasonalTheme = seasonalTheme.type == ThemeType.FALL ||
                              seasonalTheme.type == ThemeType.WINTER ||
                              seasonalTheme.type == ThemeType.HALLOWEEN

    // Build color scheme from seasonal theme
    val colorScheme = when {
        useSeasonalThemes && isDarkSeasonalTheme -> {
            // Dark color scheme for Fall, Winter, Halloween
            darkColorScheme(
                primary = seasonalTheme.colors.primary,
                primaryContainer = seasonalTheme.colors.primaryVariant,
                onPrimaryContainer = Color.White,
                secondary = seasonalTheme.colors.secondary,
                secondaryContainer = seasonalTheme.colors.secondaryVariant,
                onSecondaryContainer = Color.White,
                background = seasonalTheme.colors.background,
                onBackground = Color.White,
                surface = seasonalTheme.colors.surface,
                onSurface = Color.White,
                tertiary = seasonalTheme.colors.boardPrimary,
                tertiaryContainer = seasonalTheme.colors.boardSecondary,
                surfaceVariant = seasonalTheme.colors.cardBack,
                onSurfaceVariant = Color.White,
                outline = seasonalTheme.colors.accentLight
            )
        }
        useSeasonalThemes -> {
            // Light color scheme for Spring, Summer, Holidays
            lightColorScheme(
                primary = seasonalTheme.colors.primary,
                primaryContainer = seasonalTheme.colors.primaryVariant,
                secondary = seasonalTheme.colors.secondary,
                secondaryContainer = seasonalTheme.colors.secondaryVariant,
                background = seasonalTheme.colors.background,
                surface = seasonalTheme.colors.surface,
                tertiary = seasonalTheme.colors.boardPrimary,
                tertiaryContainer = seasonalTheme.colors.boardSecondary,
                surfaceVariant = seasonalTheme.colors.cardBack,
                outline = seasonalTheme.colors.accentDark
            )
        }
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
            window.statusBarColor = colorScheme.primary.toArgb()
            // Use light status bars for light themes, dark for dark themes
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars =
                useSeasonalThemes && !isDarkSeasonalTheme
        }
    }

    CompositionLocalProvider(LocalSeasonalTheme provides seasonalTheme) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = Typography,
            content = content
        )
    }
}